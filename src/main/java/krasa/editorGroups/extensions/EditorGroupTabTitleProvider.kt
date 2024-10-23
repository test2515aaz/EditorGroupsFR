package krasa.editorGroups.extensions

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.EDT
import krasa.editorGroups.EditorGroupPanel
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.model.AutoGroup
import krasa.editorGroups.model.EditorGroup
import krasa.editorGroups.support.doGetUniqueNameEditorTabTitle

class EditorGroupTabTitleProvider : EditorTabTitleProvider {
  override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
    if (!EDT.isCurrentThreadEdt()) return null

    val presentableNameForUI = getPresentableNameForUI(project, file)
    val textEditor = FileEditorManagerEx.getInstanceEx(project).getSelectedEditor(file)

    return getTitle(project, textEditor, presentableNameForUI)
  }

  private fun getTitle(project: Project, textEditor: FileEditor?, presentableNameForUI: String): String? {
    var result: String = presentableNameForUI
    var group: EditorGroup? = null

    if (textEditor != null) {
      group = textEditor.getUserData(EditorGroupPanel.Companion.EDITOR_GROUP)
    }

    if (group != null && group.isValid && group !is AutoGroup) {
      result = group.getPresentableTitle(project, result, state().isShowSize)
    }

    return result
  }

  /** Simulate the behavior of [com.intellij.openapi.fileEditor.impl.UniqueNameEditorTabTitleProvider]. */
  fun getPresentableNameForUI(project: Project, file: VirtualFile): String =
    doGetUniqueNameEditorTabTitle(project, file) ?: file.presentableName
}
