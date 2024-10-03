package krasa.editorGroups.model

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.impl.AbstractUrl
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentIterator
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Pair
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.TreeItem
import javax.swing.Icon

class FavoritesGroup(
  override val title: String,
  validBookmark: List<TreeItem<Pair<AbstractUrl, String>>>,
  project: Project,
  projectFileIndex: ProjectFileIndex
) : EditorGroup() {
  private val files: List<VirtualFile> = add(validBookmark, project, projectFileIndex)

  val ownerFile: VirtualFile
    get() = files[0]

  override val id: String
    get() = ID_PREFIX + title

  override val isValid: Boolean = true

  override fun switchTitle(project: Project): String = title

  override fun icon(): Icon = AllIcons.Toolwindows.ToolWindowFavorites

  override fun invalidate() = Unit

  override fun size(project: Project): Int = files.size

  override fun getLinks(project: Project): List<Link> = files.map { VirtualFileLink(it, project) }

  override fun isOwner(ownerPath: String): Boolean = false

  override fun equals(other: Any?): Boolean = other is FavoritesGroup && other.id == this.id

  override fun needSmartMode(): Boolean = true

  override fun toString(): String = "FavoritesGroup{files=$files, name='$title'}"

  override fun hashCode(): Int {
    var result = title.hashCode()
    result = 31 * result + files.hashCode()
    return result
  }

  private fun add(
    validBookmark: List<TreeItem<Pair<AbstractUrl, String>>>,
    project: Project,
    projectFileIndex: ProjectFileIndex
  ): List<VirtualFile> {
    val files: MutableList<VirtualFile> = ArrayList()
    // fixes ConcurrentModificationException
    val treeItems = ArrayList(validBookmark)

    for (pairTreeItem in treeItems) {
      val data = pairTreeItem.data
      val first = data.first
      val path = first.createPath(project)
      if (path.isNullOrEmpty() || path[0] == null) continue

      val element = path[0]
      if (element is SmartPsiElementPointer<*>) {
        add(projectFileIndex, element.element, files)
      }

      if (element is PsiElement) {
        add(projectFileIndex, element, files)
      }
      add(pairTreeItem.children, project, projectFileIndex)
    }
    return files
  }

  private fun add(projectFileIndex: ProjectFileIndex, element1: PsiElement?, files: MutableList<VirtualFile>) {
    val virtualFile = PsiUtilCore.getVirtualFile(element1) ?: return
    when {
      virtualFile.isDirectory -> iterateContentUnderDirectory(projectFileIndex, virtualFile, files)
      else                    -> files.add(virtualFile)
    }
  }

  private fun iterateContentUnderDirectory(
    projectFileIndex: ProjectFileIndex,
    virtualFile: VirtualFile,
    files: MutableList<VirtualFile>
  ) {
    val contentIterator = ContentIterator { fileOrDir: VirtualFile ->
      when {
        fileOrDir.isDirectory && fileOrDir != virtualFile -> iterateContentUnderDirectory(projectFileIndex, fileOrDir, files)
        !fileOrDir.isDirectory                            -> files.add(fileOrDir)
      }
      true
    }

    projectFileIndex.iterateContentUnderDirectory(virtualFile, contentIterator)
  }

  companion object {
    const val ID_PREFIX: String = "Favorites: "
  }
}
