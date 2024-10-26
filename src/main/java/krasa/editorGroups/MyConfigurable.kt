package krasa.editorGroups

import com.intellij.openapi.options.Configurable
import krasa.editorGroups.gui.SettingsForm
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.settings.EditorGroupsSettings.Companion.instance
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent

class MyConfigurable : Configurable {
  private var form: SettingsForm? = null

  override fun getDisplayName(): @Nls String = message("krasa.editorGroups.EditorGroupsSettings")

  override fun getHelpTopic(): @NonNls String? = null

  override fun createComponent(): JComponent? {
    if (form == null) form = SettingsForm()
    return form!!.root
  }

  override fun isModified(): Boolean = form != null && form!!.isSettingsModified(instance)

  override fun apply() {
    form?.apply()
  }

  override fun reset() {
    form?.importFrom(instance)
  }

  override fun disposeUIResources() {
    form = null
  }
}
