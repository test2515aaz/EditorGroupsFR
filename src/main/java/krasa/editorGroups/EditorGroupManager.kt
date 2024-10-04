package krasa.editorGroups

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.fileEditor.impl.EditorWindowHolder
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl
import com.intellij.openapi.fileEditor.impl.FileEditorOpenOptions
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl
import com.intellij.openapi.project.DumbService.Companion.isDumb
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.util.ui.UIUtil
import krasa.editorGroups.model.*
import krasa.editorGroups.support.Notifications
import java.awt.Color
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities
import kotlin.concurrent.Volatile

@Suppress("detekt:ArgumentListWrapping")
class EditorGroupManager(private val project: Project) {

  private var cache: IndexCache = IndexCache.getInstance(project)
  private val config: EditorGroupsSettings = EditorGroupsSettings.instance
  private val panelRefresher: PanelRefresher = PanelRefresher.getInstance(project)
  private val ideFocusManager = IdeFocusManager.findInstance()
  private var warningShown = false
  private val externalGroupProvider: ExternalGroupProvider = ExternalGroupProvider.getInstance(project)
  private val autoGroupProvider: AutoGroupProvider = AutoGroupProvider.getInstance(project)
  private val regexGroupProvider: RegexGroupProvider = RegexGroupProvider.getInstance(project)
  private var initialEditorIndex: Key<*>? = null

  @Volatile
  private var switchRequest: SwitchRequest? = null

  @Volatile
  var switching: Boolean = false

  @get:Throws(IndexNotReadyException::class)
  val allIndexedGroups: List<EditorGroupIndexValue>
    // TODO cache it?
    get() {
      val start = System.currentTimeMillis()
      val allGroups = cache.allGroups

      allGroups.sortedWith(COMPARATOR)

      if (LOG.isDebugEnabled) LOG.debug("<getAllGroups ${System.currentTimeMillis() - start}")

      return allGroups
    }

  @Throws(IndexNotReady::class)
  fun getStubGroup(
    project: Project,
    fileEditor: FileEditor,
    displayedGroup: EditorGroup,
    requestedGroup: EditorGroup?,
    currentFile: VirtualFile
  ): EditorGroup {
    val stub = true
    val refresh = false

    if (LOG.isDebugEnabled) LOG.debug("<getStubGroup: fileEditor = [$fileEditor], displayedGroup = [$displayedGroup], requestedGroup = [$requestedGroup], force = [$refresh], stub = [$stub], project = [${project.name}]")

    val start = System.currentTimeMillis()
    var result = EditorGroup.EMPTY

    try {
      val requestedOrDisplayedGroup = requestedGroup ?: displayedGroup
      val currentFilePath = currentFile.path

      // First try to take the requestedGroup or displayedGroup
      if (result.isInvalid) {
        cache.validate(requestedOrDisplayedGroup)

        if (requestedOrDisplayedGroup.isValid
          && (
            requestedOrDisplayedGroup is AutoGroup ||
              requestedOrDisplayedGroup.containsLink(project, currentFile) ||
              requestedOrDisplayedGroup.isOwner(currentFilePath)
            )
        ) {
          result = requestedOrDisplayedGroup
        }
      }

      // Next, try to retrieve the owning group of the current file path
      if (result.isInvalid) {
        result = cache.getOwningOrSingleGroup(currentFilePath)
      }

      // Next try to get the last group
      if (result.isInvalid) {
        result = cache.getLastEditorGroup(
          currentFile = currentFile,
          currentFilePath = currentFilePath,
          includeAutoGroups = true,
          includeFavorites = true,
          stub = stub
        )
      }

      // If nothing is found, try to match by regex if the option is on
      if (result.isInvalid && config.state.isSelectRegexGroup) {
        result = RegexGroupProvider.getInstance(project).findFirstMatchingRegexGroup(currentFile)
      }

      // If nothing is found, try to get the same name group if the option is on
      if (result.isInvalid && config.state.isAutoSameName) {
        result = SameNameGroup.INSTANCE
      }

      // If nothing is found, try to get the folder group if the option is on
      if (result.isInvalid && config.state.isAutoFolders) {
        result = FolderGroup.INSTANCE
      }

      // If the group is empty or is indexing, try other groups
      if (isEmptyAutogroup(project, result) || isIndexingAutoGroup(project, result)) {
        if (LOG.isDebugEnabled) LOG.debug("refreshing result")

        //_refresh
        when (result) {
          is FolderGroup    -> result = autoGroupProvider.getFolderGroup(currentFile)
          is FavoritesGroup -> result = externalGroupProvider.getFavoritesGroup(result.title)
          is BookmarkGroup  -> result = externalGroupProvider.bookmarkGroup
        }
      }

      result.isStub = stub

      if (LOG.isDebugEnabled) {
        LOG.debug("<getStubGroup ${System.currentTimeMillis() - start}ms, EDT=${SwingUtilities.isEventDispatchThread()}, file=${currentFile.name} title='${result.title} stub='${result.isStub}' $result")
      }

      cache.setLast(currentFilePath, result)
    } catch (e: IndexNotReadyException) {
      LOG.debug(e.toString())
      throw IndexNotReady(
        "<getStubGroup project = [${project.name}], fileEditor = [$fileEditor], displayedGroup = [$displayedGroup], requestedGroup = [$requestedGroup], force = [$refresh]",
        e
      )
    } catch (e: Throwable) {
      LOG.warn(e.toString())
      throw e
    }

    return result
  }

  /**
   * Index throws exceptions, nothing we can do about it here, let the caller
   * try it again later.
   */
  @Throws(IndexNotReady::class)
  fun getGroup(
    project: Project,
    fileEditor: FileEditor,
    displayedGroup: EditorGroup,
    requestedGroup: EditorGroup?,
    currentFile: VirtualFile,
    refresh: Boolean,
    stub: Boolean
  ): EditorGroup {
    if (LOG.isDebugEnabled) LOG.debug("<getGroup: fileEditor = [$fileEditor], displayedGroup = [$displayedGroup], requestedGroup = [$requestedGroup], force = [$refresh], stub = [$stub], project = [${project.name}]")

    val start = System.currentTimeMillis()
    var result = EditorGroup.EMPTY

    try {
      val requestedOrDisplayedGroup = requestedGroup ?: displayedGroup
      val currentFilePath = currentFile.path
      val force = refresh && EditorGroupsSettingsState.state().isForceSwitch

      // If force switch is on, force switching
      if (force && requestedOrDisplayedGroup !is FavoritesGroup && requestedOrDisplayedGroup !is BookmarkGroup) {
        // First try to get the owning grouo
        if (result.isInvalid) {
          result = cache.getOwningOrSingleGroup(currentFilePath)
        }

        // Otherwise try the last group
        if (result.isInvalid) {
          result = cache.getLastEditorGroup(currentFile, currentFilePath, false, true, stub)
        }

        // If not found, try with regex
        if (result.isInvalid) {
          result = RegexGroupProvider.getInstance(project).findFirstMatchingRegexGroup(currentFile)
        }
      }

      // Then, try to get the requested or displayed group
      if (result.isInvalid) {
        cache.validate(requestedOrDisplayedGroup)

        if (requestedOrDisplayedGroup.isValid
          && (
            requestedOrDisplayedGroup is AutoGroup
              || requestedOrDisplayedGroup.containsLink(project, currentFile)
              || requestedOrDisplayedGroup.isOwner(currentFilePath)
            )
        ) {
          result = requestedOrDisplayedGroup
        }
      }

      if (!force) {
        // If not found, try to get the owning group
        if (result.isInvalid) {
          result = cache.getOwningOrSingleGroup(currentFilePath)
        }

        // If not found, try to get the last group
        if (result.isInvalid) {
          result = cache.getLastEditorGroup(currentFile, currentFilePath, true, true, stub)
        }
      }

      // Next, try to match by regex, same name or folder
      if (result.isInvalid) {
        if (config.state.isSelectRegexGroup) {
          result = RegexGroupProvider.getInstance(project).findFirstMatchingRegexGroup(currentFile)
        }

        if (result.isInvalid && config.state.isAutoSameName) {
          result = SameNameGroup.INSTANCE
        }

        if (result.isInvalid && config.state.isAutoFolders) {
          result = FolderGroup.INSTANCE
        }
      }

      // If force refresh or the found group is empty or indexing
      if (refresh || isEmptyAutogroup(project, result) || isIndexingAutoGroup(project, result)) {
        if (LOG.isDebugEnabled) LOG.debug("refreshing result")

        //_refresh
        when {
          !stub && result === requestedOrDisplayedGroup && result is EditorGroupIndexValue -> cache.initGroup(result)
          !stub && result is SameNameGroup                                                 -> result =
            autoGroupProvider.getSameNameGroup(currentFile)

          !stub && result is RegexGroup                                                    -> result =
            regexGroupProvider.getRegexGroup(result, project, currentFile)

          result is FolderGroup                                                            -> result =
            autoGroupProvider.getFolderGroup(currentFile)

          result is FavoritesGroup                                                         -> result =
            externalGroupProvider.getFavoritesGroup(result.title)

          result is BookmarkGroup                                                          -> result = externalGroupProvider.bookmarkGroup
        }

        // Last resort, try multigroup
        if (!stub &&
          sameNameGroupIsEmpty(project, result, requestedOrDisplayedGroup) &&
          !(requestedOrDisplayedGroup is SameNameGroup && !requestedOrDisplayedGroup.isStub)
        ) {
          val multiGroup = cache.getMultiGroup(currentFile)
          when {
            multiGroup.isValid                                                                       -> result = multiGroup
            config.state.isAutoFolders && AutoGroup.SAME_FILE_NAME != cache.getLast(currentFilePath) -> result =
              autoGroupProvider.getFolderGroup(currentFile)
          }
        }
      }

      result.isStub = stub

      if (LOG.isDebugEnabled) {
        LOG.debug("<getGroup ${System.currentTimeMillis() - start}ms, EDT=${SwingUtilities.isEventDispatchThread()}, file=${currentFile.name} title='${result.title} stub='${result.isStub}' $result")
      }

      cache.setLast(currentFilePath, result)
    } catch (e: IndexNotReadyException) {
      if (LOG.isDebugEnabled) LOG.debug(e.toString())

      throw IndexNotReady(
        "<getGroup project = [${project.name}], fileEditor = [$fileEditor], displayedGroup = [$displayedGroup], requestedGroup = [$requestedGroup], force = [$refresh]",
        e
      )
    } catch (e: Throwable) {
      LOG.debug(e.toString())
      throw e
    }

    return result
  }

  private fun isIndexingAutoGroup(project: Project, result: EditorGroup): Boolean = when {
    result is AutoGroup && !isDumb(project) -> result.hasIndexing()
    else                                    -> false
  }

  private fun isEmptyAutogroup(project: Project, result: EditorGroup): Boolean = result is AutoGroup && result.size(project) == 0

  private fun sameNameGroupIsEmpty(project: Project, result: EditorGroup, requestedGroup: EditorGroup): Boolean = when {
    result is SameNameGroup && result.size(project) <= 1            -> true
    result === EditorGroup.EMPTY && requestedGroup is SameNameGroup -> true
    else                                                            -> false
  }

  fun switching(switchRequest: SwitchRequest) {
    this.switchRequest = switchRequest
    switching = true

    if (LOG.isDebugEnabled) LOG.debug("switching switching = [$switching], group = [${switchRequest.group}], fileToOpen = [${switchRequest.fileToOpen}], myScrollOffset = [${switchRequest.myScrollOffset}]")
  }

  fun enableSwitching() {
    SwingUtilities.invokeLater {
      ideFocusManager.doWhenFocusSettlesDown {
        if (LOG.isDebugEnabled) LOG.debug("enabling switching")
        this.switching = false
      }
    }
  }

  fun getAndClearSwitchingRequest(file: VirtualFile): SwitchRequest? {
    val switchingFile = when (switchRequest) {
      null -> null
      else -> switchRequest!!.fileToOpen
    }

    if (file == switchingFile) {
      val switchingGroup = switchRequest
      clearSwitchingRequest()
      if (LOG.isDebugEnabled) LOG.debug("<getSwitchingRequest $switchingGroup")
      return switchingGroup
    }

    if (LOG.isDebugEnabled) LOG.debug("<getSwitchingRequest=null, file=[$file], switchingFile=$switchingFile")
    return null
  }

  fun getSwitchingRequest(file: VirtualFile): SwitchRequest? {
    val switchingFile = switchRequest?.fileToOpen

    return if (file == switchingFile) switchRequest else null
  }

  fun isSwitching(): Boolean {
    if (LOG.isDebugEnabled) LOG.debug("isSwitching switchRequest=$switchRequest, switching=$switching")

    return switchRequest != null || switching
  }

  fun getGroups(file: VirtualFile): List<EditorGroup> {
    val groups = cache.findGroups(file)
    groups.sortedWith(COMPARATOR)

    return groups
  }

  fun initCache() = panelRefresher.initCache()

  /**
   * Retrieves the background color associated with the specified
   * VirtualFile.
   *
   * @param file the VirtualFile for which to retrieve the color
   * @return the Color object representing the background color of the
   *    VirtualFile, or null if not found
   */
  fun getBgColor(file: VirtualFile): Color? = cache.getEditorGroupForColor(file).bgColor

  /**
   * Retrieves the foreground color associated with the specified
   * VirtualFile.
   *
   * @param file the VirtualFile for which to retrieve the color
   * @return the Color object representing the foreground color of the
   *    VirtualFile, or null if not found
   */
  fun getFgColor(file: VirtualFile): Color? = cache.getEditorGroupForColor(file).fgColor

  fun open(
    groupPanel: EditorGroupPanel,
    fileToOpen: VirtualFile,
    line: Int?,
    newWindow: Boolean,
    newTab: Boolean,
    split: Splitters
  ): Result? {
    val displayedGroup = groupPanel.displayedGroup
    val tabs = groupPanel.tabs

    val parentOfType = UIUtil.getParentOfType(EditorWindowHolder::class.java, groupPanel)
    var currentWindow: EditorWindow? = null
    if (parentOfType != null) {
      currentWindow = parentOfType.editorWindow
    }

    return open2(
      currentWindow,
      groupPanel.file,
      fileToOpen,
      line,
      displayedGroup,
      newWindow,
      newTab,
      split,
      SwitchRequest(displayedGroup, fileToOpen, tabs.myScrollOffset, tabs.width, line)
    )
  }

  fun open(
    virtualFileByAbsolutePath: VirtualFile,
    window: Boolean,
    tab: Boolean,
    split: Splitters,
    group: EditorGroup,
    current: VirtualFile?
  ): Result? =
    open2(null, current, virtualFileByAbsolutePath, null, group, window, tab, split, SwitchRequest(group, virtualFileByAbsolutePath))

  private fun open2(
    currentWindowParam: EditorWindow?,
    currentFile: VirtualFile?,
    fileToOpen: VirtualFile,
    line: Int?,
    group: EditorGroup,
    newWindow: Boolean,
    newTab: Boolean,
    splitters: Splitters,
    switchRequest: SwitchRequest
  ): Result? {
    if (LOG.isDebugEnabled) LOG.debug("open2 fileToOpen = [$fileToOpen], currentFile = [$currentFile], group = [$group], newWindow = [$newWindow], newTab = [$newTab], splitters = [$splitters], switchingRequest = [$switchRequest]")

    val resultAtomicReference = AtomicReference<Result>()
    switching(switchRequest)

    if (!warningShown && UISettings.getInstance().reuseNotModifiedTabs) {
      Notifications.notifyBugs()
      warningShown = true
    }

    if (initialEditorIndex == null) {
      // TODO it does not work in constructor
      try {
        initialEditorIndex = Key.create<Any>("initial editor index")
      } catch (e: Exception) {
        LOG.error(e)
        initialEditorIndex = Key.create<Any>("initial editor index not found")
      }
    }


    CommandProcessor.getInstance().executeCommand(project, {
      val manager = FileEditorManager.getInstance(project) as FileEditorManagerImpl
      var currentWindow = currentWindowParam ?: manager.currentWindow
      var selectedFile = currentFile ?: currentWindow?.selectedFile

      if (!splitters.isSplit && !newWindow && fileToOpen == selectedFile) {
        val editors = currentWindow!!.manager.getSelectedEditor(fileToOpen)
        val scroll = scroll(line, editors!!)
        if (scroll) {
          resultAtomicReference.set(Result(true))
        }

        if (LOG.isDebugEnabled) {
          LOG.debug("fileToOpen.equals(selectedFile) [fileToOpen=$fileToOpen, selectedFile=$selectedFile, currentFile=$currentFile]")
        }

        resetSwitching()
        return@executeCommand
      }

      fileToOpen.putUserData(EditorGroupPanel.EDITOR_GROUP, group) // for project view colors

      if (initialEditorIndex != null) {
        fileToOpen.putUserData(initialEditorIndex!!, null)
      }

      when {
        splitters.isSplit && currentWindow != null -> {
          if (LOG.isDebugEnabled) LOG.debug("openFileInSplit $fileToOpen")

          val splitter = currentWindow.split(splitters.orientation, true, fileToOpen, true)
          if (splitter == null) {
            if (LOG.isDebugEnabled) LOG.debug("no editors opened.")
            resetSwitching()
          }
        }

        newWindow                                  -> {
          if (LOG.isDebugEnabled) LOG.debug("openFileInNewWindow fileToOpen = $fileToOpen")

          val pair = manager.openFileInNewWindow(fileToOpen)
          scroll(line, *pair.first)

          if (pair.first.size == 0) {
            if (LOG.isDebugEnabled) LOG.debug("no editors opened..")
            resetSwitching()
          }
        }

        else                                       -> {
          val reuseNotModifiedTabs = UISettings.getInstance().reuseNotModifiedTabs
          try {
            if (newTab) UISettings.getInstance().reuseNotModifiedTabs = false

            if (LOG.isDebugEnabled) LOG.debug("openFile $fileToOpen")

            val pair = when (currentWindow) {
              null -> manager.openFile(fileToOpen, null, FileEditorOpenOptions(requestFocus = true, reuseOpen = true))
              else -> manager.openFile(fileToOpen, currentWindow)
            }

            val fileEditors = pair.allEditors

            if (fileEditors.size == 0) {  // directory or some fail
              Notifications.showWarning("Unable to open editor for file " + fileToOpen.name)
              if (LOG.isDebugEnabled) LOG.debug("no editors opened")

              resetSwitching()
              return@executeCommand
            }

            for (fileEditor in fileEditors) {
              if (LOG.isDebugEnabled) LOG.debug("opened fileEditor = $fileEditor")
            }

            scroll(line, *fileEditors.toTypedArray())

            if (reuseNotModifiedTabs) return@executeCommand

            // not sure, but it seems to mess order of tabs less if we do it after opening a new tab
            if (selectedFile != null && !newTab) {
              if (LOG.isDebugEnabled) LOG.debug("closeFile $selectedFile")
              checkNotNull(currentWindow)
              manager.closeFile(selectedFile, currentWindow)
            }
          } finally {
            UISettings.getInstance().reuseNotModifiedTabs = reuseNotModifiedTabs
          }
        }
      }
    }, null, null)


    return resultAtomicReference.get()
  }

  private fun scroll(line: Int?, vararg fileEditors: FileEditor): Boolean {
    if (line == null) return false

    for (fileEditor in fileEditors) {
      if (fileEditor is TextEditorImpl) {
        val editor: Editor = fileEditor.editor
        val position = LogicalPosition(line, 0)
        editor.caretModel.removeSecondaryCarets()
        editor.caretModel.moveToLogicalPosition(position)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        editor.selectionModel.removeSelection()
        IdeFocusManager.getGlobalInstance().requestFocus(editor.contentComponent, true)
        return true
      }
    }
    return false
  }

  fun resetSwitching() {
    clearSwitchingRequest()
    enableSwitching()
  }

  private fun clearSwitchingRequest() {
    if (LOG.isDebugEnabled) LOG.debug("clearSwitchingRequest")
    switchRequest = null
  }

  @Suppress("detekt:UseDataClass")
  class Result(var isScrolledOnly: Boolean)

  companion object {
    private val LOG = Logger.getInstance(EditorGroupManager::class.java)

    val COMPARATOR: Comparator<EditorGroup> = Comparator.comparing { o: EditorGroup -> o.title.lowercase(Locale.getDefault()) }

    @JvmStatic
    fun getInstance(project: Project): EditorGroupManager = project.getService(EditorGroupManager::class.java)
  }
}
