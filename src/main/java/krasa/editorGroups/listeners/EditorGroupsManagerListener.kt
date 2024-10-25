package krasa.editorGroups.listeners

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import krasa.editorGroups.EditorGroupManager
import krasa.editorGroups.EditorGroupPanel
import krasa.editorGroups.services.TabGroupColorizer

class EditorGroupsManagerListener : FileEditorManagerListener {
  /** When a tab is selected, refresh the editor group panel. */
  override fun selectionChanged(event: FileEditorManagerEvent) {
    val project = event.manager.project
    thisLogger().debug("selectionChanged $event")
    val fileEditor = event.newEditor
    if (fileEditor == null) return

    val panel = fileEditor.getUserData(EditorGroupPanel.Companion.EDITOR_PANEL)
    if (panel == null) return

    val instance = EditorGroupManager.Companion.getInstance(project)
    val switchRequest = instance.getAndClearSwitchingRequest(panel.file)

    if (switchRequest != null) {
      val switchingGroup = switchRequest.group
      val scrollOffset = switchRequest.myScrollOffset

      // Refresh panel
      panel.refreshOnSelectionChanged(false, switchingGroup, scrollOffset)
    } else {
      panel.refreshPane(false, null)
    }

    ApplicationManager.getApplication().invokeLater {
      TabGroupColorizer.getInstance(project).refreshTabs(event)
    }
  }

}
