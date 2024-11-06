package krasa.editorGroups.tabs2

interface EditorGroupsTabsListener {
  fun selectionChanged(oldSelection: KrTabInfo?, newSelection: KrTabInfo?): Unit = Unit
  fun beforeSelectionChanged(oldSelection: KrTabInfo?, newSelection: KrTabInfo?): Unit = Unit
  fun tabRemoved(tabToRemove: KrTabInfo): Unit = Unit
  fun tabsMoved(): Unit = Unit
}
