package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import krasa.editorGroups.icons.EditorGroupsIcons
import javax.swing.Icon

class FolderGroup(private val folder: VirtualFile?, links: List<Link>) : AutoGroup(links) {
  override var isValid: Boolean = false
    get() = folder != null && field && (DIRECTORY_INSTANCE == folder || folder.isDirectory)
    set(isValid) {
      super.isValid = isValid
      field = isValid
    }

  override fun icon(): Icon = EditorGroupsIcons.folder

  override fun getPresentableTitle(project: Project, presentableNameForUI: String, showSize: Boolean): String = "Current folder"

  override val id: String
    get() = DIRECTORY

  override val title: String
    get() = DIRECTORY

  override fun toString(): String = "FolderGroup{links=${links.size}, stub='$isStub'}"

  companion object {
    val DIRECTORY_INSTANCE: LightVirtualFile = LightVirtualFile("DIRECTORY_INSTANCE")
    val INSTANCE: FolderGroup = FolderGroup(DIRECTORY_INSTANCE, emptyList())
  }
}
