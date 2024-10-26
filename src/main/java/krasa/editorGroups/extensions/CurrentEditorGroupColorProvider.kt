package krasa.editorGroups.extensions

import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.EditorGroupManager
import krasa.editorGroups.settings.EditorGroupsSettings
import java.awt.Color

class CurrentEditorGroupColorProvider : EditorTabColorProvider {
  override fun getEditorTabColor(
    project: Project,
    file: VirtualFile
  ): Color? {
    val lastGroup = EditorGroupManager.getInstance(project).lastGroup
    return when {
      lastGroup.isStub                            -> null
      !EditorGroupsSettings.instance.isColorTabs  -> null
      !lastGroup.containsLink(project, file.path) -> null
      else                                        -> lastGroup.bgColor
    }
  }
}
