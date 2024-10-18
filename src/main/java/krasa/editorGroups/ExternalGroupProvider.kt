package krasa.editorGroups

import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.ide.favoritesTreeView.FavoritesManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.model.BookmarksGroup
import krasa.editorGroups.model.EditorGroup
import krasa.editorGroups.model.FavoritesGroup

@Service(Service.Level.PROJECT)
class ExternalGroupProvider(private val project: Project) {
  private val favoritesManager: FavoritesManager = FavoritesManager.getInstance(project)
  private val fileIndex: ProjectFileIndex = ProjectFileIndex.getInstance(project)

  val defaultBookmarkGroup: BookmarksGroup
    get() {
      val defaultGroup = BookmarksManager.getInstance(project)?.defaultGroup
      return BookmarksGroup(defaultGroup, project)
    }

  val bookmarkGroups: List<BookmarksGroup>
    get() {
      val bookmarksManager = BookmarksManager.getInstance(project)
      val allGroups = bookmarksManager?.groups ?: return emptyList()
      val nonDefaultGroups = allGroups.filter { it != bookmarksManager.defaultGroup }

      return nonDefaultGroups
        .map { BookmarksGroup(it, project) }
    }

  val favoritesGroups: List<FavoritesGroup>
    get() {
      return favoritesManager.availableFavoritesListNames
        .asSequence()
        .mapNotNull { name ->
          val favoritesListRootUrls = favoritesManager.getFavoritesListRootUrls(name)
          if (favoritesListRootUrls.isEmpty()) return@mapNotNull null

          FavoritesGroup(
            title = name,
            validBookmark = favoritesListRootUrls,
            project = project,
            projectFileIndex = fileIndex
          ).takeIf { it.size(project) > 0 }
        }
        .toList()

    }

  fun getFavoritesGroup(title: String): EditorGroup {
    val favoritesListRootUrls = favoritesManager.getFavoritesListRootUrls(title)
    if (favoritesListRootUrls.isEmpty()) return EditorGroup.EMPTY

    return FavoritesGroup(title, favoritesListRootUrls, project, fileIndex)
  }

  fun findGroups(currentFile: VirtualFile): List<FavoritesGroup> {
    val start = System.currentTimeMillis()

    val favoritesGroups = this.favoritesGroups
      .filter { it.containsLink(project, currentFile) }

    thisLogger().debug("findGroups ${System.currentTimeMillis() - start}ms")

    return favoritesGroups
  }

  companion object {
    fun getInstance(project: Project): ExternalGroupProvider = project.getService(ExternalGroupProvider::class.java)
  }
}
