// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl;

import krasa.editorGroups.tabs2.label.EditorGroupTabInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public abstract class KrLayoutPassInfo {
  public final List<EditorGroupTabInfo> myVisibleInfos;

  @NotNull
  public Rectangle entryPointRect = new Rectangle();
  @NotNull
  public Rectangle moreRect = new Rectangle();
  @NotNull
  public Rectangle titleRect = new Rectangle();

  protected KrLayoutPassInfo(List<EditorGroupTabInfo> visibleInfos) {
    myVisibleInfos = visibleInfos;
  }

  @Nullable
  public static EditorGroupTabInfo getPrevious(List<EditorGroupTabInfo> list, int i) {
    return i > 0 ? list.get(i - 1) : null;
  }

  @Nullable
  public static EditorGroupTabInfo getNext(List<EditorGroupTabInfo> list, int i) {
    return i < list.size() - 1 ? list.get(i + 1) : null;
  }

  public abstract int getRowCount();

  public abstract Rectangle getHeaderRectangle();

  public abstract int getRequiredLength();

  public abstract int getScrollExtent();
}
