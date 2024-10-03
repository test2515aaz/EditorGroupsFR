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

  val state: ProjectComponent.State
    get() {
      val state = ProjectComponent.State()
      val entries: Set<Map.Entry<String, EditorGroups>> = groupsByLinks.entries
      val autoSameName = config.state.isAutoSameName
      val autoFolders = config.state.isAutoFolders

      for ((key, value) in entries) {
        val last = value.last
        when {
          last == null                                     -> continue
          autoSameName && last == AutoGroup.SAME_FILE_NAME -> continue
          autoFolders && last == AutoGroup.DIRECTORY       -> continue
        }

        if (state.lastGroup.size > MAX_HISTORY_SIZE) break

        state.lastGroup.add(StringPair(key, last))
      }
      return state
    }

  /** Return the EditorGroups of the given path. */
  fun getOwningOrSingleGroup(canonicalPath: String): EditorGroup {
    var result: EditorGroup = EditorGroup.EMPTY
    val editorGroups = groupsByLinks[canonicalPath]

    if (editorGroups != null) {
      val values = editorGroups.all

      when (values.size) {
        1    -> result = values.first()
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

  /**
   * Validates the given [group]. This method checks if the given [group] is
   * valid and performs additional validation based on the type of group.
   *
   * @param group The [EditorGroup] to validate.
   */
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

    if (LOG.isDebugEnabled) LOG.debug("<validate $valid")
  }

  /**
   * Retrieves the EditorGroup from the index based on the provided id.
   *
   * @param id The id of the EditorGroup.
   * @return The EditorGroup with the matching id.
   */
  private fun getGroupFromIndexById(id: String): EditorGroup {
    // Fetch the values from the index
    val values = FileBasedIndex.getInstance().getValues(
      EditorGroupIndex.NAME,
      id,
      GlobalSearchScope.projectScope(project)
    )

    if (values.size > 1) {
      Notifications.duplicateId(project, id, values)
    }

    val editorGroup = if (values.isEmpty()) EditorGroup.EMPTY else values[0]

    if (LOG.isDebugEnabled) LOG.debug("<getGroupFromIndexById $editorGroup")

    return editorGroup
  }

  /**
   * Adds an [EditorGroupIndexValue] to the [groupsByLinks] map.
   *
   * @param group The [EditorGroupIndexValue] to add.
   * @param path The path to associate with the [EditorGroupIndexValue].
   */
  private fun add(group: EditorGroupIndexValue, path: String) {
    val editorGroups = groupsByLinks[path]

    if (editorGroups == null) {
      val groups = EditorGroups()
      groups.add(group)
      groupsByLinks[path] = groups
    } else {
      editorGroups.add(group)
    }
  }

  /**
   * Handles the completion of indexing for a specific editor group.
   *
   * @param ownerPath The path of the owner of the group.
   * @param group The editor group that has finished indexing.
   * @return The updated editor group.
   */
  fun onIndexingDone(ownerPath: String, group: EditorGroupIndexValue): EditorGroupIndexValue {
    val updatedGroup = groupsByLinks[ownerPath]?.getById(group.id).takeIf { it == group } as? EditorGroupIndexValue

    return updatedGroup ?: run {
      initGroup(group)
      group
    }
  }

  /**
   * Initializes the given group by adding it to the groupsByLinks map,
   * setting its links, and adding its related paths.
   *
   * @param group The EditorGroupIndexValue to initialize.
   * @throws ProcessCanceledException if the process is canceled.
   */
  @Throws(ProcessCanceledException::class)
  fun initGroup(group: EditorGroupIndexValue) {
    if (LOG.isDebugEnabled) LOG.debug("<initGroup = [$group]")
    if (!EditorGroup.exists(group)) return

    add(group, group.ownerPath)

    val links = FileResolver.resolveLinks(group, project)
    group.setLinks(links)

    links.forEach { add(group, it.path) }
  }

  /**
   * Retrieves the last editor group based on the provided parameters.
   *
   * @param currentFile The VirtualFile currently open in the editor.
   * @param currentFilePath The path of the current file.
   * @param includeAutoGroups Whether to include auto groups in the result.
   * @param includeFavorites Whether to include favorite groups in the
   *    result.
   * @param stub Whether to return a stub group if no other valid group is
   *    found.
   * @return The last EditorGroup based on the provided parameters.
   */
  fun getLastEditorGroup(
    currentFile: VirtualFile,
    currentFilePath: String,
    includeAutoGroups: Boolean,
    includeFavorites: Boolean,
    stub: Boolean
  ): EditorGroup {
    var result = EditorGroup.EMPTY
    if (!config.state.isRememberLastGroup) {
      if (LOG.isDebugEnabled) LOG.debug("<getLastEditorGroup $result (isRememberLastGroup=false)")
      return result
    }

    val groups = groupsByLinks[currentFilePath]
    if (groups != null) {
      val last = groups.last
      if (LOG.isDebugEnabled) LOG.debug("last = $last")

      if (last != null && config.state.isRememberLastGroup) {
        when {
          includeAutoGroups && config.state.isAutoSameName && last == AutoGroup.SAME_FILE_NAME -> result = SameNameGroup.INSTANCE
          includeAutoGroups && config.state.isAutoFolders && last == AutoGroup.DIRECTORY       -> result = FolderGroup.INSTANCE
          last == HidePanelGroup.ID                                                            -> result = HidePanelGroup.INSTANCE

          includeFavorites && last.startsWith(FavoritesGroup.ID_PREFIX)                        -> {
            val favoritesGroup = externalGroupProvider.getFavoritesGroup(last.substring(FavoritesGroup.ID_PREFIX.length))
            if (favoritesGroup.containsLink(project, currentFile)) result = favoritesGroup
          }

          includeFavorites && last.startsWith(RegexGroup.ID_PREFIX)                            -> {
            val groupName = last.substring(RegexGroup.ID_PREFIX.length)
            result = RegexGroupProvider.getInstance(project).findRegexGroup(currentFile, groupName)
          }

          includeFavorites && last == BookmarkGroup.ID                                         -> result =
            externalGroupProvider.bookmarkGroup

          stub                                                                                 -> result = StubGroup()

          else                                                                                 -> {
            val lastGroup = getById(last)
            if (lastGroup.containsLink(project, currentFile) || lastGroup.isOwner(currentFilePath)) result = lastGroup
          }
        }
      }

      if (result.isInvalid) {
        result = getMultiGroup(currentFile)
      }
    }

    if (LOG.isDebugEnabled) LOG.debug("<getLastEditorGroup $result")

    return result
  }

  /**
   * Finds all EditorGroups associated with the given currentFile.
   *
   * @param currentFile The [VirtualFile] for which [EditorGroups] are to be
   *    found.
   * @return A List of [EditorGroup] objects found for the currentFile.
   */
  fun findGroups(currentFile: VirtualFile): List<EditorGroup> {
    val result: MutableList<EditorGroup> = ArrayList()

    val editorGroups = groupsByLinks[currentFile.path]
    if (editorGroups != null) {
      editorGroups.validate(this)
      result.addAll(editorGroups.all)
    }

    result.addAll(externalGroupProvider.findGroups(currentFile))

    if (LOG.isDebugEnabled) LOG.debug("<findGroups $result")
    return result
  }

  /**
   * Retrieves the EditorGroup associated with the given VirtualFile.
   *
   * @param currentFile The [VirtualFile] for which the [EditorGroup] is to
   *    be retrieved.
   * @return The [EditorGroup] corresponding to the currentFile. Returns an
   *    empty [EditorGroup] if no groups are found.
   */
  fun getMultiGroup(currentFile: VirtualFile): EditorGroup {
    var result = EditorGroup.EMPTY
    val favouriteGroups = findGroups(currentFile)

    when {
      favouriteGroups.size == 1 -> result = favouriteGroups[0]
      favouriteGroups.size > 1  -> result = EditorGroups(favouriteGroups)
    }

    if (LOG.isDebugEnabled) LOG.debug("<getMultiGroup $result")

    return result
  }

  /** called very often! */
  fun getEditorGroupForColor(currentFile: VirtualFile): EditorGroup {
    var result = EditorGroup.EMPTY
    val groups = groupsByLinks[currentFile.path]

    if (groups == null) return result

    val last = groups.last
    if (last != null && config.state.isRememberLastGroup) {
      val lastEditorGroups = groupsByLinks[last]

      if (lastEditorGroups != null) {
        val lastGroup = lastEditorGroups.getById(last)
        if (lastGroup.isValid && lastGroup.containsLink(project, currentFile)) {
          result = lastGroup
        }
      }
    }

    if (result.isInvalid) {
      result = groups.ownerOrLast(currentFile.path)
    }

    return result
  }

  fun setLast(currentFile: String, result: EditorGroup) {
    if (!result.isValid || result.isStub) return

    var editorGroups = groupsByLinks[currentFile]
    if (editorGroups == null) {
      editorGroups = EditorGroups()
      editorGroups.add(result)
      groupsByLinks[currentFile] = editorGroups
    }

    editorGroups.last = result.id
  }

  fun getLast(currentFilePath: String): String? = groupsByLinks[currentFilePath]?.last

  fun loadState(state: ProjectComponent.State) {
    for (stringStringPair in state.lastGroup) {
      val editorGroups = EditorGroups()
      groupsByLinks[stringStringPair.key] = editorGroups
      editorGroups.last = stringStringPair.value
    }
  }

  fun removeGroup(ownerPath: String) {
    for ((_, value) in groupsByLinks) {
      value.all.forEach { editorGroup ->
        if (editorGroup.isOwner(ownerPath)) {
          if (LOG.isDebugEnabled) LOG.debug("removeFromIndex invalidating$editorGroup")

          editorGroup.invalidate()

          val links = editorGroup.getLinks(project)
          for (link in links) {
            val editorGroups = groupsByLinks[link.path]
            editorGroups?.remove(editorGroup)
          }
        }
      }
    }

    PanelRefresher.getInstance(project).refresh(ownerPath)
  }

  fun getCached(userData: EditorGroup): EditorGroup = groupsByLinks[userData.ownerPath]?.getById(userData.id) ?: EditorGroup.EMPTY

  companion object {
    private val LOG = Logger.getInstance(IndexCache::class.java)

    const val MAX_HISTORY_SIZE: Int = 1000

    @JvmStatic
    fun getInstance(project: Project): IndexCache = project.getService(IndexCache::class.java)
  }
}
