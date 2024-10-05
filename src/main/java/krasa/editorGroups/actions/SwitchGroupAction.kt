package krasa.editorGroups.actions

import com.intellij.ide.actions.QuickSwitchSchemeAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.actionSystem.impl.ActionMenuItem
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.PopupHandler
import krasa.editorGroups.*
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.actions.PopupMenu.popupInvoked
import krasa.editorGroups.icons.EditorGroupsIcons
import krasa.editorGroups.model.*
import krasa.editorGroups.support.Notifications.showWarning
import krasa.editorGroups.support.getVirtualFileByAbsolutePath
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import java.util.stream.Collectors
import javax.swing.JComponent

class SwitchGroupAction : QuickSwitchSchemeAction(), DumbAware, CustomComponentAction {

  override fun showPopup(e: AnActionEvent, popup: ListPopup) {
    val project = e.project
    if (project == null) {
      popup.showInBestPositionFor(e.dataContext)
      return
    }

    val inputEvent = e.inputEvent
    if (inputEvent !is MouseEvent) {
      popup.showCenteredInCurrentWindow(project)
      return
    }

    val component = inputEvent.component
    when (component) {
      is ActionMenuItem -> popup.showInBestPositionFor(e.dataContext)
      else              -> popup.showUnderneathOf(component)
    }
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    val button = ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
    presentation.icon = EditorGroupsIcons.groupBy

    button.addMouseListener(object : PopupHandler() {
      override fun invokePopup(comp: Component, x: Int, y: Int) = popupInvoked(comp, x, y)
    })

    return button
  }

  override fun fillActions(project: Project, defaultActionGroup: DefaultActionGroup, dataContext: DataContext) {
    try {
      val data = dataContext.getData(PlatformDataKeys.FILE_EDITOR)
      var panel: EditorGroupPanel? = null
      var displayedGroup = EditorGroup.EMPTY
      var editorGroups = emptyList<EditorGroup>()
      val tempGroup = DefaultActionGroup()
      var file: VirtualFile? = null
      var regexGroups = emptyList<RegexGroup>()

      if (data != null) {
        panel = data.getUserData(EditorGroupPanel.EDITOR_PANEL)
        if (panel != null) {
          file = panel.file
          displayedGroup = panel.displayedGroup

          defaultActionGroup.add(
            createAction(
              displayedGroup,
              SameNameGroup(file.nameWithoutExtension, emptyList()),
              project,
              refreshHandler(panel)
            )
          )
          defaultActionGroup.add(createAction(displayedGroup, FolderGroup(file.parent, emptyList()), project, refreshHandler(panel)))
          defaultActionGroup.add(createAction(displayedGroup, HidePanelGroup(), project, refreshHandler(panel)))

          editorGroups = fillCurrentFileGroups(project, tempGroup, panel, file)
          regexGroups = fillRegexGroups(project, tempGroup, panel, file)
        }
      }

      addBookmarkGroup(project, defaultActionGroup, panel, displayedGroup, file)
      fillOtherIndexedGroups(tempGroup, editorGroups, displayedGroup, project)
      fillFavorites(tempGroup, project, editorGroups, displayedGroup)
      fillGlobalRegexGroups(tempGroup, project, editorGroups, displayedGroup, regexGroups)


      if (state().isGroupSwitchGroupAction) {
        defaultActionGroup.addAll(*tempGroup.childActionsOrStubs)
      } else {
        val childActionsOrStubs = tempGroup.childActionsOrStubs
        val list = Arrays.stream(childActionsOrStubs)
          .filter { anAction: AnAction? -> anAction !is Separator }
          .sorted { o1: AnAction, o2: AnAction -> o1.templatePresentation.text.compareTo(o2.templatePresentation.text, ignoreCase = true) }
          .collect(Collectors.toList())

        defaultActionGroup.add(Separator())
        defaultActionGroup.addAll(list)
      }

      defaultActionGroup.add(Separator())
      //			defaultActionGroup.add(ActionManager.getInstance().getAction("krasa.editorGroups.TogglePanelVisibility"));
      defaultActionGroup.add(ActionManager.getInstance().getAction("krasa.editorGroups.OpenConfiguration"))
    } catch (e: IndexNotReadyException) {
      LOG.error("That should not happen", e)
    }
  }

  private fun addBookmarkGroup(
    project: Project,
    defaultActionGroup: DefaultActionGroup,
    panel: EditorGroupPanel?,
    displayedGroup: EditorGroup,
    file: VirtualFile?
  ) {
    val bookmarkGroup = ExternalGroupProvider.getInstance(project).bookmarkGroup
    val action = createAction(displayedGroup, bookmarkGroup, project, object : Handler() {
      override fun run(editorGroup: EditorGroup) {
        if (panel != null && file != null && bookmarkGroup.containsLink(project, file)) {
          refreshHandler(panel).run(bookmarkGroup)
        } else {
          otherGroupHandler(project).run(bookmarkGroup)
        }
      }
    })

    if (bookmarkGroup.size(project) == 0) {
      action.templatePresentation.isEnabled = false
      action.templatePresentation.setText("${bookmarkGroup.title} - empty")
    }

    defaultActionGroup.add(action)
  }

  private fun fillCurrentFileGroups(
    project: Project,
    group: DefaultActionGroup,
    panel: EditorGroupPanel,
    file: VirtualFile?
  ): List<EditorGroup> {
    val displayedGroup = panel.displayedGroup
    val manager = EditorGroupManager.getInstance(project)
    val groups = manager.getGroups(file!!)

    group.add(Separator("Groups for the Current File"))

    for (g in groups) {
      group.add(createAction(displayedGroup, g, project, refreshHandler(panel)))
    }

    return groups
  }

  private fun fillRegexGroups(project: Project, group: DefaultActionGroup, panel: EditorGroupPanel, file: VirtualFile?): List<RegexGroup> {
    val regexGroups = RegexGroupProvider.getInstance(project).findMatchingRegexGroups(file!!)

    for (regexGroup in regexGroups) {
      group.add(createAction(panel.displayedGroup, regexGroup, project, refreshHandler(panel)))
    }

    return regexGroups
  }

  private fun fillOtherIndexedGroups(
    group: DefaultActionGroup,
    currentGroups: List<EditorGroup>,
    displayedGroup: EditorGroup,
    project: Project
  ) {
    val manager = EditorGroupManager.getInstance(project)

    group.add(Separator("Other Groups"))

    try {
      val allGroups = manager.allIndexedGroups
      for (g in allGroups) {
        if (!(currentGroups as Collection<EditorGroup>).contains(g)) {
          group.add(createAction(displayedGroup, g, project, otherGroupHandler(project)))
        }
      }

    } catch (e: ProcessCanceledException) {
      val action: AnAction = object : AnAction("Indexing...") {
        override fun actionPerformed(anActionEvent: AnActionEvent) = Unit
      }

      action.templatePresentation.isEnabled = false
      group.add(action)
    } catch (e: IndexNotReadyException) {
      val action: AnAction = object : AnAction("Indexing...") {
        override fun actionPerformed(anActionEvent: AnActionEvent) = Unit
      }

      action.templatePresentation.isEnabled = false
      group.add(action)
    }
  }

  private fun fillFavorites(
    defaultActionGroup: DefaultActionGroup,
    project: Project,
    editorGroups: List<EditorGroup>,
    displayedGroup: EditorGroup
  ) {
    val favoritesGroups = ExternalGroupProvider.getInstance(project).favoritesGroups

    val alreadyDisplayed: MutableSet<String> = HashSet()
    for (group in editorGroups) {
      if (group is FavoritesGroup) {
        alreadyDisplayed.add(group.title)
      }
    }

    if (!favoritesGroups.isEmpty()) {
      defaultActionGroup.add(Separator("Favourites"))
      for (favoritesGroup in favoritesGroups) {
        if (!alreadyDisplayed.contains(favoritesGroup.title)) {
          defaultActionGroup.add(createAction(displayedGroup, favoritesGroup, project, otherGroupHandler(project)))
        }
      }
    }
  }

  private fun fillGlobalRegexGroups(
    defaultActionGroup: DefaultActionGroup,
    project: Project,
    editorGroups: List<EditorGroup>,
    displayedGroup: EditorGroup,
    alreadyFilledRegexGroups: List<RegexGroup>
  ) {
    val regexGroups = RegexGroupProvider.getInstance(project).findProjectRegexGroups()

    val alreadyDisplayed: MutableSet<String?> = HashSet()
    for (group in alreadyFilledRegexGroups) {
      if (group.regexGroupModel.scope == RegexGroupModel.Scope.WHOLE_PROJECT) {
        alreadyDisplayed.add(group.regexGroupModel.regex)
      }
    }

    if (!regexGroups.isEmpty()) {
      defaultActionGroup.add(Separator("Regexps"))

      regexGroups
        .asSequence()
        .filterNot { alreadyDisplayed.contains(it.regexGroupModel.regex) }
        .forEach {
          defaultActionGroup.add(createAction(displayedGroup, it, project, object : Handler() {
            override fun run(editorGroup: EditorGroup) {
              val regexGroup = RegexGroupProvider.getInstance(project).getRegexGroup(it, project, null)
              otherGroupHandler(project).run(regexGroup)
            }
          }))
        }
    }
  }

  private fun refreshHandler(panel: EditorGroupPanel): Handler = object : Handler() {
    override fun run(editorGroup: EditorGroup) {
      if (LOG.isDebugEnabled) LOG.debug("switching group")
      panel._refresh(false, editorGroup)
    }
  }

  private fun otherGroupHandler(project: Project?): Handler = object : Handler() {
    override fun run(editorGroup: EditorGroup) {
      val file = editorGroup.getFirstExistingFile(project!!)

      if (file != null) {
        EditorGroupManager.getInstance(project).open(
          virtualFileByAbsolutePath = file,
          window = false,
          tab = true,
          split = Splitters.NONE,
          group = editorGroup,
          current = null
        )
      } else {
        val ownerPath = editorGroup.ownerPath
        val virtualFileByAbsolutePath = getVirtualFileByAbsolutePath(ownerPath)

        if (virtualFileByAbsolutePath != null) {
          EditorGroupManager.getInstance(project).open(
            virtualFileByAbsolutePath = virtualFileByAbsolutePath,
            window = false,
            tab = true,
            split = Splitters.NONE,
            group = editorGroup,
            current = null
          )
        } else {
          showWarning("No matching file found")
          if (LOG.isDebugEnabled) LOG.debug("opening failed, no file and not even owner exist $editorGroup")
        }

        if (LOG.isDebugEnabled) LOG.debug("opening failed, no file exists $editorGroup")
      }
    }
  }

  private fun createAction(displayedGroup: EditorGroup, groupLink: EditorGroup, project: Project, actionHandler: Handler): DumbAwareAction {
    val isSelected = displayedGroup.isSelected(groupLink)
    var title = groupLink.switchTitle(project)
    val description = groupLink.switchDescription

    if (isSelected) {
      title += " - Current"
    }

    val dumbAwareAction: DumbAwareAction = object : DumbAwareAction(title, description, groupLink.icon()) {
      override fun actionPerformed(e1: AnActionEvent) = actionHandler.run(groupLink)
    }

    dumbAwareAction.templatePresentation.isEnabled = !isSelected
    return dumbAwareAction
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    val presentation = e.presentation
    val data = e.getData(PlatformDataKeys.FILE_EDITOR)

    if (data != null) {
      val panel = data.getUserData(EditorGroupPanel.EDITOR_PANEL)
      if (panel != null) {
        var displayedGroup = panel.displayedGroup
        if (displayedGroup === EditorGroup.EMPTY) {
          val toBeRendered = panel.toBeRendered
          if (toBeRendered != null) {
            displayedGroup = toBeRendered // to remove flicker when switching
          }
        }
        presentation.icon = displayedGroup.icon()
      }
    }
  }

  internal abstract class Handler {
    abstract fun run(editorGroup: EditorGroup)
  }

  companion object {
    private val LOG = Logger.getInstance(SwitchGroupAction::class.java)

    const val ID = "krasa.editorGroups.SwitchGroup"
  }
}
