package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import krasa.editorGroups.settings.EditorGroupsSettings

class ToggleShowSizeAction : ToggleAction(), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isSelected(e: AnActionEvent): Boolean = EditorGroupsSettings.instance.isShowSize

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    EditorGroupsSettings.instance.isShowSize = state
  }

  companion object {
    const val ID = "krasa.editorGroups.ToggleShowSize"
  }
}
