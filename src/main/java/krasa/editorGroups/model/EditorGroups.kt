package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import krasa.editorGroups.IndexCache
import krasa.editorGroups.icons.EditorGroupsIcons
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon

class EditorGroups : EditorGroup, GroupsHolder {

  private val groupsMap: MutableMap<String, EditorGroup?> = ConcurrentHashMap()
  var last: String? = null

  val all: Collection<EditorGroup?>
    get() = groupsMap.values

  override val id: String
    get() = ID

  override val title: String = ""

  override val isValid: Boolean = true

  override val groups: Collection<EditorGroup?>
    get() = groupsMap.values

  constructor()

  /** no filtering by type */
  constructor(editorGroups: List<EditorGroup>) {
    editorGroups.forEach { addGroup(it) }
  }

  /** no filtering by type */
  constructor(result: EditorGroup, editorGroups: List<EditorGroup>) {
    addGroup(result)
    for (group in editorGroups) {
      addGroup(group)
    }
  }

  private fun addGroup(group: EditorGroup) {
    if (group is GroupsHolder) {
      val groups = (group as GroupsHolder).groups
      for (editorGroup in groups!!) {
        groupsMap[editorGroup!!.id] = editorGroup
      }
    } else {
      groupsMap[group.id] = group
    }
  }

  fun add(editorGroup: EditorGroup) {
    when (editorGroup) {
      is AutoGroup      -> return

      is EditorGroups   -> return

      is FavoritesGroup -> return

      is BookmarkGroup  -> return

      else              -> addGroup(editorGroup)
    }
  }

  fun remove(editorGroup: EditorGroup) {
    groupsMap.remove(editorGroup.id)
  }

  override fun icon(): Icon? {
    return EditorGroupsIcons.groupBy
  }

  override fun invalidate() {
  }

  override fun size(project: Project): Int {
    return groupsMap.size
  }

  override fun getLinks(project: Project): List<Link> {
    return emptyList()
  }

  override fun isOwner(ownerPath: String): Boolean {
    return false
  }

  fun validate(indexCache: IndexCache) {
    var iterator = groupsMap.values.iterator()
    while (iterator.hasNext()) {
      val next = iterator.next()
      indexCache.validate(next!!)
    }
    // IndexCache.validate accesses index which can triggers indexing which updates this map,
    // removing it in one cycle would remove a key with new validvalue
    iterator = groupsMap.values.iterator()
    while (iterator.hasNext()) {
      val next = iterator.next()
      if (next!!.isInvalid) {
        iterator.remove()
      }
    }
  }

  fun first(): EditorGroup? {
    for (editorGroup in groupsMap.values) {
      return editorGroup
    }
    return EMPTY
  }

  fun getById(id: String): EditorGroup {
    var editorGroup = groupsMap[id]
    if (editorGroup == null) {
      editorGroup = EMPTY
    }
    return editorGroup
  }

  fun ownerOrLast(currentFilePath: String?): EditorGroup? {
    val iterator: Iterator<EditorGroup?> = groupsMap.values.iterator()
    var group: EditorGroup? = EMPTY
    while (iterator.hasNext()) {
      group = iterator.next()
      if (group!!.isOwner(currentFilePath!!)) {
        break
      }
    }
    return group
  }

  override fun toString(): String = "EditorGroups{map=$groupsMap, last='$last'}"

  companion object {
    const val ID: String = "EDITOR_GROUPS"
  }
}
