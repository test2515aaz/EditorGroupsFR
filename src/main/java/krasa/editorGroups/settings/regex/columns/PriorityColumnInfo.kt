/*
 * The MIT License (MIT)
 *
 *  Copyright (c) 2015-2022 Elior "Mallowigi" Boukhobza
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package krasa.editorGroups.settings.regex.columns

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.cellvalidators.StatefulValidatingCellEditor
import com.intellij.openapi.ui.cellvalidators.ValidatingTableCellRendererWrapper
import com.intellij.util.ui.table.TableModelEditor.EditableColumnInfo
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.model.RegexGroupModel
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

/** Editable column for the priority. */
@Suppress("UnstableApiUsage")
class PriorityColumnInfo(private val parent: Disposable, private val editable: Boolean) :
  EditableColumnInfo<RegexGroupModel, String>(message("RegexGroupConfigurable.columns.priority")) {

  /** Priority class is Integer. */
  override fun getColumnClass(): Class<Int> = Int::class.java

  /**
   * The value of the column is the priority
   *
   * @param item the [RegexGroupModel]
   * @return the priority
   */
  override fun valueOf(item: RegexGroupModel): String = item.priority.toString()

  /**
   * Set the [RegexGroupModel]'s priority. Must be > 0
   *
   * @param item the [RegexGroupModel]
   * @param value the new value
   */
  override fun setValue(item: RegexGroupModel, value: String) {
    val newValue = value.toIntOrNull()
    if (newValue != null) {
      item.touched = true
      item.priority = newValue
    }
  }

  /**
   * Creates an editor for the priority, with empty value validation
   *
   * @param item the [RegexGroupModel]
   * @return the [TableCellEditor]
   */
  override fun getEditor(item: RegexGroupModel): TableCellEditor {
    val cellEditor = JTextField()
    return StatefulValidatingCellEditor(cellEditor, parent)
  }

  /**
   * Creates a renderer for the priority with validation
   *
   * @param item the [RegexGroupModel]
   * @return the [TableCellRenderer]
   */
  override fun getRenderer(item: RegexGroupModel): TableCellRenderer? = ValidatingTableCellRendererWrapper(ModifiedInfoCellRenderer(item))
    .withCellValidator { value: Any?, _: Int, _: Int -> validate(value as String) }

  /**
   * Whether the cell is editable
   *
   * @param item the [RegexGroupModel]
   * @return true if editable
   */
  override fun isCellEditable(item: RegexGroupModel): Boolean = editable

  /**
   * Returns the relevant validation message
   *
   * @param value
   * @return
   */
  private fun validate(value: String?): ValidationInfo? = when {
    value == null     -> ValidationInfo(message("RegexGroupConfigurable.PriorityEditor.empty"))
    value.toInt() < 0 -> ValidationInfo(message("RegexGroupConfigurable.PriorityEditor.wrong"))
    else              -> null
  }

  /** Compare by priority for sorting. */
  override fun getComparator(): Comparator<RegexGroupModel> = Comparator.comparingInt { c: RegexGroupModel -> c.priority }

  /** Column width. */
  override fun getWidth(table: JTable?): Int = 70
}
