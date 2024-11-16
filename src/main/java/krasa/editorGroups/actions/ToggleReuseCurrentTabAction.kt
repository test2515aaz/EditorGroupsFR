package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.settings.EditorGroupsSettings
import krasa.editorGroups.support.Notifications

class ToggleReuseCurrentTabAction : ToggleAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean = EditorGroupsSettings.instance.reuseCurrentTab

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    EditorGroupsSettings.instance.reuseCurrentTab = state
    EditorGroupsSettings.instance.fireChanged()
    Notifications.notifyState(message("reuse.current.tab"), state)
  }

  companion object {
    const val ID: String = "krasa.editorGroups.ToggleReuseCurrentTabAction"
  }
}
