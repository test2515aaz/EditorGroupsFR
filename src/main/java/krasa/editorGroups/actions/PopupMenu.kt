package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import java.awt.Component

object PopupMenu {
  val actions = listOf(
    SwitchGroupAction.ID,
    SwitchFileAction.ID,
    "-",
    RefreshAction.ID,
    "-",
    NextAction.ID,
    PreviousAction.ID,
    "-",
    ReindexThisFileAction.ID,
    ReindexAction.ID,
    "-",
    ToggleAutoSameNameGroupsAction.ID,
    ToggleAutoFolderGroupsAction.ID,
    ToggleForceAction.ID,
    ToggleHideEmptyAction.ID,
    "-",
    OpenConfigurationAction.ID
  )

  private val defaultActionGroup: DefaultActionGroup = DefaultActionGroup()

  init {
    addActionsToGroup()
  }

  private fun addActionsToGroup() {
    val actionManager = ActionManager.getInstance()
    actions.forEach {
      when (it) {
        "-"  -> defaultActionGroup.add(Separator())
        else -> defaultActionGroup.add(actionManager.getAction(it))
      }
    }
  }

  @JvmStatic
  fun popupInvoked(component: Component?, x: Int, y: Int) {
    val group = defaultActionGroup
    val menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, group)
    menu.component.show(component, x, y)
  }
}
