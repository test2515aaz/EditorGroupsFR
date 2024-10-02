package krasa.editorGroups.model

import com.intellij.icons.AllIcons
import com.intellij.ide.bookmarks.Bookmark
import com.intellij.openapi.project.Project
import javax.swing.Icon

class BookmarkGroup(validBookmarks: List<Bookmark>, project: Project) : EditorGroup() {
  private val links: MutableList<Link> = ArrayList(validBookmarks.size)

  override val id: String = ID
  override val title: String = "Bookmarks"
  override val isValid: Boolean = true

  init {
    validBookmarks.forEach { validBookmark ->
      val file = validBookmark.file
      val icon = validBookmark.icon
      val line = validBookmark.line
      links.add(VirtualFileLink(file, icon, line, project))
    }
  }

  override fun switchTitle(project: Project): String = title

  override fun icon(): Icon = AllIcons.Actions.Checked_selected

  override fun invalidate() = Unit

  override fun size(project: Project): Int = links.size

  override fun getLinks(project: Project): List<Link> = links

  override fun isOwner(ownerPath: String): Boolean = false

  override fun equals(other: Any?): Boolean = (other is BookmarkGroup) && (other.id == this.id)

  override fun toString(): String = "BookmarksGroup{links=$links}"

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + links.hashCode()
    result = 31 * result + title.hashCode()
    result = 31 * result + isValid.hashCode()
    return result
  }

  companion object {
    const val ID: String = "BOOKMARKS"
  }
}
