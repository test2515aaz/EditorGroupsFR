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
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl
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

  @Throws(IndexNotReady::class)
  fun getStubGroup(
    project: Project,
    fileEditor: FileEditor,
    displayedGroup: EditorGroup,
    requestedGroup: EditorGroup?,
    currentFile: VirtualFile
  ): EditorGroup {
    var requestedGroup = requestedGroup
    val stub = true
    val refresh = false
    if (LOG.isDebugEnabled) LOG.debug(">getStubGroup: fileEditor = [" + fileEditor + "], displayedGroup = [" + displayedGroup + "], requestedGroup = [" + requestedGroup + "], force = [" + refresh + "], stub = [" + stub + "]" + ", project = [" + project.name + "]")

    val start = System.currentTimeMillis()

    var result = EditorGroup.EMPTY
    try {
      if (requestedGroup == null) {
        requestedGroup = displayedGroup
      }

      val currentFilePath = currentFile.path


      if (result.isInvalid) {
        cache.validate(requestedGroup)
        if (requestedGroup.isValid
          && (requestedGroup is AutoGroup || requestedGroup.containsLink(project, currentFile) || requestedGroup.isOwner(currentFilePath))
        ) {
          result = requestedGroup
        }
      }

      if (result.isInvalid) {
        result = cache.getOwningOrSingleGroup(currentFilePath)
      }

      if (result.isInvalid) {
        result = cache.getLastEditorGroup(currentFile, currentFilePath, true, true, stub)
      }

      if (result.isInvalid) {
        if (config.state.isSelectRegexGroup) {
          result = RegexGroupProvider.getInstance(project).findFirstMatchingRegexGroup_stub(currentFile)
        }
        if (result.isInvalid && config.state.isAutoSameName) {
          result = SameNameGroup.INSTANCE
        } else if (result.isInvalid && config.state.isAutoFolders) {
          result = FolderGroup.INSTANCE
        }
      }

      if (isEmptyAutogroup(project, result) || isIndexingAutoGroup(project, result)) {
        if (LOG.isDebugEnabled) {
          LOG.debug("refreshing result")
        }
        //_refresh
        if (result is FolderGroup) {
          result = autoGroupProvider.getFolderGroup(currentFile)
        } else if (result is FavoritesGroup) {
          result = externalGroupProvider.getFavoritesGroup(result.title)
        } else if (result is BookmarkGroup) {
          result = externalGroupProvider.bookmarkGroup
        }
      }

      result.isStub = stub

      if (LOG.isDebugEnabled) {
        LOG.debug("< getStubGroup " + (System.currentTimeMillis() - start) + "ms, EDT=" + SwingUtilities.isEventDispatchThread() + ", file=" + currentFile.name + " title='" + result.title + " stub='" + result.isStub + "' " + result)
      }
      cache.setLast(currentFilePath, result)
    } catch (e: IndexNotReadyException) {
      LOG.debug(e.toString())
      throw IndexNotReady(
        ">getStubGroup project = [" + project.name + "], fileEditor = [" + fileEditor + "], displayedGroup = [" + displayedGroup + "], requestedGroup = [" + requestedGroup + "], force = [" + refresh + "]",
        e
      )
    } catch (e: Throwable) {
      LOG.debug(e.toString())
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
    var requestedGroup = requestedGroup
    if (LOG.isDebugEnabled) LOG.debug(">getGroup: fileEditor = [" + fileEditor + "], displayedGroup = [" + displayedGroup + "], requestedGroup = [" + requestedGroup + "], force = [" + refresh + "], stub = [" + stub + "]" + ", project = [" + project.name + "]")

    if (requestedGroup != null && requestedGroup !== displayedGroup) {
      val debuggingHelperLine = true
    }

    val start = System.currentTimeMillis()

    var result = EditorGroup.EMPTY
    try {
      if (requestedGroup == null) {
        requestedGroup = displayedGroup
      }

      val currentFilePath = currentFile.path

      val force = refresh && EditorGroupsSettingsState.state().isForceSwitch
      if (force && requestedGroup !is FavoritesGroup && requestedGroup !is BookmarkGroup) {
        if (result.isInvalid) {
          result = cache.getOwningOrSingleGroup(currentFilePath)
        }
        if (result.isInvalid) {
          result = cache.getLastEditorGroup(currentFile, currentFilePath, false, true, stub)
        }
        if (result.isInvalid) {
          result = RegexGroupProvider.getInstance(project).findFirstMatchingRegexGroup_stub(currentFile)
        }
      }

      if (result.isInvalid) {
        cache.validate(requestedGroup)
        if (requestedGroup.isValid
          && (requestedGroup is AutoGroup || requestedGroup.containsLink(project, currentFile) || requestedGroup.isOwner(currentFilePath))
        ) {
          result = requestedGroup
        }
      }

      if (!force) {
        if (result.isInvalid) {
          result = cache.getOwningOrSingleGroup(currentFilePath)
        }

        if (result.isInvalid) {
          result = cache.getLastEditorGroup(currentFile, currentFilePath, true, true, stub)
        }
      }

      if (result.isInvalid) {
        if (config.state.isSelectRegexGroup) {
          result = RegexGroupProvider.getInstance(project).findFirstMatchingRegexGroup_stub(currentFile)
        }
        if (result.isInvalid && config.state.isAutoSameName) {
          result = SameNameGroup.INSTANCE
        } else if (result.isInvalid && config.state.isAutoFolders) {
          result = FolderGroup.INSTANCE
        }
      }

      if (refresh || isEmptyAutogroup(project, result) || isIndexingAutoGroup(project, result)) {
        if (LOG.isDebugEnabled) {
          LOG.debug("refreshing result")
        }
        //_refresh
        if (!stub && result === requestedGroup && result is EditorGroupIndexValue) { // force loads new one from index
          cache.initGroup(result)
        } else if (!stub && result is SameNameGroup) {
          result = autoGroupProvider.getSameNameGroup(currentFile)
        } else if (!stub && result is RegexGroup) {
          result = regexGroupProvider.getRegexGroup(result, project, currentFile)
        } else if (result is FolderGroup) {
          result = autoGroupProvider.getFolderGroup(currentFile)
        } else if (result is FavoritesGroup) {
          result = externalGroupProvider.getFavoritesGroup(result.title)
        } else if (result is BookmarkGroup) {
          result = externalGroupProvider.bookmarkGroup
        }


        if (!stub && sameNameGroupIsEmpty(
            project,
            result,
            requestedGroup
          ) && !(requestedGroup is SameNameGroup && !requestedGroup.isStub())
        ) {
          val multiGroup = cache.getMultiGroup(currentFile)
          if (multiGroup.isValid) {
            result = multiGroup
          } else if (config.state.isAutoFolders && AutoGroup.SAME_FILE_NAME != cache.getLast(currentFilePath)) {
            result = autoGroupProvider.getFolderGroup(currentFile)
          }
        }
      }

      result.isStub = stub

      if (LOG.isDebugEnabled) {
        LOG.debug("< getGroup " + (System.currentTimeMillis() - start) + "ms, EDT=" + SwingUtilities.isEventDispatchThread() + ", file=" + currentFile.name + " title='" + result.title + " stub='" + result.isStub + "' " + result)
      }
      cache.setLast(currentFilePath, result)
    } catch (e: IndexNotReadyException) {
      LOG.debug(e.toString())
      throw IndexNotReady(
        ">getGroup project = [" + project.name + "], fileEditor = [" + fileEditor + "], displayedGroup = [" + displayedGroup + "], requestedGroup = [" + requestedGroup + "], force = [" + refresh + "]",
        e
      )
    } catch (e: Throwable) {
      LOG.debug(e.toString())
      throw e
    }
    return result
  }

  private fun isIndexingAutoGroup(project: Project, result: EditorGroup): Boolean {
    if (result is AutoGroup && !isDumb.isDumb(project)) {
      return result.hasIndexing()
    }
    return false
  }

  private fun isEmptyAutogroup(project: Project, result: EditorGroup): Boolean {
    return result is AutoGroup && result.size(project) == 0
  }

  private fun sameNameGroupIsEmpty(project: Project, result: EditorGroup, requestedGroup: EditorGroup): Boolean {
    return (result is SameNameGroup && result.size(project) <= 1) || (result === EditorGroup.EMPTY && requestedGroup is SameNameGroup)
  }

  fun switching(switchRequest: SwitchRequest) {
    this.switchRequest = switchRequest
    switching = true
    if (LOG.isDebugEnabled) LOG.debug("switching " + "switching = [" + switching + "], group = [" + switchRequest.getGroup() + "], fileToOpen = [" + switchRequest.getFileToOpen() + "], myScrollOffset = [" + switchRequest.getMyScrollOffset() + "]")
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
    val switchingFile = if (switchRequest == null) {
      null
    } else {
      switchRequest!!.fileToOpen
    }


    if (file == switchingFile) {
      val switchingGroup = switchRequest
      clearSwitchingRequest()
      if (LOG.isDebugEnabled) {
        LOG.debug("<getSwitchingRequest $switchingGroup")
      }
      return switchingGroup
    }
    if (LOG.isDebugEnabled) LOG.debug("<getSwitchingRequest=null  file = [$file], switchingFile=$switchingFile")
    return null
  }

  fun getSwitchingRequest(file: VirtualFile): SwitchRequest? {
    val switchingFile = if (switchRequest == null) {
      null
    } else {
      switchRequest!!.fileToOpen
    }

    if (file == switchingFile) {
      return switchRequest
    }
    return null
  }

  fun isSwitching(): Boolean {
    if (LOG.isDebugEnabled) {
      LOG.debug("isSwitching switchRequest=$switchRequest, switching=$switching")
    }
    return switchRequest != null || switching
  }

  fun getGroups(file: VirtualFile?): List<EditorGroup> {
    val groups = cache.findGroups(file)
    groups.sort(COMPARATOR)
    return groups
  }

  @get:Throws(IndexNotReadyException::class)
  val allIndexedGroups: List<EditorGroupIndexValue>
    // TODO cache it?
    get() {
      val start = System.currentTimeMillis()
      val allGroups = cache.allGroups
      allGroups.sort(COMPARATOR)
      if (LOG.isDebugEnabled) LOG.debug("getAllGroups " + (System.currentTimeMillis() - start))
      return allGroups
    }

  fun initCache() {
    panelRefresher.initCache()
  }

  fun getColor(file: VirtualFile?): Color? {
    val group = cache.getEditorGroupForColor(file)
    if (group != null) {
      return group.bgColor
    }
    return null
  }

  fun getFgColor(file: VirtualFile?): Color? {
    val group = cache.getEditorGroupForColor(file)
    if (group != null) {
      return group.fgColor
    }
    return null
  }

  fun open(
    groupPanel: EditorGroupPanel,
    fileToOpen: VirtualFile,
    line: Int?,
    newWindow: Boolean,
    newTab: Boolean,
    split: Splitters
  ): Result {
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
  ): Result {
    return open2(null, current, virtualFileByAbsolutePath, null, group, window, tab, split, SwitchRequest(group, virtualFileByAbsolutePath))
  }

  private fun open2(
    currentWindowParam: EditorWindow?,
    currentFile: VirtualFile?,
    fileToOpen: VirtualFile,
    line: Int?,
    group: EditorGroup,
    newWindow: Boolean,
    newTab: Boolean,
    split: Splitters,
    switchRequest: SwitchRequest
  ): Result {
    if (LOG.isDebugEnabled) LOG.debug("open2 fileToOpen = [$fileToOpen], currentFile = [$currentFile], group = [$group], newWindow = [$newWindow], newTab = [$newTab], split = [$split], switchingRequest = [$switchRequest]")
    val resultAtomicReference = AtomicReference<Result>()
    switching(switchRequest)

    if (!warningShown && UISettings.getInstance().reuseNotModifiedTabs) {
      Notifications.notifyBugs()
      warningShown = true
    }


    if (initialEditorIndex == null) {
      // TODO it does not work in constructor
      try {
        initialEditorIndex = Key.findKeyByName("initial editor index")
      } catch (e: Exception) {
        LOG.error(e)
        initialEditorIndex = Key.create<Any>("initial editor index not found")
      }
    }


    CommandProcessor.getInstance().executeCommand(project, {
      val manager = FileEditorManager.getInstance(project) as FileEditorManagerImpl
      var selectedFile = currentFile
      var currentWindow = currentWindowParam
      if (currentWindow == null) {
        currentWindow = manager.currentWindow
      }
      if (selectedFile == null && currentWindow != null) {
        selectedFile = currentWindow.selectedFile
      }

      if (!split.isSplit && !newWindow && fileToOpen == selectedFile) {
        val editors = Objects.requireNonNull(currentWindow)?.manager?.getSelectedEditor(fileToOpen)
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
      if (split.isSplit && currentWindow != null) {
        if (LOG.isDebugEnabled) LOG.debug("openFileInSplit $fileToOpen")
        val split1 = currentWindow.split(split.orientation, true, fileToOpen, true)
        if (split1 == null) {
          LOG.debug("no editors opened")
          resetSwitching()
        }
      } else if (newWindow) {
        if (LOG.isDebugEnabled) LOG.debug("openFileInNewWindow fileToOpen = $fileToOpen")
        val pair = manager.openFileInNewWindow(fileToOpen)
        scroll(line, *pair.first)
        if (pair.first.size == 0) {
          LOG.debug("no editors opened")
          resetSwitching()
        }
      } else {
        val reuseNotModifiedTabs = UISettings.getInstance().reuseNotModifiedTabs

        //				boolean fileWasAlreadyOpen = currentWindow.isFileOpen(fileToOpen);
        try {
          if (newTab) {
            UISettings.getInstance().reuseNotModifiedTabs = false
          }

          if (LOG.isDebugEnabled) LOG.debug("openFile $fileToOpen")
          val pair = if (currentWindow == null) {
            manager.openFileWithProviders(fileToOpen, true, true)
          } else {
            manager.openFileWithProviders(fileToOpen, true, currentWindow)
          }
          val fileEditors = pair.first

          if (fileEditors.size == 0) {  // directory or some fail
            Notifications.warning("Unable to open editor for file " + fileToOpen.name)
            LOG.debug("no editors opened")
            resetSwitching()
            return@executeCommand
          }
          for (fileEditor in fileEditors) {
            if (LOG.isDebugEnabled) LOG.debug("opened fileEditor = $fileEditor")
          }
          scroll(line, *fileEditors)

          if (reuseNotModifiedTabs) {
            return@executeCommand
          }
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
    }, null, null)


    return resultAtomicReference.get()
  }

  private fun scroll(line: Int?, vararg fileEditors: FileEditor): Boolean {
    if (line != null) {
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
    }
    return false
  }

  fun resetSwitching() {
    clearSwitchingRequest()
    enableSwitching()
  }

  private fun clearSwitchingRequest() {
    LOG.debug("clearSwitchingRequest")
    switchRequest = null
  }

  class Result(var isScrolledOnly: Boolean)

  companion object {
    private val LOG = Logger.getInstance(EditorGroupManager::class.java)
    val COMPARATOR: Comparator<EditorGroup> = Comparator.comparing { o: EditorGroup -> o.title.lowercase(Locale.getDefault()) }

    @JvmStatic
    fun getInstance(project: Project): EditorGroupManager {
      return project.getService(EditorGroupManager::class.java)
    }
  }
}
