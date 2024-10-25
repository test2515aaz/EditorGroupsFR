package krasa.editorGroups.extensions

import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.EditorGroupManager
import java.awt.Color

class CurrentEditorGroupColorProvider : EditorTabColorProvider {
  override fun getEditorTabColor(
    project: Project,
    file: VirtualFile
  ): Color? {
    val lastGroup = EditorGroupManager.getInstance(project).lastGroup
    if (lastGroup.isStub) return null
    if (!lastGroup.containsLink(project, file.path)) return null

    return lastGroup.bgColor
  }
}
