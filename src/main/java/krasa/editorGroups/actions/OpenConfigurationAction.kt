package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import krasa.editorGroups.icons.EditorGroupsIcons
import krasa.editorGroups.settings.EditorSettingsConfigurable
import javax.swing.JComponent

class OpenConfigurationAction : DumbAwareAction(), CustomComponentAction {
  override fun actionPerformed(e: AnActionEvent) {
    val instance = EditorSettingsConfigurable()
    ShowSettingsUtil.getInstance().editConfigurable(e.project, EditorSettingsConfigurable.ID, instance, true)
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    val refresh = ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
    presentation.icon = EditorGroupsIcons.settings
    return refresh
  }

  companion object {
    const val ID: String = "krasa.editorGroups.OpenConfiguration"
  }
}
