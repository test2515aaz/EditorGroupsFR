package krasa.editorGroups

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.EDT
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.model.AutoGroup
import krasa.editorGroups.model.EditorGroup

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
      group = textEditor.getUserData(EditorGroupPanel.EDITOR_GROUP)
    }

    if (group != null && group.isValid && group !is AutoGroup) {
      result = group.getPresentableTitle(project, result, state().isShowSize)
    }

    return result
  }

  fun getPresentableNameForUI(project: Project, file: VirtualFile): String {
    val editorTabTitleProviders: List<EditorTabTitleProvider> =
      DumbService.getInstance(project).filterByDumbAwareness<EditorTabTitleProvider>(
        EditorTabTitleProvider.EP_NAME.extensionList
      )

    for (provider in editorTabTitleProviders) {
      if (provider is EditorGroupTabTitleProvider) continue
      var result = provider.getEditorTabTitle(project, file)
      if (result != null) return result
    }

    return file.presentableName
  }
}
