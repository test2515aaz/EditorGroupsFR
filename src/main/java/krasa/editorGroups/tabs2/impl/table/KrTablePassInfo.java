// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl.table;

import krasa.editorGroups.tabs2.impl.KrLayoutPassInfo;
import krasa.editorGroups.tabs2.impl.KrTabsImpl;
import krasa.editorGroups.tabs2.label.EditorGroupTabInfo;

import java.awt.*;
import java.util.List;
import java.util.*;

public final class KrTablePassInfo extends KrLayoutPassInfo {
  final List<KrTableRow> table = new ArrayList<>();
  public final Rectangle toFitRec;
  public final Rectangle tabRectangle = new Rectangle();
  final Map<EditorGroupTabInfo, KrTableRow> myInfo2Row = new HashMap<>();
  final KrTabsImpl myTabs;
  public final List<EditorGroupTabInfo> invisible = new ArrayList<>();
  final Map<EditorGroupTabInfo, Integer> lengths = new LinkedHashMap<>();
  final Map<EditorGroupTabInfo, Rectangle> bounds = new HashMap<>();
  int requiredLength = 0;

  KrTablePassInfo(KrTableLayout layout, List<EditorGroupTabInfo> visibleInfos) {
    super(visibleInfos);
    myTabs = layout.myTabs;
    final Insets insets = myTabs.getLayoutInsets();
    toFitRec =
      new Rectangle(insets.left, insets.top, myTabs.getWidth() - insets.left - insets.right, myTabs.getHeight() - insets.top - insets.bottom);
  }

  public boolean isInSelectionRow(final EditorGroupTabInfo tabInfo) {
    final KrTableRow row = myInfo2Row.get(tabInfo);
    final int index = table.indexOf(row);
    return index != -1 && index == table.size() - 1;
  }

  @Override
  public int getRowCount() {
    return table.size();
  }

  @Override
  public Rectangle getHeaderRectangle() {
    return (Rectangle) tabRectangle.clone();
  }

  @Override
  public int getRequiredLength() {
    return requiredLength;
  }

  @Override
  public int getScrollExtent() {
    return !moreRect.isEmpty() ? moreRect.x - toFitRec.x - myTabs.getActionsInsets().left
      : table.size() > 1 || entryPointRect.isEmpty() ? toFitRec.width
      : entryPointRect.x - toFitRec.x - myTabs.getActionsInsets().left;
  }
}
