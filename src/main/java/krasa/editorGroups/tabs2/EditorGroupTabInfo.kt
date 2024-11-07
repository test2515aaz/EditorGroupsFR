/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package krasa.editorGroups.tabs2

import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsContexts.TabTitle
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.StyleAttributeConstant
import krasa.editorGroups.tabs2.impl.KrTabLabel
import java.awt.Color
import java.beans.PropertyChangeSupport
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import javax.swing.Icon
import javax.swing.JComponent

open class EditorGroupTabInfo(var component: JComponent? = null) {
  val changeSupport: PropertyChangeSupport = PropertyChangeSupport(this)

  // Internal tab label
  var tabLabel: KrTabLabel? = null

  /** The tab icon. */
  var icon: Icon? = null
    set(icon) {
      val old = field
      if (old != icon) {
        field = icon
        changeSupport.firePropertyChange(ICON, old, icon)
      }
    }

  /** hidden state */
  var isHidden: Boolean = false
    set(hidden) {
      val old = field
      field = hidden
      changeSupport.firePropertyChange(HIDDEN, old, field)
    }

  /** enabled state */
  var isEnabled: Boolean = true
    set(enabled) {
      val old = field
      field = enabled
      changeSupport.firePropertyChange(ENABLED, old, field)
    }

  private var lastFocusOwnerRef: Reference<JComponent>? = null

  /** Last focus owner. */
  var lastFocusOwner: JComponent?
    get() = lastFocusOwnerRef?.get()
    set(value) {
      lastFocusOwnerRef = value?.let { WeakReference(it) }
    }

  val coloredText: SimpleColoredText = SimpleColoredText()

  /** Text. */
  var text: @TabTitle String
    get() = coloredText.toString()
    set(text) {
      val attributes: MutableList<SimpleTextAttributes?> = coloredText.attributes
      val textAttributes = attributes.singleOrNull()?.toTextAttributes()
      val defaultAttributes = getDefaultAttributes()

      if (coloredText.toString() != text || textAttributes != defaultAttributes.toTextAttributes()) {
        clearText(false)
        append(text, defaultAttributes)
      }
    }

  /** Tooltip text. */
  var tooltipText: @NlsContexts.Tooltip String? = null
    set(text) {
      val old = field
      if (old != text) {
        field = text
        changeSupport.firePropertyChange(TEXT, old, text)
      }
    }

  /** Tab background color. */
  var tabColor: Color? = null
    set(color) {
      val old = field
      if (color != old) {
        field = color
        changeSupport.firePropertyChange(TAB_COLOR, old, color)
      }
    }

  /** The tab's default foreground. */
  private var defaultForeground: Color? = null

  /** The attributes containing the foreground. */
  private var defaultAttributes: SimpleTextAttributes? = null

  /** Custom editor attributes. */
  private var editorAttributes: TextAttributes? = null

  @StyleAttributeConstant
  private var defaultStyle: Int = -1

  /** Compute default attributes. */
  private fun getDefaultAttributes(): SimpleTextAttributes {
    if (defaultAttributes != null) return defaultAttributes!!

    val style = ((when (defaultStyle) {
      -1   -> SimpleTextAttributes.STYLE_PLAIN
      else -> defaultStyle
    }) or SimpleTextAttributes.STYLE_USE_EFFECT_COLOR)

    when (editorAttributes) {
      null -> defaultAttributes = SimpleTextAttributes(style, defaultForeground)
      else -> {
        val attr = SimpleTextAttributes.fromTextAttributes(editorAttributes)
        defaultAttributes = SimpleTextAttributes.merge(SimpleTextAttributes(style, defaultForeground), attr)
      }
    }

    return defaultAttributes!!
  }

  fun getFontSize(): Int = when (tabLabel) {
    null -> 0
    else -> tabLabel!!.font.size
  }

  fun clearText(invalidate: Boolean): EditorGroupTabInfo {
    val old = coloredText.toString()
    coloredText.clear()
    if (invalidate) {
      changeSupport.firePropertyChange(TEXT, old, coloredText.toString())
    }
    return this
  }

  fun append(fragment: @NlsContexts.Label String, attributes: SimpleTextAttributes): EditorGroupTabInfo {
    val old = coloredText.toString()
    coloredText.append(fragment, attributes)
    changeSupport.firePropertyChange(TEXT, old, coloredText.toString())
    return this
  }

  fun setDefaultStyle(@StyleAttributeConstant style: Int): EditorGroupTabInfo {
    defaultStyle = style
    defaultAttributes = null
    update()
    return this
  }

  fun setDefaultForeground(fg: Color?): EditorGroupTabInfo {
    defaultForeground = fg
    defaultAttributes = null
    update()
    return this
  }

  fun setDefaultForegroundAndAttributes(foregroundColor: Color?, attributes: TextAttributes?): EditorGroupTabInfo {
    defaultForeground = foregroundColor
    editorAttributes = attributes
    defaultAttributes = null
    update()
    return this
  }

  fun revalidate() {
    defaultAttributes = null
    update()
  }

  private fun update() {
    this.text = this.text
  }

  override fun toString(): String = this.text

  companion object {
    const val ACTION_GROUP: String = "actionGroup"
    const val ICON: String = "icon"
    const val TAB_COLOR: String = "color"
    const val COMPONENT: String = "component"
    const val TEXT: String = "text"

    const val HIDDEN: String = "hidden"
    const val ENABLED: String = "enabled"

  }
}
