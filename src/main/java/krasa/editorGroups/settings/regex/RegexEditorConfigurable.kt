package krasa.editorGroups.settings.regex

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.settings.resetButton

internal class RegexEditorConfigurable : BoundSearchableConfigurable(
  message("settings.regex"),
  ID
) {
  private var main: DialogPanel? = null

  private val settings: RegexGroupsSettings = RegexGroupsSettings.instance
  private val settingsClone: RegexGroupsSettings = settings.clone()

  /** Init the dialog. */
  fun init() {
    initComponents()
  }

  private fun initComponents() {
    main = panel {
      resetButton { doReset() }
    }
  }

  private fun doReset() {
    RegexGroupsSettings.instance.askResetSettings {
      settingsClone.reset()
      settings.reset()
      main?.reset()
    }
  }

  override fun createPanel(): DialogPanel {
    init()
    return main!!
  }

  override fun getId(): String = ID

  override fun getDisplayName(): String = message("settings.regex")

  override fun isModified(): Boolean {
    if (super.isModified()) return true
    return settings != settingsClone
  }

  override fun apply() {
    super.apply()

    settings.apply(settingsClone)
    RegexGroupsSettings.instance.fireChanged()
  }

  companion object {
    const val ID: String = "RegexEditorConfigurable"
  }
}
