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

import com.intellij.util.ui.table.TableModelEditor.EditableColumnInfo
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.model.RegexGroupModel
import javax.swing.DefaultCellEditor
import javax.swing.JCheckBox
import javax.swing.JTable
import javax.swing.table.TableCellEditor

/** Column info for the touched state. */
class TouchedColumnInfo :
  EditableColumnInfo<RegexGroupModel, Boolean>(message("RegexEditorConfigurable.columns.touched")) {

  /** Column class is [Boolean]. */
  override fun getColumnClass(): Class<Boolean> = Boolean::class.java

  /** Compare with touched state for putting touched at the top. */
  override fun getComparator(): Comparator<RegexGroupModel>? = Comparator.comparing { c: RegexGroupModel -> c.touched }

  /** Set touched state as a checkbox (hidden) */
  override fun getEditor(item: RegexGroupModel): TableCellEditor = DefaultCellEditor(JCheckBox())

  /** No name for this column. */
  override fun getName(): String = ""

  /** Hide this column by setting this width to 1. */
  override fun getWidth(table: JTable?): Int = 1

  /** Set touched state. */
  override fun setValue(item: RegexGroupModel, value: Boolean) {
    item.touched = value
  }

  /** Touched state of the [RegexGroupModel]. */
  override fun valueOf(item: RegexGroupModel): Boolean = item.touched
}
