package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import krasa.editorGroups.Splitters

class NextInNewWindowAction : EditorGroupsAction() {
  override fun actionPerformed(anActionEvent: AnActionEvent) {
    getEditorGroupPanel(anActionEvent)?.goToNextTab(true, true, Splitters.NONE)
  }

  companion object {
    const val ID = "krasa.editorGroups.NextInNewWindow"
  }
}
