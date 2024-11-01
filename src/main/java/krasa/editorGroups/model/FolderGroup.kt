package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.FileColorManager
import krasa.editorGroups.icons.EditorGroupsIcons
import krasa.editorGroups.messages.EditorGroupsBundle.message
import org.jetbrains.annotations.NonNls
import java.awt.Color
import javax.swing.Icon

class FolderGroup(
  private val folder: VirtualFile?,
  links: List<Link>,
  override val project: Project? = null
) : AutoGroup(links) {
  override var isValid: Boolean = false
    get() = folder != null && (DIRECTORY_INSTANCE == folder || folder.isDirectory)

  override val id: String = DIRECTORY
  override val title: String = DIRECTORY

  override val bgColor: Color?
    get() {
      val fileColorManager = FileColorManager.getInstance(project ?: return null)
      return fileColorManager.getScopeColor(FOLDER_GROUP_SCOPE_ID)
    }

  override fun icon(): Icon = EditorGroupsIcons.folder

  override fun getPresentableTitle(project: Project, presentableNameForUI: String, showSize: Boolean): String = message("current.folder")

  override fun getTabTitle(
    project: Project,
    presentableNameForUI: String,
    showSize: Boolean
  ): String {
    var nameForUI = presentableNameForUI
    val size = size(project)

    return when {
      showSize -> "[$size] $nameForUI"
      else     -> nameForUI
    }
  }

  override fun toString(): String = "FolderGroup{links=${links.size}, stub='$isStub'}"

  class FolderGroupScope : NamedScope(
    FOLDER_GROUP_SCOPE_ID,
    EditorGroupsIcons.folder,
    null
  ) {
    @NonNls
    override fun getDefaultColorName(): String = "Violet"

    override fun getPresentableName(): String = FOLDER_GROUP_SCOPE_NAME
  }

  companion object {
    val DIRECTORY_INSTANCE: LightVirtualFile = LightVirtualFile("DIRECTORY_INSTANCE")
    val INSTANCE: FolderGroup = FolderGroup(DIRECTORY_INSTANCE, emptyList())

    const val FOLDER_GROUP_SCOPE_ID: String = "krasa.editorGroups.model.FolderGroup"
    const val FOLDER_GROUP_SCOPE_NAME: String = "Editor Groups: Current Folder"
    val FOLDER_GROUP_SCOPE: NamedScope = FolderGroupScope()
  }
}
