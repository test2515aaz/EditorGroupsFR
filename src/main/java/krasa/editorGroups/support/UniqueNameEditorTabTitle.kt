package krasa.editorGroups.support

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.UniqueVFilePathBuilder
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

fun getEditorTabText(result: String, separator: String, hideKnownExtensionInTabs: Boolean): String {
  if (hideKnownExtensionInTabs) {
    val withoutExtension = FileUtilRt.getNameWithoutExtension(result)
    if (!withoutExtension.isEmpty() && !withoutExtension.endsWith(separator)) {
      return withoutExtension
    }
  }
  return result
}

internal fun doGetUniqueNameEditorTabTitle(project: Project, file: VirtualFile): String? {
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
    separator = File.separator,
    hideKnownExtensionInTabs = uiSettings.hideKnownExtensionInTabs,
  )

  return uniqueName.takeIf { uniqueName != file.name }
}
