// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl.singleRow;

import com.intellij.ui.ExperimentalUI;
import krasa.editorGroups.tabs2.impl.KrShapeTransform;
import krasa.editorGroups.tabs2.impl.KrTabLayout;
import krasa.editorGroups.tabs2.impl.KrTabsImpl;
import krasa.editorGroups.tabs2.label.EditorGroupTabInfo;
import krasa.editorGroups.tabs2.label.EditorGroupTabLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class KrSingleRowLayout extends KrTabLayout {
  final KrTabsImpl tabs;
  public KrSingleRowPassInfo lastSingleRowLayout;

  private final KrSingleRowLayoutStrategy myTop;
  private final KrSingleRowLayoutStrategy myBottom;

  @Override
  public KrShapeTransform createShapeTransform(Rectangle labelRec) {
    return getStrategy().createShapeTransform(labelRec);
  }

  public KrSingleRowLayout(final KrTabsImpl tabs) {
    this.tabs = tabs;
    myTop = new KrSingleRowLayoutStrategy.Top(this);
    myBottom = new KrSingleRowLayoutStrategy.Bottom(this);
  }

  KrSingleRowLayoutStrategy getStrategy() {
    return switch (tabs.getPresentation().getTabsPosition()) {
      case TOP -> myTop;
      case BOTTOM -> myBottom;
    };
  }

  protected boolean checkLayoutLabels(KrSingleRowPassInfo data) {
    boolean layoutLabels = true;

    if (!tabs.getForcedRelayout$EditorGroups() &&
      lastSingleRowLayout != null &&
      lastSingleRowLayout.contentCount == tabs.getTabCount() &&
      lastSingleRowLayout.layoutSize.equals(tabs.getSize()) &&
      lastSingleRowLayout.scrollOffset == getScrollOffset()) {
      for (EditorGroupTabInfo each : data.myVisibleInfos) {
        final EditorGroupTabLabel eachLabel = tabs.getInfoToLabel().get(each);
        if (!eachLabel.isValid()) {
          layoutLabels = true;
          break;
        }
        if (tabs.getSelectedInfo() == each) {
          if (eachLabel.getBounds().width != 0) {
            layoutLabels = false;
          }
        }
      }
    }

    return layoutLabels;
  }

  public KrLayoutPassInfo layoutSingleRow(List<EditorGroupTabInfo> visibleInfos) {
    KrSingleRowPassInfo data = new KrSingleRowPassInfo(this, visibleInfos);

    final boolean shouldLayoutLabels = checkLayoutLabels(data);
    if (!shouldLayoutLabels) {
      data = lastSingleRowLayout;
    }

    final EditorGroupTabInfo selected = tabs.getSelectedInfo();
    prepareLayoutPassInfo(data, selected);

    tabs.resetLayout(shouldLayoutLabels || tabs.isHideTabs());

    if (shouldLayoutLabels && !tabs.isHideTabs()) {
      recomputeToLayout(data);

      data.position = getStrategy().getStartPosition(data) - getScrollOffset();

      layoutTitle(data);

      layoutLabels(data);
      layoutEntryPointButton(data);
      layoutMoreButton(data);

    }

    if (selected != null) {
      data.component = new WeakReference<>(selected.getComponent());
      getStrategy().layoutComp(data);
    }

    data.tabRectangle = new Rectangle();

    if (!data.toLayout.isEmpty()) {
      final EditorGroupTabLabel firstLabel = tabs.getInfoToLabel().get(data.toLayout.get(0));
      final EditorGroupTabLabel lastLabel = findLastVisibleLabel(data);
      if (firstLabel != null && lastLabel != null) {
        data.tabRectangle.x = firstLabel.getBounds().x;
        data.tabRectangle.y = firstLabel.getBounds().y;
        data.tabRectangle.width = ExperimentalUI.isNewUI()
          ? (int) data.entryPointRect.getMaxX() + tabs.getActionsInsets().right - data.tabRectangle.x
          : (int) lastLabel.getBounds().getMaxX() - data.tabRectangle.x;
        data.tabRectangle.height = (int) lastLabel.getBounds().getMaxY() - data.tabRectangle.y;
      }
    }

    lastSingleRowLayout = data;
    return data;
  }

  @Nullable
  protected EditorGroupTabLabel findLastVisibleLabel(KrSingleRowPassInfo data) {
    return tabs.getInfoToLabel().get(data.toLayout.get(data.toLayout.size() - 1));
  }

  protected void prepareLayoutPassInfo(KrSingleRowPassInfo data, EditorGroupTabInfo selected) {
    data.insets = tabs.getLayoutInsets();
    if (tabs.isHorizontalTabs()) {
      data.insets.left += tabs.getFirstTabOffset();
    }

    final KrTabsImpl.Toolbar selectedToolbar = tabs.getInfoToToolbar().get(selected);
    data.hToolbar =
      new WeakReference<>(selectedToolbar != null && tabs.getHorizontalSide() && !selectedToolbar.isEmpty() ? selectedToolbar : null);
    data.vToolbar =
      new WeakReference<>(selectedToolbar != null && !tabs.getHorizontalSide() && !selectedToolbar.isEmpty() ? selectedToolbar : null);
    data.toFitLength = getStrategy().getToFitLength(data);
  }

  protected void layoutTitle(KrSingleRowPassInfo data) {
    data.titleRect = getStrategy().getTitleRect(data);
    data.position += tabs.isHorizontalTabs() ? data.titleRect.width : data.titleRect.height;
  }

  protected void layoutMoreButton(KrSingleRowPassInfo data) {
    if (!data.toDrop.isEmpty()) {
      data.moreRect = getStrategy().getMoreRect(data);
    }
  }

  protected void layoutEntryPointButton(KrSingleRowPassInfo data) {
    data.entryPointRect = getStrategy().getEntryPointRect(data);
  }

  protected void layoutLabels(final KrSingleRowPassInfo data) {
    boolean layoutStopped = false;
    for (EditorGroupTabInfo eachInfo : data.toLayout) {
      final EditorGroupTabLabel label = tabs.getInfoToLabel().get(eachInfo);
      if (layoutStopped) {
        final Rectangle rec = getStrategy().getLayoutRect(data, 0, 0);
        tabs.layout(label, rec);
        continue;
      }

      final Dimension eachSize = label.getPreferredSize();

      int length = getStrategy().getLengthIncrement(eachSize);
      boolean continueLayout = applyTabLayout(data, label, length);

      data.position = getStrategy().getMaxPosition(label.getBounds());
      data.position += tabs.getTabHGap();

      if (!continueLayout) {
        layoutStopped = true;
      }
    }

    for (EditorGroupTabInfo eachInfo : data.toDrop) {
      KrTabsImpl.Companion.resetLayout(tabs.getInfoToLabel().get(eachInfo));
    }
  }

  protected boolean applyTabLayout(KrSingleRowPassInfo data, EditorGroupTabLabel label, int length) {
    final Rectangle rec = getStrategy().getLayoutRect(data, data.position, length);
    tabs.layout(label, rec);

    label.setAlignmentToCenter();
    return true;
  }


  protected abstract void recomputeToLayout(final KrSingleRowPassInfo data);

  protected void calculateRequiredLength(KrSingleRowPassInfo data) {
    data.setRequiredLength(data.getRequiredLength() + data.insets.left + data.insets.right);

    for (EditorGroupTabInfo eachInfo : data.myVisibleInfos) {
      data.setRequiredLength(data.getRequiredLength() + getRequiredLength(eachInfo));
      data.toLayout.add(eachInfo);
    }

    data.setRequiredLength(data.getRequiredLength() + getStrategy().getAdditionalLength());
  }

  protected int getRequiredLength(EditorGroupTabInfo eachInfo) {
    EditorGroupTabLabel label = tabs.getInfoToLabel().get(eachInfo);
    return getStrategy().getLengthIncrement(label != null ? label.getPreferredSize() : new Dimension())
      + (tabs.isEditorTabs() ? tabs.getTabHGap() : 0);
  }


  @Override
  public boolean isTabHidden(@NotNull EditorGroupTabInfo info) {
    return lastSingleRowLayout != null && lastSingleRowLayout.toDrop.contains(info);
  }

}
