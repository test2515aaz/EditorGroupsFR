package krasa.editorGroups.tabs2

import krasa.editorGroups.tabs2.label.EditorGroupTabInfo

interface EditorGroupsTabsListener {
  fun selectionChanged(oldSelection: EditorGroupTabInfo?, newSelection: EditorGroupTabInfo?): Unit = Unit
  fun beforeSelectionChanged(oldSelection: EditorGroupTabInfo?, newSelection: EditorGroupTabInfo?): Unit = Unit
  fun tabRemoved(tabToRemove: EditorGroupTabInfo): Unit = Unit
  fun tabsMoved(): Unit = Unit
}
