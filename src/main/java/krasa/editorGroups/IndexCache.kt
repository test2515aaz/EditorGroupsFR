package krasa.editorGroups

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import krasa.editorGroups.ProjectComponent.StringPair
import krasa.editorGroups.index.EditorGroupIndex
import krasa.editorGroups.model.*
import krasa.editorGroups.support.FileResolver
import krasa.editorGroups.support.Notifications
import java.util.concurrent.ConcurrentHashMap

class IndexCache(private val project: Project) {
  private val groupsByLinks: MutableMap<String, EditorGroups> = ConcurrentHashMap()
  private val config = EditorGroupsSettings.instance

  private val externalGroupProvider = ExternalGroupProvider.getInstance(project)

  /** Return the EditorGroups of the given path. */
  fun getOwningOrSingleGroup(canonicalPath: String): EditorGroup {
    var result: EditorGroup = EditorGroup.EMPTY
    val editorGroups = groupsByLinks[canonicalPath]

    if (editorGroups != null) {
      val values = editorGroups.all

      when (values.size) {
        1    -> result = values.first() as EditorGroup
        else -> {
          // Try to find all groups that own this path
          val matchedValues = values.filter { it.ownerPath == canonicalPath }
          when {
            matchedValues.size > 1  -> result = EditorGroup.EMPTY // More than one matching group, return empty
            matchedValues.size == 1 -> result = matchedValues[0]
          }
        }
      }
    }

    // init
    result.getLinks(project)
    if (LOG.isDebugEnabled) LOG.debug("<getOwningOrSingleGroup result = $result")

    return result
  }

  /** Get by id. */
  fun getById(id: String): EditorGroup {
    val result = getGroupFromIndexById(id)
    // init
    result.getLinks(project)

    return result
  }

  /** Clear the cache. */
  fun clear() = groupsByLinks.clear()

  fun validate(group: EditorGroup) {
    if (group.isInvalid) return

    if (group is EditorGroupIndexValue) {
      val id = group.id
      try {
        val groupFromIndex = getGroupFromIndexById(id)
        if (groupFromIndex != group) {
          group.invalidate()
          return
        }
      } catch (ignored: ProcessCanceledException) {
      } catch (ignored: IndexNotReadyException) {
      }
    }

    val valid = group.isValid
    if (LOG.isDebugEnabled) {
      LOG.debug("<validate $valid")
    }
  }

  private fun getGroupFromIndexById(id: String): EditorGroup {
    val values = FileBasedIndex.getInstance().getValues(
      EditorGroupIndex.NAME, id, GlobalSearchScope.projectScope(
        project
      )
    )
    if (values.size > 1) {
      Notifications.duplicateId(project, id, values)
    }
    val editorGroup = if (values.isEmpty()) EditorGroup.EMPTY else values[0]
    if (LOG.isDebugEnabled) {
      LOG.debug("<getGroupFromIndexById $editorGroup")
    }
    return editorGroup
  }

  private fun add(group: EditorGroupIndexValue, path: String) {
    val editorGroups = groupsByLinks[path]
    if (editorGroups == null) {
      val value = EditorGroups()
      value.add(group)
      groupsByLinks[path] = value
    } else {
      editorGroups.add(group)
    }
  }

  fun onIndexingDone(ownerPath: String, group: EditorGroupIndexValue): EditorGroupIndexValue {
    val editorGroups = groupsByLinks[ownerPath]
    if (editorGroups != null) {
      val editorGroup = editorGroups.getById(group.id)
      if (group == editorGroup) {
        return editorGroup as EditorGroupIndexValue
      }
    }


    initGroup(group)
    return group
  }

  @Throws(ProcessCanceledException::class)
  fun initGroup(group: EditorGroupIndexValue) {
    if (LOG.isDebugEnabled) LOG.debug("initGroup = [$group]")
    if (!EditorGroup.exists(group)) {
      return
    }

    add(group, group.ownerPath)

    val links = FileResolver.resolveLinks(group, project)
    group.setLinks(links)

    for (link in links) {
      add(group, link.path)
    }
  }

  fun getLastEditorGroup(
    currentFile: VirtualFile,
    currentFilePath: String,
    includeAutoGroups: Boolean,
    includeFavorites: Boolean,
    stub: Boolean
  ): EditorGroup {
    var result = EditorGroup.EMPTY
    if (!config.state.isRememberLastGroup) {
      LOG.debug("<getLastEditorGroup $result (isRememberLastGroup=false)")
      return result
    }

    val groups = groupsByLinks[currentFilePath]
    val config = config.state

    if (groups != null) {
      val last = groups.last
      if (LOG.isDebugEnabled) LOG.debug("last = $last")
      if (last != null && this.config.state.isRememberLastGroup) {
        when {
          includeAutoGroups && config.isAutoSameName && AutoGroup.SAME_FILE_NAME == last -> result = SameNameGroup.INSTANCE

          includeAutoGroups && config.isAutoFolders && AutoGroup.DIRECTORY == last       -> result = FolderGroup.INSTANCE

          HidePanelGroup.ID == last                                                      -> result = AutoGroup.HIDE_GROUP_INSTANCE

          includeFavorites && last.startsWith(FavoritesGroup.ID_PREFIX)                  -> {
            val favoritesGroup = externalGroupProvider.getFavoritesGroup(last.substring(FavoritesGroup.ID_PREFIX.length))
            if (favoritesGroup.containsLink(project, currentFile)) {
              result = favoritesGroup
            }
          }

          includeFavorites && last.startsWith(RegexGroup.ID_PREFIX)                      ->
            result = RegexGroupProvider.getInstance(project).findRegexGroup_stub(currentFile, last.substring(RegexGroup.ID_PREFIX.length))

          includeFavorites && last == BookmarkGroup.ID                                   -> result = externalGroupProvider.bookmarkGroup

          stub                                                                           -> result = StubGroup()

          else                                                                           -> {
            val lastGroup = getById(last)
            if (lastGroup.containsLink(project, currentFile) || lastGroup.isOwner(currentFilePath)) {
              result = lastGroup
            }
          }
        }
      }

      if (result.isInvalid) {
        result = getMultiGroup(currentFile)
      }
    }
    if (LOG.isDebugEnabled) {
      LOG.debug("<getLastEditorGroup $result")
    }
    return result
  }

  fun findGroups(currentFile: VirtualFile): List<EditorGroup> {
    val result: MutableList<EditorGroup> = ArrayList()
    val editorGroups = groupsByLinks[currentFile.path]
    if (editorGroups != null) {
      editorGroups.validate(this)
      result.addAll(editorGroups.all)
    }
    result.addAll(externalGroupProvider.findGroups(currentFile))

    if (LOG.isDebugEnabled) {
      LOG.debug("<findGroups $result")
    }
    return result
  }

  fun getMultiGroup(currentFile: VirtualFile): EditorGroup {
    var result = EditorGroup.EMPTY

    val favouriteGroups = findGroups(currentFile)
    if (favouriteGroups.size == 1) {
      result = favouriteGroups[0]
    } else if (favouriteGroups.size > 1) {
      result = EditorGroups(favouriteGroups)
    }
    if (LOG.isDebugEnabled) {
      LOG.debug("<getSlaveGroup $result")
    }
    return result
  }

  /** called very often! */
  fun getEditorGroupForColor(currentFile: VirtualFile): EditorGroup {
    var result = EditorGroup.EMPTY
    val groups = groupsByLinks[currentFile.path]

    if (groups != null) {
      val last = groups.last
      if (last != null && config.state.isRememberLastGroup) {
        val editorGroups = groupsByLinks[last]
        if (editorGroups != null) {
          val lastGroup = editorGroups.getById(last)
          if (lastGroup.isValid && lastGroup.containsLink(project, currentFile)) {
            result = lastGroup
          }
        }
      }

      if (result.isInvalid) {
        result = groups.ownerOrLast(currentFile.path)
      }
    }
    return result
  }

  fun setLast(currentFile: String, result: EditorGroup) {
    if (!result.isValid || result.isStub) {
      return
    }

    var editorGroups = groupsByLinks[currentFile]
    if (editorGroups == null) {
      editorGroups = EditorGroups()
      editorGroups.add(result)
      groupsByLinks[currentFile] = editorGroups
    }
    editorGroups.last = result.id
  }

  @get:Throws(IndexNotReadyException::class)
  val allGroups: List<EditorGroupIndexValue>
    get() {
      val instance = FileBasedIndex.getInstance()
      val allKeys = instance.getAllKeys(EditorGroupIndex.NAME, project)
      val scope = GlobalSearchScope.projectScope(project)

      val all: MutableList<EditorGroupIndexValue> = ArrayList(allKeys.size)

      for (allKey in allKeys) {
        instance.processValues(EditorGroupIndex.NAME, allKey, null, { file: VirtualFile?, value: EditorGroupIndexValue ->
          all.add(value)
          true
        }, scope)
      }
      return all
    }

  fun getLast(currentFilePath: String): String? {
    val groups = groupsByLinks[currentFilePath]

    if (groups != null) {
      return groups.last
    }
    return null
  }

  fun loadState(state: ProjectComponent.State) {
    for (stringStringPair in state.lastGroup) {
      val editorGroups = EditorGroups()
      groupsByLinks[stringStringPair.key] = editorGroups
      editorGroups.last = stringStringPair.value
    }
  }

  val state: ProjectComponent.State
    get() {
      val state = ProjectComponent.State()
      val entries: Set<Map.Entry<String, EditorGroups>> = groupsByLinks.entries
      val autoSameName = config.state.isAutoSameName
      val autoFolders = config.state.isAutoFolders

      for ((key, value) in entries) {
        val last = value.last
        if (last == null) {
          continue
        } else if (autoSameName && AutoGroup.SAME_FILE_NAME == last) {
          continue
        } else if (autoFolders && AutoGroup.DIRECTORY == last) {
          continue
        }
        if (state.lastGroup.size > MAX_HISTORY_SIZE) {  // TODO config
          break
        }
        state.lastGroup.add(StringPair(key, last))
      }
      return state
    }

  fun removeGroup(ownerPath: String?) {
    var group: EditorGroup? = null
    for ((_, value) in groupsByLinks) {
      for (editorGroup in value.all) {
        group = editorGroup
        if (group!!.isOwner(ownerPath)) {
          if (LOG.isDebugEnabled) LOG.debug("removeFromIndex invalidating$group")
          group.invalidate()
        }
      }
    }

    if (group != null) {
      val links = group.getLinks(project)
      for (link in links) {
        val editorGroups = groupsByLinks[link.path]
        editorGroups?.remove(group)
      }
    }

    PanelRefresher.getInstance(project).refresh(ownerPath)
  }

  fun getCached(userData: EditorGroup): EditorGroup {
    val editorGroups = groupsByLinks[userData.ownerPath]
    if (editorGroups != null) {
      return editorGroups.getById(userData.id)
    }
    return EditorGroup.EMPTY
  }

  companion object {
    private val LOG = Logger.getInstance(IndexCache::class.java)
    const val MAX_HISTORY_SIZE: Int = 1000

    @JvmStatic
    fun getInstance(project: Project): IndexCache = project.getService(IndexCache::class.java)
  }
}
