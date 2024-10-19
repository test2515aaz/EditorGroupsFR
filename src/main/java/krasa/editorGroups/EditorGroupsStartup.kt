package krasa.editorGroups

import com.intellij.ide.ui.UISettings
import com.intellij.ide.ui.UISettingsListener
import com.intellij.ide.ui.UISettingsListener.TOPIC
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.support.unwrapPreview
import javax.swing.SwingConstants

class EditorGroupsStartup : FileEditorManagerListener {
  override fun fileOpened(manager: FileEditorManager, file: VirtualFile) {
    val project = manager.project
    thisLogger().debug(">fileOpenedSync [$file]")

    val fileToOpen = unwrapPreview(file) ?: return
    val editorGroupManager = EditorGroupManager.getInstance(project)

    val switchRequest = editorGroupManager.getAndClearSwitchingRequest(fileToOpen)
    val editors = manager.getEditors(fileToOpen)

    // Create editor group panel if it doesn't exist'
    for (fileEditor in editors) {
      if (fileEditor.getUserData(EditorGroupPanel.EDITOR_PANEL) != null) continue

      val start = System.currentTimeMillis()

      createPanel(
        project = project,
        manager = manager,
        file = fileToOpen,
        switchRequest = switchRequest,
        fileEditor = fileEditor
      )

      thisLogger().debug("<fileOpenedSync EditorGroupPanel created, file=$fileToOpen in ${System.currentTimeMillis() - start}ms, fileEditor=$fileEditor")
    }
  }

  /** When a tab is selected, refresh the editor group panel. */
  override fun selectionChanged(event: FileEditorManagerEvent) {
    val project = event.manager.project
    thisLogger().debug("selectionChanged $event")
    val fileEditor = event.newEditor
    if (fileEditor == null) return

    val panel = fileEditor.getUserData(EditorGroupPanel.EDITOR_PANEL)
    if (panel == null) return

    val instance = EditorGroupManager.getInstance(project)
    val switchRequest = instance.getAndClearSwitchingRequest(panel.file)

    if (switchRequest != null) {
      val switchingGroup = switchRequest.group
      val scrollOffset = switchRequest.myScrollOffset

      // Refresh panel
      panel.refreshOnSelectionChanged(false, switchingGroup, scrollOffset)
    } else {
      panel.refreshPane(false, null)
    }
  }

  override fun fileClosed(source: FileEditorManager, file: VirtualFile) = thisLogger().debug("fileClosed [$file]")

  /**
   * Creates the editor group panel
   *
   * @param project The [Project] associated with the editor panel.
   * @param manager The [FileEditorManager] that will manage the newly created panel.
   * @param file The [VirtualFile] associated with the editor panel.
   * @param switchRequest The request that triggered the panel creation, can be null.
   * @param fileEditor The [FileEditor] for which the panel is being created.
   */
  private fun createPanel(
    project: Project,
    manager: FileEditorManager,
    file: VirtualFile,
    switchRequest: SwitchRequest?,
    fileEditor: FileEditor
  ) {
    if (!fileEditor.isValid) {
      thisLogger().debug(">createPanel: fileEditor already disposed")
      return
    }

    fun renderPanel(): EditorGroupPanel {
      val panel = EditorGroupPanel(fileEditor, project, switchRequest, file)
      val editorTabPlacement = UISettings.getInstance().editorTabPlacement
      when (editorTabPlacement) {
        SwingConstants.TOP    -> manager.addTopComponent(fileEditor, panel.root)
        SwingConstants.BOTTOM -> manager.addBottomComponent(fileEditor, panel.root)
        else                  -> manager.addTopComponent(fileEditor, panel.root)
      }
      panel.postConstruct()
      return panel
    }

    val panel = renderPanel()

    // Listen for UI settings changes on this panel
    ApplicationManager.getApplication().messageBus.connect(panel)
      .subscribe(TOPIC, object : UISettingsListener {
        override fun uiSettingsChanged(uiSettings: UISettings) {
          when {
            !panel.isValid                                                               -> return
            UISettings.getInstance().editorTabPlacement == uiSettings.editorTabPlacement -> return
            else                                                                         -> {
              manager.removeTopComponent(fileEditor, panel.root)
              manager.removeBottomComponent(fileEditor, panel.root)
              renderPanel()
            }
          }
        }
      })

  }
}
