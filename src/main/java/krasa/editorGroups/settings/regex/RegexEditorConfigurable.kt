package krasa.editorGroups.settings.regex

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SearchTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.ColumnInfo
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.settings.regex.columns.*
import krasa.editorGroups.settings.resetButton
import javax.swing.JComponent
import javax.swing.JPanel

internal class RegexEditorConfigurable :
  BoundSearchableConfigurable(
    message("settings.regex"),
    ID
  ),
  Disposable {
  private var main: DialogPanel? = null

  private val settings: RegexGroupsSettings = RegexGroupsSettings.instance
  private val settingsClone: RegexGroupsSettings = settings.clone()

  private var regexTablePanel: JPanel? = null
  private var regexSearch: SearchTextField = SearchTextField()
  private lateinit var regexModelTable: JComponent
  private var regexModelTableEditor: RegexTableModelEditor? = null

  private val columns = arrayOf<ColumnInfo<*, *>>(
    EnabledColumnInfo(),
    TouchedColumnInfo(),
    NameEditableColumnInfo(parent = this, editable = true),
    PatternEditableColumnInfo(parent = this, editable = true),
    ScopeEditableColumnInfo(editable = true),
    NotComparingGroupsEditableColumnInfo(parent = this, editable = true),
  )

  init {
    createTable()

    regexTablePanel = panel {
      row {
        cell(regexSearch)
          .align(Align.FILL)
      }

      row {
        cell(regexModelTable)
          .align(Align.FILL)
      }
    }

    main = panel {
      row {
        comment(message("RegexEditorConfigurable.explanation.text"))
      }

      row {
        comment(message("RegexEditorConfigurable.explanation2.text"))
      }

      row {
        comment(message("RegexEditorConfigurable.explanation3.text"))
      }

      row {
        cell(regexTablePanel!!)
          .align(Align.FILL)
      }

      resetButton { doReset() }
    }

    regexSearch.textEditor.emptyText.text = message("fileSearch.placeholder")
  }

  /** Create the file icons. */
  private fun createTable() {
    val itemEditor = RegexTableItemEditor()
    regexModelTableEditor = RegexTableModelEditor(
      columns,
      itemEditor,
      message("no.regex.models"),
      regexSearch,
    )
    regexModelTable = (regexModelTableEditor ?: return).createComponent()
  }

  private fun doReset() {
    RegexGroupsSettings.instance.askResetSettings {
      settingsClone.reset()
      settings.reset()
      main?.reset()
    }
  }

  override fun createPanel(): DialogPanel {
    loadData()
    return main!!
  }

  private fun loadData() {
    ApplicationManager.getApplication().invokeLater {
      if (regexModelTableEditor != null) {
        (regexModelTableEditor ?: return@invokeLater).reset(settings.regexGroupModels.regexModels)
      }
    }
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

  override fun dispose() {
    regexTablePanel = null
  }

  companion object {
    const val ID: String = "RegexEditorConfigurable"
  }
}
