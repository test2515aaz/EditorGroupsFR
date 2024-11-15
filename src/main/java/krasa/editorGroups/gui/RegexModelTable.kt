package krasa.editorGroups.gui

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.ui.table.JBTable
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.model.RegexGroupModel
import krasa.editorGroups.model.Scope
import krasa.editorGroups.settings.regex.RegexGroupsSettings
import java.awt.Component
import java.util.*
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class RegexModelTable : JBTable() {
  private val myTableModel: MyTableModel = MyTableModel()
  private val myRegexGroupModels: MutableList<RegexGroupModel> = MutableList(0) { RegexGroupModel() }

  init {
    model = myTableModel

    val column = getColumnModel().getColumn(REGEX_COLUMN)
    column.cellRenderer = object : DefaultTableCellRenderer() {
      override fun getTableCellRendererComponent(
        table: JTable,
        value: Any,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
      ): Component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    }

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  }

  fun getRegexModelValueAt(row: Int): Scope = getValueAt(row, SCOPE_COLUMN) as Scope

  fun addRegexModel() {
    val regexModelEditor = RegexModelEditor(message("add.regexgroup"), "", "", Scope.CURRENT_FOLDER)

    if (regexModelEditor.showAndGet()) {
      val name = regexModelEditor.regex

      myRegexGroupModels.add(
        RegexGroupModel.from(
          regex = name,
          scope = regexModelEditor.scopeCombo,
          notComparingGroups = regexModelEditor.notComparingGroups
        )
      )

      val index = indexOfRegexModelWithName(name)
      thisLogger().assertTrue(index >= 0)

      myTableModel.fireTableDataChanged()
      setRowSelectionInterval(index, index)
    }
  }

  private fun isValidRow(selectedRow: Int): Boolean = selectedRow >= 0 && selectedRow < myRegexGroupModels.size

  fun moveUp() {
    val selectedRow = selectedRow
    val prevRow = selectedRow - 1
    if (selectedRow != -1) Collections.swap(myRegexGroupModels, selectedRow, prevRow)

    setRowSelectionInterval(prevRow, prevRow)
  }

  fun moveDown() {
    val selectedRow = selectedRow
    val nextRow = selectedRow + 1
    if (selectedRow != -1) Collections.swap(myRegexGroupModels, selectedRow, nextRow)

    setRowSelectionInterval(nextRow, nextRow)
  }

  fun removeSelectedRegexModels() {
    val selectedRows = this.selectedRows
    if (selectedRows.size == 0) return

    Arrays.sort(selectedRows)
    val originalRow = selectedRows[0]

    selectedRows.indices.reversed()
      .asSequence()
      .map { selectedRows[it] }
      .filter { isValidRow(it) }
      .forEach { myRegexGroupModels.removeAt(it) }

    myTableModel.fireTableDataChanged()

    when {
      originalRow < rowCount -> setRowSelectionInterval(originalRow, originalRow)
      rowCount > 0           -> {
        val index = rowCount - 1
        setRowSelectionInterval(index, index)
      }
    }
  }

  fun commit(settings: RegexGroupsSettings) {
    settings.regexGroupModels.regexModels = myRegexGroupModels
  }

  fun reset(settings: RegexGroupsSettings) {
    obtainRegexModels(myRegexGroupModels, settings)
    myTableModel.fireTableDataChanged()
  }

  private fun indexOfRegexModelWithName(name: String): Int = myRegexGroupModels.indexOfFirst { it.myRegex == name }

  private fun obtainRegexModels(regexModels: MutableList<RegexGroupModel>, settings: RegexGroupsSettings) {
    regexModels.clear()
    val regexGroupModels = settings.regexGroupModels.regexModels

    regexGroupModels.mapTo(regexModels) { it.copy() }
  }

  fun editRegexModel(): Boolean {
    if (selectedRowCount != 1) return false

    val selectedRow = this.selectedRow
    val regexGroupModel = myRegexGroupModels[selectedRow]
    val editor = RegexModelEditor(
      message("edit.regexgroup"),
      regexGroupModel.myRegex,
      regexGroupModel.myNotComparingGroups,
      regexGroupModel.myScope
    )

    if (editor.showAndGet()) {
      regexGroupModel.myRegex = editor.regex
      regexGroupModel.myNotComparingGroups = editor.notComparingGroups
      regexGroupModel.myScope = editor.scopeCombo
      myTableModel.fireTableDataChanged()
    }

    return true
  }

  fun isModified(settings: RegexGroupsSettings): Boolean {
    val regexGroupModels = MutableList<RegexGroupModel>(0) { RegexGroupModel() }
    obtainRegexModels(regexGroupModels, settings)
    return regexGroupModels != myRegexGroupModels
  }

  private inner class MyTableModel : AbstractTableModel() {
    override fun getColumnCount(): Int = 2

    override fun getRowCount(): Int = myRegexGroupModels.size

    override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
      val regexGroupModel = myRegexGroupModels[rowIndex]
      when (columnIndex) {
        REGEX_COLUMN -> return regexGroupModel.myRegex!!

        SCOPE_COLUMN -> return regexGroupModel.myScope!!
      }

      thisLogger().error("Wrong indices")
      return null
    }

    override fun getColumnName(columnIndex: Int): String? = when (columnIndex) {
      REGEX_COLUMN -> message("regex")
      SCOPE_COLUMN -> message("scope")
      else         -> null
    }
  }

  companion object {
    private const val REGEX_COLUMN = 0
    private const val SCOPE_COLUMN = 1
  }
}
