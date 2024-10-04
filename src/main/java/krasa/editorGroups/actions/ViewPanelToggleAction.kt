package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.PanelRefresher

class ViewPanelToggleAction : ToggleAction("Editor Groups Panel"), DumbAware {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun isSelected(event: AnActionEvent): Boolean = state().isShowPanel

  override fun setSelected(event: AnActionEvent, state: Boolean) {
    val editorGroupsSettingsState = state()
    editorGroupsSettingsState.isShowPanel = !editorGroupsSettingsState.isShowPanel

    PanelRefresher.getInstance(event.project!!).refresh()
  }

  companion object {
    const val ID = "krasa.editorGroups.ViewPanelToggleAction"
  }
}
