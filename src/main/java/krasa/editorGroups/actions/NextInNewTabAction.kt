package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import krasa.editorGroups.support.Splitters

class NextInNewTabAction : EditorGroupsAction() {
  override fun actionPerformed(anActionEvent: AnActionEvent) {
    getEditorGroupPanel(anActionEvent)?.goToNextTab(true, false, Splitters.NONE)
  }

  companion object {
    const val ID = "krasa.editorGroups.NextInNewTab"
  }
}
