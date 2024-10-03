package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import krasa.editorGroups.EditorGroupPanel

abstract class EditorGroupsAction : DumbAwareAction() {
  protected fun getEditorGroupPanel(anActionEvent: AnActionEvent): EditorGroupPanel? {
    var panel: EditorGroupPanel? = null
    val data = anActionEvent.getData(PlatformDataKeys.FILE_EDITOR)

    if (data != null) {
      panel = data.getUserData(EditorGroupPanel.EDITOR_PANEL)
    }
    return panel
  }

  companion object {
    private val LOG = Logger.getInstance(EditorGroupsAction::class.java)
  }
}
