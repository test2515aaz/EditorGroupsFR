package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.services.TabGroupColorizer
import krasa.editorGroups.settings.EditorGroupsSettings
import krasa.editorGroups.support.Notifications

class ToggleColorizeGroupsAction : ToggleAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean = EditorGroupsSettings.instance.isColorTabs

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    EditorGroupsSettings.instance.isColorTabs = state
    EditorGroupsSettings.instance.fireChanged()
    Notifications.notifyState(message("colorize.tabs"), state)

    TabGroupColorizer.getInstance(e.project ?: return).refreshTabs(force = true)

  }

  companion object {
    const val ID: String = "krasa.editorGroups.ToggleColorizeGroupsAction"
  }
}
