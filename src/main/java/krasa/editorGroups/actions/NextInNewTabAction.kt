package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import krasa.editorGroups.support.Splitters

class NextInNewTabAction : EditorGroupsAction() {
  override fun actionPerformed(anActionEvent: AnActionEvent) {
    getEditorGroupPanel(anActionEvent)?.goToNextTab(
      newTab = true,
      newWindow = false,
      split = Splitters.NONE
    )
  }

  companion object {
    const val ID: String = "krasa.editorGroups.NextInNewTab"
  }
}
