package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import krasa.editorGroups.Splitters

class PreviousInNewTabAction : EditorGroupsAction() {
  override fun actionPerformed(anActionEvent: AnActionEvent) {
    getEditorGroupPanel(anActionEvent)?.previous(true, false, Splitters.NONE)
  }

  companion object {
    const val ID = "krasa.editorGroups.PreviousInNewTab"
  }
}
