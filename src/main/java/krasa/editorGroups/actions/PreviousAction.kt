package krasa.editorGroups.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.ui.PopupHandler
import com.intellij.util.BitUtil
import krasa.editorGroups.support.Splitters
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent

class PreviousAction : EditorGroupsAction(), CustomComponentAction {
  override fun actionPerformed(anActionEvent: AnActionEvent) {
    val panel = getEditorGroupPanel(anActionEvent)

    if (panel != null) {
      val e = checkNotNull(anActionEvent.inputEvent)
      val newTab = BitUtil.isSet(e.modifiersEx, InputEvent.CTRL_DOWN_MASK) && e is MouseEvent && e.clickCount > 0

      panel.goToPreviousTab(
        newTab = newTab,
        newWindow = BitUtil.isSet(e.modifiersEx, InputEvent.SHIFT_DOWN_MASK),
        split = Splitters.from(e)
      )
    }
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    val button = ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
    presentation.icon = AllIcons.Actions.Back

    button.addMouseListener(object : PopupHandler() {
      override fun invokePopup(comp: Component, x: Int, y: Int) = PopupMenu.popupInvoked(comp, x, y)
    })

    return button
  }

  companion object {
    const val ID: String = "krasa.editorGroups.Previous"
  }
}
