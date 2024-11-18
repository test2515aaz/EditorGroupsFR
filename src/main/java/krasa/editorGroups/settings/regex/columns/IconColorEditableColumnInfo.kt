/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2023 Elior "Mallowigi" Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package krasa.editorGroups.settings.regex.columns

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.cellvalidators.StatefulValidatingCellEditor
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.util.ui.ColumnInfo
import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.model.RegexGroupModel
import krasa.editorGroups.support.toHex
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

/** Column info for the icon of a **File Icon Association**. Displays the icon path alongside the icon. */
class IconColorEditableColumnInfo(private val parent: Disposable) :
  ColumnInfo<RegexGroupModel, String>(message("RegexEditorConfigurable.columns.color")) {

  /** Returns the value to display in the column. */
  override fun valueOf(item: RegexGroupModel): String = item.myColor.toHex()

  /**
   * Set column value (sets the color)
   *
   * @param item [RegexGroupModel] to set
   * @param value the color
   */
  override fun setValue(item: RegexGroupModel, value: String?) {
    if (value != null) {
      item.touched = true
      item.color = value
    }
  }

  /** Returns the renderer for the column. */
  override fun getRenderer(item: RegexGroupModel): TableCellRenderer = object : DefaultTableCellRenderer() {
    override fun repaint() {
      background = item.myColor
      foreground = when (ColorUtil.isDark(background)) {
        true  -> JBColor.WHITE
        false -> JBColor.BLACK
      }
    }
  }

  /** Returns the editor for the column. */
  override fun getEditor(item: RegexGroupModel): TableCellEditor {
    val cellEditor = ExtendableTextField()
    return StatefulValidatingCellEditor(cellEditor, parent)
  }

  /** Is the column editable? */
  override fun isCellEditable(item: RegexGroupModel): Boolean = false

  /** Column width. */
  override fun getWidth(table: JTable?): Int = 50

  /** Needed. */
  override fun getComparator(): Comparator<RegexGroupModel> = Comparator.comparingInt { c: RegexGroupModel -> c.priority }
}
