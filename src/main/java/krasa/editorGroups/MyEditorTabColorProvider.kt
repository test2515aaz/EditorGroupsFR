package krasa.editorGroups

import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.model.EditorGroup
import java.awt.Color

class MyEditorTabColorProvider : EditorTabColorProvider {
  override fun getEditorTabColor(project: Project, file: VirtualFile): Color? = getBgColor(file)
  // override fun getEditorTabForegroundColor(project: Project, file: VirtualFile): ColorKey? = getFgColor(file)

  private fun getFgColor(file: VirtualFile): Color? {
    var group: EditorGroup? = file.getUserData(EditorGroupPanel.EDITOR_GROUP)
    return group?.fgColor
  }

  private fun getBgColor(file: VirtualFile): Color? {
    var group: EditorGroup? = file.getUserData(EditorGroupPanel.EDITOR_GROUP)
    return group?.bgColor
  }
}
