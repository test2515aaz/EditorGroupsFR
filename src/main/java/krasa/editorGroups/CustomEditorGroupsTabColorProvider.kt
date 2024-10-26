package krasa.editorGroups

import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.model.EditorGroup
import java.awt.Color

class CustomEditorGroupsTabColorProvider : EditorTabColorProvider {
  override fun getEditorTabColor(project: Project, file: VirtualFile): Color? = getBgColor(file)

  @Suppress("UnstableApiUsage")
  override fun getEditorTabForegroundColor(project: Project, file: VirtualFile): ColorKey? {
    val fgColor = getFgColor(file) ?: return null

    return ColorKey.createColorKey(COLOR_KEY, fgColor)
  }

  private fun getFgColor(file: VirtualFile): Color? {
    var group: EditorGroup? = file.getUserData(EditorGroupPanel.EDITOR_GROUP)
    return group?.fgColor
  }

  private fun getBgColor(file: VirtualFile): Color? {
    var group: EditorGroup? = file.getUserData(EditorGroupPanel.EDITOR_GROUP)
    return group?.bgColor
  }

  companion object {
    const val COLOR_KEY = "EditorGroupsTabColor"
  }
}
