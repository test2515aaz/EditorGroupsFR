package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.PanelRefresher

class TogglePanelVisibilityAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(e: AnActionEvent) {
    PanelRefresher.getInstance(getEventProject(e)!!).refresh()
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    val state = state()

    when {
      state.isShowPanel -> e.presentation.text = "Hide Panel"
      else              -> e.presentation.text = "Show Panel"
    }
  }

  companion object {
    const val ID = "krasa.editorGroups.TogglePanelVisibility"
  }
}
