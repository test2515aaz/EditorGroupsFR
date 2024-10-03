package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import java.awt.Component

object PopupMenu {
  val defaultActionGroup: DefaultActionGroup
    get() {
      val group = DefaultActionGroup()
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.SwitchGroup"))
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.SwitchFile"))
      group.add(Separator())
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.Refresh"))
      group.add(Separator())
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.Next"))
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.Previous"))
      group.add(Separator())
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.ReindexThisFile"))
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.Reindex"))
      group.add(Separator())
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.ToggleAutoSameNameGroups"))
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.ToggleFolderEditorGroups"))
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.ToggleForce"))
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.ToggleHideEmpty"))
      //		group.add(ActionManager.getInstance().getAction("krasa.editorGroups.ToggleShowSize"));
      group.add(Separator())
      group.add(ActionManager.getInstance().getAction("krasa.editorGroups.OpenConfiguration"))
      return group
    }

  @JvmStatic
  fun popupInvoked(component: Component?, x: Int, y: Int) {
    val group = defaultActionGroup
    val menu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.UNKNOWN, group)
    menu.component.show(component, x, y)
  }
}
