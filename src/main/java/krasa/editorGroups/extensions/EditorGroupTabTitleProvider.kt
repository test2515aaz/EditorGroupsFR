package krasa.editorGroups.extensions

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.UniqueVFilePathBuilder
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.EDT
import krasa.editorGroups.EditorGroupPanel
import krasa.editorGroups.model.EditorGroup
import java.io.File

class EditorGroupTabTitleProvider : EditorTabTitleProvider {
  override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
    if (EDT.isCurrentThreadEdt()) return null

    val presentableNameForUI = getPresentableNameForUI(project, file)
    val textEditor = FileEditorManagerEx.getInstanceEx(project).getSelectedEditor(file)

    return getTitle(
      project = project,
      textEditor = textEditor,
      presentableNameForUI = presentableNameForUI
    )
  }

  private fun getTitle(project: Project, textEditor: FileEditor?, presentableNameForUI: String): String? {
    var currentTitle: String = presentableNameForUI
    var group: EditorGroup? = null

    if (textEditor != null) {
      group = textEditor.getUserData(EditorGroupPanel.EDITOR_GROUP)
    }

    if (group != null && group.isValid) {
      currentTitle = group.getTabTitle(
        project = project,
        presentableNameForUI = currentTitle,
      )
    }

    return currentTitle
  }

  /** Simulate the behavior of [com.intellij.openapi.fileEditor.impl.UniqueNameEditorTabTitleProvider]. */
  fun getPresentableNameForUI(project: Project, file: VirtualFile): String =
    doGetUniqueNameEditorTabTitle(project = project, file = file) ?: file.presentableName

  private fun getEditorTabText(result: String, hideKnownExtensionInTabs: Boolean): String {
    if (!hideKnownExtensionInTabs) return result

    val withoutExtension = FileUtilRt.getNameWithoutExtension(result)
    if (!withoutExtension.isEmpty() && !withoutExtension.endsWith(File.separator)) return withoutExtension

    return result
  }

  /**
   * Generates a unique editor tab title for the given file in the specified project.
   *
   * @param project The project to which the file belongs.
   * @param file The file for which the unique editor tab title is to be generated.
   * @return A unique editor tab title if applicable, or null if conditions are not met.
   */
  private fun doGetUniqueNameEditorTabTitle(project: Project, file: VirtualFile): String? {
    val uiSettings = UISettings.instanceOrNull
    if (uiSettings == null || !uiSettings.showDirectoryForNonUniqueFilenames || DumbService.isDumb(project)) return null

    // Even though this is a 'tab title provider' it is used also when tabs are not shown, namely for building IDE frame title.
    val uniqueFilePathBuilder = UniqueVFilePathBuilder.getInstance()
    var uniqueName = ReadAction.compute<String?, Throwable> {
      when (uiSettings.editorTabPlacement) {
        uiSettings.editorTabPlacement -> uniqueFilePathBuilder.getUniqueVirtualFilePath(project, file)
        else                          -> uniqueFilePathBuilder.getUniqueVirtualFilePathWithinOpenedFileEditors(project, file)
      }
    }

    uniqueName = getEditorTabText(
      result = uniqueName,
      hideKnownExtensionInTabs = uiSettings.hideKnownExtensionInTabs,
    )

    return uniqueName.takeIf { uniqueName != file.name }
  }

}
