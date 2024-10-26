package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import krasa.editorGroups.settings.EditorGroupsSettings
import krasa.editorGroups.support.Notifications

class ToggleAutoSameNameGroupsAction : ToggleAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean = EditorGroupsSettings.instance.isAutoSameName

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    EditorGroupsSettings.instance.isAutoSameName = state
    EditorGroupsSettings.instance.fireChanged()
    Notifications.notifySimple("Auto Same Name Group ${if (state) "enabled" else "disabled"}")
  }

  companion object {
    const val ID = "krasa.editorGroups.ToggleAutoSameNameGroups"
  }
}
