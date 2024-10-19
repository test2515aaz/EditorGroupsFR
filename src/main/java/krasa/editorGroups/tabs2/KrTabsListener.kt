// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2

interface KrTabsListener {
  fun selectionChanged(oldSelection: KrTabInfo?, newSelection: KrTabInfo?) = Unit
  fun beforeSelectionChanged(oldSelection: KrTabInfo?, newSelection: KrTabInfo?) = Unit
  fun tabRemoved(tabToRemove: KrTabInfo) = Unit
  fun tabsMoved() = Unit
}
