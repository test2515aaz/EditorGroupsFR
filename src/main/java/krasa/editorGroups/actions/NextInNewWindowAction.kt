package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import krasa.editorGroups.support.Splitters

class NextInNewWindowAction : EditorGroupsAction() {
  override fun actionPerformed(anActionEvent: AnActionEvent) {
    getEditorGroupPanel(anActionEvent)?.goToNextTab(
      newTab = true,
      newWindow = true,
      split = Splitters.NONE
    )
  }

  companion object {
    const val ID: String = "krasa.editorGroups.NextInNewWindow"
  }
}
