// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl.table;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import krasa.editorGroups.tabs2.EditorGroupTabInfo;
import krasa.editorGroups.tabs2.impl.KrLayoutPassInfo;
import krasa.editorGroups.tabs2.impl.KrTabLabel;
import krasa.editorGroups.tabs2.impl.KrTabLayout;
import krasa.editorGroups.tabs2.impl.KrTabsImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class KrTableLayout extends KrTabLayout {
  private int myScrollOffset;

  final KrTabsImpl myTabs;

  public KrTablePassInfo lastTableLayout;

  private final boolean myWithScrollBar;

  public KrTableLayout(KrTabsImpl tabs) {
    this(tabs, false);
  }

  public KrTableLayout(KrTabsImpl tabs, final boolean isWithScrollBar) {
    myTabs = tabs;
    myWithScrollBar = isWithScrollBar;
  }

  private KrTablePassInfo computeLayoutTable(final List<EditorGroupTabInfo> visibleInfos) {
    KrTablePassInfo data = new KrTablePassInfo(this, visibleInfos);
    if (myTabs.isHideTabs()) {
      return data;
    }
    doScrollToSelectedTab(lastTableLayout);

    final boolean singleRow = myTabs.isSingleRow();
    final boolean showPinnedTabsSeparately = KrTabLayout.showPinnedTabsSeparately();
    final boolean scrollable = UISettings.getInstance().getHideTabsIfNeeded() && singleRow;
    final int titleWidth = myTabs.getTitleWrapper().getPreferredSize().width;

    data.titleRect.setBounds(data.toFitRec.x, data.toFitRec.y, titleWidth, myTabs.getHeaderFitSize().height);
    data.entryPointRect.setBounds(data.toFitRec.x + data.toFitRec.width - myTabs.getEntryPointPreferredSize().width - myTabs.getActionsInsets().right,
      data.toFitRec.y,
      myTabs.getEntryPointPreferredSize().width,
      myTabs.getHeaderFitSize().height);
    data.moreRect.setBounds(data.toFitRec.x + data.toFitRec.width - myTabs.getEntryPointPreferredSize().width - myTabs.getActionsInsets().right,
      data.toFitRec.y, 0, myTabs.getHeaderFitSize().height);
    calculateLengths(data);

    int eachX = data.titleRect.x + data.titleRect.width;
    final Insets insets = myTabs.getLayoutInsets();
    int eachY = insets.top;
    int requiredRowsPinned = 0;
    int requiredRowsUnpinned = 0;

    int maxX = data.moreRect.x - (singleRow ? myTabs.getActionsInsets().left : 0);
    if (!singleRow && showPinnedTabsSeparately) {
      maxX += myTabs.getEntryPointPreferredSize().width;
    }

    final int hGap = myTabs.getTabHGap();
    int entryPointMargin = scrollable ? 0 : myTabs.getEntryPointPreferredSize().width;
    for (final EditorGroupTabInfo eachInfo : data.myVisibleInfos) {
      final KrTabLabel eachLabel = myTabs.getTabLabel(eachInfo);
      final boolean pinned = eachLabel.isPinned();
      int width = data.lengths.get(eachInfo);
      if (!pinned || !showPinnedTabsSeparately) {
        data.requiredLength += width;
      }
      if (pinned && showPinnedTabsSeparately) {
        if (requiredRowsPinned == 0) {
          requiredRowsPinned = 1;
        }
        myTabs.layout(eachLabel, eachX, eachY, width, myTabs.getHeaderFitSize().height);
        data.bounds.put(eachInfo, eachLabel.getBounds());
      } else {
        if ((!scrollable && eachX + width + hGap > maxX - entryPointMargin && !singleRow) || (showPinnedTabsSeparately)) {
          requiredRowsUnpinned++;
          eachY += myTabs.getHeaderFitSize().height;
          eachX = data.toFitRec.x;
        } else if (requiredRowsUnpinned == 0) {
          requiredRowsUnpinned = 1;
        }
        if (scrollable) {
          if (eachX - myScrollOffset + width + hGap > maxX - entryPointMargin) {
            width = Math.max(0, maxX - eachX + myScrollOffset);
            data.invisible.add(eachInfo);
          }
        }

        myTabs.layout(eachLabel, eachX - myScrollOffset, eachY, width == 1 ? 0 : width, myTabs.getHeaderFitSize().height);
        final Rectangle rectangle = new Rectangle(myTabs.getHeaderFitSize());
        data.bounds.put(eachInfo, eachLabel.getBounds());
        final int intersection = eachLabel.getBounds().intersection(rectangle).width;
        if (scrollable && intersection < eachLabel.getBounds().width) {
          data.invisible.add(eachInfo);
        }
      }
      eachX += width + hGap;
      if (requiredRowsPinned + requiredRowsUnpinned > 1) {
        entryPointMargin = singleRow ? 0 : -data.moreRect.width;
      }
    }
    if (requiredRowsPinned > 0 && requiredRowsUnpinned > 0)
      data.moreRect.y += myTabs.getHeaderFitSize().height /*+ myTabs.getSeparatorWidth()*/;

    if (data.invisible.isEmpty()) {
      data.moreRect.setBounds(0, 0, 0, 0);
    }

    eachY = -1;
    KrTableRow eachTableRow = new KrTableRow(data);

    for (final EditorGroupTabInfo eachInfo : data.myVisibleInfos) {
      KrTabLabel eachLabel = myTabs.getTabLabel(eachInfo);
      if (eachY == -1 || eachY != eachLabel.getY()) {
        if (eachY != -1) {
          eachTableRow = new KrTableRow(data);
        }
        eachY = eachLabel.getY();
        data.table.add(eachTableRow);
      }
      eachTableRow.add(eachInfo, eachLabel.getWidth());
    }

    doScrollToSelectedTab(data);
    clampScrollOffsetToBounds(data);
    return data;
  }

  private void calculateLengths(final KrTablePassInfo data) {
    final boolean compressible = false;
    final boolean showPinnedTabsSeparately = KrTabLayout.showPinnedTabsSeparately();

    final int standardLengthToFit = data.moreRect.x - (data.titleRect.x + data.titleRect.width) - myTabs.getActionsInsets().left;
    if (compressible || showPinnedTabsSeparately) {
      if (showPinnedTabsSeparately) {
        final List<EditorGroupTabInfo> pinned = ContainerUtil.filter(data.myVisibleInfos, info -> false);
        calculateCompressibleLengths(pinned, data, standardLengthToFit);
        final List<EditorGroupTabInfo> unpinned = ContainerUtil.filter(data.myVisibleInfos, info -> true);
        calculateRawLengths(unpinned, data);
        if (KrTableLayout.getTotalLength(unpinned, data) > standardLengthToFit) {
          final int moreWidth = getMoreRectAxisSize();
          final int entryPointsWidth = pinned.isEmpty() ? myTabs.getEntryPointPreferredSize().width : 0;
          data.moreRect.setBounds(data.toFitRec.x + data.toFitRec.width - moreWidth - entryPointsWidth - myTabs.getActionsInsets().right,
            myTabs.getLayoutInsets().top, moreWidth, myTabs.getHeaderFitSize().height);
          calculateRawLengths(unpinned, data);
        }
      } else {
        calculateCompressibleLengths(data.myVisibleInfos, data, standardLengthToFit);
      }
    } else {//both scrollable and multi-row
      calculateRawLengths(data.myVisibleInfos, data);
      if (KrTableLayout.getTotalLength(data.myVisibleInfos, data) > standardLengthToFit) {
        final int moreWidth = getMoreRectAxisSize();
        data.moreRect.setBounds(data.toFitRec.x + data.toFitRec.width - moreWidth, data.toFitRec.y, moreWidth, myTabs.getHeaderFitSize().height);
        calculateRawLengths(data.myVisibleInfos, data);
      }
    }
  }

  private int getMoreRectAxisSize() {
    return myTabs.isSingleRow() ? myTabs.getMoreToolbarPreferredSize().width : 0;
  }

  private static int getTotalLength(@NotNull final List<EditorGroupTabInfo> list, @NotNull final KrTablePassInfo data) {
    int total = 0;
    for (final EditorGroupTabInfo info : list) {
      total += data.lengths.get(info);
    }
    return total;
  }

  private void calculateCompressibleLengths(final List<EditorGroupTabInfo> list, final KrTablePassInfo data, final int toFitLength) {
    if (list.isEmpty()) return;
    int spentLength = 0;
    int lengthEstimation = 0;

    for (final EditorGroupTabInfo tabInfo : list) {
      lengthEstimation += Math.max(KrTabLayout.getMinTabWidth(), myTabs.getInfoToLabel().get(tabInfo).getPreferredSize().width);
    }

    int extraWidth = toFitLength - lengthEstimation;

    for (final Iterator<EditorGroupTabInfo> iterator = list.iterator(); iterator.hasNext(); ) {
      final EditorGroupTabInfo tabInfo = iterator.next();
      KrTabLabel label = myTabs.getInfoToLabel().get(tabInfo);

      int length;
      final int lengthIncrement = label.getPreferredSize().width;
      if (!iterator.hasNext()) {
        length = Math.min(toFitLength - spentLength, lengthIncrement);
      } else if (extraWidth <= 0) {//need compress
        length = (int) (lengthIncrement * (float) toFitLength / lengthEstimation);
      } else {
        length = lengthIncrement;
      }
      length = Math.max(KrTabLayout.getMinTabWidth(), length);
      data.lengths.put(tabInfo, length);
      spentLength += length + myTabs.getTabHGap();
    }
  }

  private void calculateRawLengths(final List<EditorGroupTabInfo> list, final KrTablePassInfo data) {
    for (final EditorGroupTabInfo info : list) {
      final KrTabLabel eachLabel = myTabs.getTabLabel(info);
      final Dimension size =
        eachLabel.isPinned() && KrTabLayout.showPinnedTabsSeparately() ? eachLabel.getNotStrictPreferredSize() : eachLabel.getPreferredSize();
      data.lengths.put(info, Math.max(KrTabLayout.getMinTabWidth(), size.width + myTabs.getTabHGap()));
    }
  }

  public KrLayoutPassInfo layoutTable(final List<EditorGroupTabInfo> visibleInfos) {
    myTabs.resetLayout(true);
    Rectangle unitedTabArea = null;
    final KrTablePassInfo data = computeLayoutTable(visibleInfos);

    final Rectangle rect = new Rectangle(data.moreRect);
    rect.y += myTabs.getBorderThickness();
    myTabs.getMoreToolbar().getComponent().setBounds(rect);

    final ActionToolbar entryPointToolbar = myTabs.getEntryPointToolbar();
    if (entryPointToolbar != null) {
      entryPointToolbar.getComponent().setBounds(data.entryPointRect);
    }
    myTabs.getTitleWrapper().setBounds(data.titleRect);

    final Insets insets = myTabs.getLayoutInsets();
    final int eachY = insets.top;
    for (final EditorGroupTabInfo info : visibleInfos) {
      final Rectangle bounds = data.bounds.get(info);
      if (unitedTabArea == null) {
        unitedTabArea = bounds;
      } else {
        unitedTabArea = unitedTabArea.union(bounds);
      }
    }

    if (myTabs.getSelectedInfo() != null) {
      KrTabsImpl.Toolbar selectedToolbar = myTabs.getInfoToToolbar().get(myTabs.getSelectedInfo());

      int componentY = (unitedTabArea != null ? unitedTabArea.y + unitedTabArea.height : eachY) + (myTabs.isEditorTabs() ? 0 : 2) -
        myTabs.getLayoutInsets().top;
      if (!myTabs.getHorizontalSide() && selectedToolbar != null && !selectedToolbar.isEmpty()) {
        int toolbarWidth = selectedToolbar.getPreferredSize().width;
        int vSeparatorWidth = toolbarWidth > 0 ? myTabs.separatorWidth : 0;
        if (myTabs.isSideComponentBefore()) {
          final Rectangle compRect =
            myTabs.layoutComp(toolbarWidth + vSeparatorWidth, componentY, myTabs.getSelectedInfo().getComponent(), 0, 0);
          myTabs.layout(selectedToolbar, compRect.x - toolbarWidth - vSeparatorWidth, compRect.y, toolbarWidth, compRect.height);
        } else {
          int width = myTabs.getWidth() - toolbarWidth - vSeparatorWidth;
          final Rectangle compRect = myTabs.layoutComp(new Rectangle(0, componentY, width, myTabs.getHeight()),
            myTabs.getSelectedInfo().getComponent(), 0, 0);
          myTabs.layout(selectedToolbar, compRect.x + compRect.width + vSeparatorWidth, compRect.y, toolbarWidth, compRect.height);
        }
      } else {
        myTabs.layoutComp(0, componentY, myTabs.getSelectedInfo().getComponent(), 0, 0);
      }
    }
    if (unitedTabArea != null) {
      data.tabRectangle.setBounds(unitedTabArea);
    }
    lastTableLayout = data;
    return data;
  }

  @Override
  public boolean isTabHidden(@NotNull final EditorGroupTabInfo info) {
    final KrTabLabel label = myTabs.getInfoToLabel().get(info);
    final Rectangle bounds = label.getBounds();
    final int deadzone = JBUI.scale(KrTabLayout.DEADZONE_FOR_DECLARE_TAB_HIDDEN);
    return bounds.x < -deadzone || bounds.width < label.getPreferredSize().width - deadzone;
  }

  @Override
  public boolean isDragOut(@NotNull final KrTabLabel tabLabel, final int deltaX, final int deltaY) {
    if (lastTableLayout == null) {
      return super.isDragOut(tabLabel, deltaX, deltaY);
    }

    Rectangle area = new Rectangle(lastTableLayout.toFitRec.width, tabLabel.getBounds().height);
    for (int i = 0; i < lastTableLayout.myVisibleInfos.size(); i++) {
      area = area.union(myTabs.getInfoToLabel().get(lastTableLayout.myVisibleInfos.get(i)).getBounds());
    }
    return Math.abs(deltaY) > area.height * KrTabLayout.getDragOutMultiplier();
  }

  @Override
  public int getScrollOffset() {
    return myScrollOffset;
  }

  @Override
  public void scroll(final int units) {
    if (!myTabs.isSingleRow()) {
      myScrollOffset = 0;
      return;
    }
    myScrollOffset += units;

    clampScrollOffsetToBounds(lastTableLayout);
  }

  private void clampScrollOffsetToBounds(@Nullable final KrTablePassInfo data) {
    if (data == null) {
      return;
    }
    if (data.requiredLength < data.toFitRec.width) {
      myScrollOffset = 0;
    } else {
      final int entryPointsWidth = data.moreRect.y == data.entryPointRect.y ? data.entryPointRect.width + 1 : 0;
      myScrollOffset = Math.max(0, Math.min(myScrollOffset,
        data.requiredLength - data.toFitRec.width + data.moreRect.width + entryPointsWidth /*+ (1 + myTabs.getIndexOf(myTabs.getSelectedInfo())) * myTabs.getBorderThickness()*/ + data.titleRect.width));
    }
  }

  @Override
  public boolean isWithScrollBar() {
    return myWithScrollBar;
  }

  public int getScrollUnitIncrement() {
    return 10;
  }

  private void doScrollToSelectedTab(final KrTablePassInfo data) {
    if (myTabs.isMouseInsideTabsArea()
      || data == null
      || data.lengths.isEmpty()
      || myTabs.isHideTabs()
      || !KrTabLayout.showPinnedTabsSeparately()) {
      return;
    }

    int offset = -myScrollOffset;
    for (final EditorGroupTabInfo info : data.myVisibleInfos) {
      int length = data.lengths.get(info);
      if (info == myTabs.getSelectedInfo()) {
        if (offset < 0) {
          scroll(offset);
        } else {
          int maxLength = data.moreRect.x;
          if (offset + length > maxLength) {
            // left side should be always visible
            if (length < maxLength) {
              scroll(offset + length - maxLength);
            } else {
              scroll(offset);
            }
          }
        }
        break;
      }
      offset += length;
    }
  }
}
