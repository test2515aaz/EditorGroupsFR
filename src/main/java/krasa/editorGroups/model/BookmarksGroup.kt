package krasa.editorGroups.model

import com.intellij.ide.bookmark.BookmarkGroup
import com.intellij.ide.bookmark.FileBookmark
import com.intellij.ide.bookmark.LineBookmark
import com.intellij.openapi.project.Project
import krasa.editorGroups.icons.EditorGroupsIcons
import javax.swing.Icon

class BookmarksGroup(val bookmarkGroup: BookmarkGroup?, project: Project) : EditorGroup() {
  private val links: MutableList<Link> = mutableListOf()
  override val id: String = "$ID_PREFIX:${bookmarkGroup?.name}"
  override val title: String = "Bookmarks"
  override val isValid: Boolean = true

  init {
    bookmarkGroup?.getBookmarks()?.forEach { bookmark ->
      when (bookmark) {
        is LineBookmark -> {
          val file = bookmark.file
          val icon = EditorGroupsIcons.bookmarks
          val line = bookmark.line
          val desc = bookmarkGroup.getDescription(bookmark)
          links.add(VirtualFileLink(file, icon, line, project).withDescription(desc))
        }

        is FileBookmark -> {
          val file = bookmark.file
          val desc = bookmarkGroup.getDescription(bookmark)
          links.add(VirtualFileLink(file, project).withDescription(desc))
        }
      }
    }
  }

  override fun isSelected(editorGroup: EditorGroup): Boolean = when (editorGroup) {
    is BookmarksGroup -> editorGroup.id == this.id
    else              -> super.isSelected(editorGroup)
  }

  override fun switchTitle(project: Project): String = "$title - [${bookmarkGroup?.name ?: "unnamed"}]"

  override fun icon(): Icon = EditorGroupsIcons.bookmarks

  override fun invalidate() = Unit

  override fun size(project: Project): Int = links.size

  override fun getLinks(project: Project): List<Link> = links

  override fun isOwner(ownerPath: String): Boolean = false

  override fun equals(other: Any?): Boolean = other is BookmarksGroup && other.id == this.id

  override fun needSmartMode(): Boolean = true

  override fun toString(): String = "BookmarksGroup{links=$links, name='$title'}"

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + links.hashCode()
    result = 31 * result + title.hashCode()
    result = 31 * result + isValid.hashCode()
    return result
  }

  companion object {
    const val ID_PREFIX: String = "BOOKMARKS"
  }
}
