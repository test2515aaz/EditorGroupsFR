// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl.singleRow;

import com.intellij.ui.ExperimentalUI;
import krasa.editorGroups.tabs2.impl.*;
import krasa.editorGroups.tabs2.label.EditorGroupTabInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class KrSingleRowLayout extends KrTabLayout {
  final KrTabsImpl myTabs;
  public KrSingleRowPassInfo lastSingleRowLayout;

  private final KrSingleRowLayoutStrategy myTop;
  private final KrSingleRowLayoutStrategy myLeft;
  private final KrSingleRowLayoutStrategy myBottom;
  private final KrSingleRowLayoutStrategy myRight;

  @Override
  public boolean isSideComponentOnTabs() {
    return getStrategy().isSideComponentOnTabs();
  }

  @Override
  public KrShapeTransform createShapeTransform(Rectangle labelRec) {
    return getStrategy().createShapeTransform(labelRec);
  }

  @Override
  public boolean isDragOut(@NotNull EditorGroupTabLabel tabLabel, int deltaX, int deltaY) {
    return getStrategy().isDragOut(tabLabel, deltaX, deltaY);
  }

  public KrSingleRowLayout(final KrTabsImpl tabs) {
    myTabs = tabs;
    myTop = new KrSingleRowLayoutStrategy.Top(this);
    myLeft = new KrSingleRowLayoutStrategy.Left(this);
    myBottom = new KrSingleRowLayoutStrategy.Bottom(this);
    myRight = new KrSingleRowLayoutStrategy.Right(this);
  }

  KrSingleRowLayoutStrategy getStrategy() {
    return switch (myTabs.getPresentation().getTabsPosition()) {
      case TOP -> myTop;
      case BOTTOM -> myBottom;
    };
  }

  protected boolean checkLayoutLabels(KrSingleRowPassInfo data) {
    boolean layoutLabels = true;

    if (!myTabs.getForcedRelayout$EditorGroups() &&
      lastSingleRowLayout != null &&
      lastSingleRowLayout.contentCount == myTabs.getTabCount() &&
      lastSingleRowLayout.layoutSize.equals(myTabs.getSize()) &&
      lastSingleRowLayout.scrollOffset == getScrollOffset()) {
      for (EditorGroupTabInfo each : data.myVisibleInfos) {
        final EditorGroupTabLabel eachLabel = myTabs.getInfoToLabel().get(each);
        if (!eachLabel.isValid()) {
          layoutLabels = true;
          break;
        }
        if (myTabs.getSelectedInfo() == each) {
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

    final EditorGroupTabInfo selected = myTabs.getSelectedInfo();
    prepareLayoutPassInfo(data, selected);

    myTabs.resetLayout(shouldLayoutLabels || myTabs.isHideTabs());

    if (shouldLayoutLabels && !myTabs.isHideTabs()) {
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
      final EditorGroupTabLabel firstLabel = myTabs.getInfoToLabel().get(data.toLayout.get(0));
      final EditorGroupTabLabel lastLabel = findLastVisibleLabel(data);
      if (firstLabel != null && lastLabel != null) {
        data.tabRectangle.x = firstLabel.getBounds().x;
        data.tabRectangle.y = firstLabel.getBounds().y;
        data.tabRectangle.width = ExperimentalUI.isNewUI()
          ? (int) data.entryPointRect.getMaxX() + myTabs.getActionsInsets().right - data.tabRectangle.x
          : (int) lastLabel.getBounds().getMaxX() - data.tabRectangle.x;
        data.tabRectangle.height = (int) lastLabel.getBounds().getMaxY() - data.tabRectangle.y;
      }
    }

    lastSingleRowLayout = data;
    return data;
  }

  @Nullable
  protected EditorGroupTabLabel findLastVisibleLabel(KrSingleRowPassInfo data) {
    return myTabs.getInfoToLabel().get(data.toLayout.get(data.toLayout.size() - 1));
  }

  protected void prepareLayoutPassInfo(KrSingleRowPassInfo data, EditorGroupTabInfo selected) {
    data.insets = myTabs.getLayoutInsets();
    if (myTabs.isHorizontalTabs()) {
      data.insets.left += myTabs.getFirstTabOffset();
    }

    final KrTabsImpl.Toolbar selectedToolbar = myTabs.getInfoToToolbar().get(selected);
    data.hToolbar =
      new WeakReference<>(selectedToolbar != null && myTabs.getHorizontalSide() && !selectedToolbar.isEmpty() ? selectedToolbar : null);
    data.vToolbar =
      new WeakReference<>(selectedToolbar != null && !myTabs.getHorizontalSide() && !selectedToolbar.isEmpty() ? selectedToolbar : null);
    data.toFitLength = getStrategy().getToFitLength(data);
  }

  protected void layoutTitle(KrSingleRowPassInfo data) {
    data.titleRect = getStrategy().getTitleRect(data);
    data.position += myTabs.isHorizontalTabs() ? data.titleRect.width : data.titleRect.height;
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
      final EditorGroupTabLabel label = myTabs.getInfoToLabel().get(eachInfo);
      if (layoutStopped) {
        final Rectangle rec = getStrategy().getLayoutRect(data, 0, 0);
        myTabs.layout(label, rec);
        continue;
      }

      final Dimension eachSize = label.getPreferredSize();

      int length = getStrategy().getLengthIncrement(eachSize);
      boolean continueLayout = applyTabLayout(data, label, length);

      data.position = getStrategy().getMaxPosition(label.getBounds());
      data.position += myTabs.getTabHGap();

      if (!continueLayout) {
        layoutStopped = true;
      }
    }

    for (EditorGroupTabInfo eachInfo : data.toDrop) {
      KrTabsImpl.Companion.resetLayout(myTabs.getInfoToLabel().get(eachInfo));
    }
  }

  protected boolean applyTabLayout(KrSingleRowPassInfo data, EditorGroupTabLabel label, int length) {
    final Rectangle rec = getStrategy().getLayoutRect(data, data.position, length);
    myTabs.layout(label, rec);

    label.setAlignmentToCenter(myTabs.isEditorTabs() && getStrategy().isToCenterTextWhenStretched());
    return true;
  }


  protected abstract void recomputeToLayout(final KrSingleRowPassInfo data);

  protected void calculateRequiredLength(KrSingleRowPassInfo data) {
    data.requiredLength += myTabs.isHorizontalTabs() ? data.insets.left + data.insets.right
      : data.insets.top + data.insets.bottom;
    for (EditorGroupTabInfo eachInfo : data.myVisibleInfos) {
      data.requiredLength += getRequiredLength(eachInfo);
      data.toLayout.add(eachInfo);
    }
    data.requiredLength += getStrategy().getAdditionalLength();
  }

  protected int getRequiredLength(EditorGroupTabInfo eachInfo) {
    EditorGroupTabLabel label = myTabs.getInfoToLabel().get(eachInfo);
    return getStrategy().getLengthIncrement(label != null ? label.getPreferredSize() : new Dimension())
      + (myTabs.isEditorTabs() ? myTabs.getTabHGap() : 0);
  }


  @Override
  public boolean isTabHidden(@NotNull EditorGroupTabInfo info) {
    return lastSingleRowLayout != null && lastSingleRowLayout.toDrop.contains(info);
  }

}
