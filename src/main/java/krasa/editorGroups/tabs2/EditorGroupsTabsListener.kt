package krasa.editorGroups.tabs2

interface EditorGroupsTabsListener {
  fun selectionChanged(oldSelection: EditorGroupTabInfo?, newSelection: EditorGroupTabInfo?): Unit = Unit
  fun beforeSelectionChanged(oldSelection: EditorGroupTabInfo?, newSelection: EditorGroupTabInfo?): Unit = Unit
  fun tabRemoved(tabToRemove: EditorGroupTabInfo): Unit = Unit
  fun tabsMoved(): Unit = Unit
}
