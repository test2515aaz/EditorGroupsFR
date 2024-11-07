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

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.ui.Queryable
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsContexts.TabTitle
import com.intellij.reference.SoftReference
import com.intellij.ui.PlaceProvider
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.content.AlertIcon
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.event.MouseEvent
import java.beans.PropertyChangeSupport
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import javax.swing.Icon
import javax.swing.JComponent

open class EditorGroupTabInfo(var component: JComponent? = null) : Queryable, PlaceProvider {
  val changeSupport: PropertyChangeSupport = PropertyChangeSupport(this)

  /** The tab icon. */
  var icon: Icon? = null
    private set

  private var myLastFocusOwner: Reference<JComponent?>? = null

  var tabLabelActions: ActionGroup? = null
    private set

  var tabPaneActions: ActionGroup? = null
    private set

  var tabActionPlace: String? = null
    private set

  var blinkCount: Int = 0

  var isAlertRequested: Boolean = false
    private set

  private var myHidden = false

  val coloredText: SimpleColoredText = SimpleColoredText()

  var tooltipText: @NlsContexts.Tooltip String? = null
    private set

  private var myDefaultStyle = -1

  var defaultForeground: Color? = null
    private set

  private var editorAttributes: TextAttributes? = null

  private var myDefaultAttributes: SimpleTextAttributes? = null

  private var myEnabled = true

  var tabColor: Color? = null
    private set

  private var myQueryable: Queryable? = null

  var dragOutDelegate: DragOutDelegate? = null
    private set

  val text: @TabTitle String
    get() = coloredText.toString()

  var dragDelegate: DragDelegate? = null

  val alertIcon: AlertIcon
    get() = DEFAULT_ALERT_ICON

  /**
   * The tab which was selected before the mouse was pressed on this tab. Focus will be transferred to that tab if this tab is dragged out
   * of its container. (IDEA-61536)
   */
  private var myPreviousSelection = WeakReference<EditorGroupTabInfo?>(null)

  fun setText(text: @TabTitle String): EditorGroupTabInfo {
    val attributes: MutableList<SimpleTextAttributes?> = coloredText.getAttributes()
    val textAttributes = if (attributes.size == 1) attributes.get(0)!!.toTextAttributes() else null
    val defaultAttributes = this.defaultAttributes
    if (coloredText.toString() != text || !Comparing.equal<TextAttributes?>(textAttributes, defaultAttributes.toTextAttributes())) {
      clearText(false)
      append(text, defaultAttributes)
    }
    return this
  }

  private val defaultAttributes: SimpleTextAttributes
    get() {
      if (myDefaultAttributes == null) {
        val style = ((if (myDefaultStyle != -1) myDefaultStyle else SimpleTextAttributes.STYLE_PLAIN)
          or SimpleTextAttributes.STYLE_USE_EFFECT_COLOR)
        if (editorAttributes != null) {
          var attr = SimpleTextAttributes.fromTextAttributes(editorAttributes)
          attr = SimpleTextAttributes.merge(SimpleTextAttributes(style, this.defaultForeground), attr)
          myDefaultAttributes = attr
        } else {
          myDefaultAttributes = SimpleTextAttributes(style, this.defaultForeground)
        }
      }
      return myDefaultAttributes!!
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

  fun setIcon(icon: Icon?): EditorGroupTabInfo {
    val old = this.icon
    if (old != icon) {
      this.icon = icon
      changeSupport.firePropertyChange(ICON, old, icon)
    }
    return this
  }

  override fun getPlace(): String? = null

  var lastFocusOwner: JComponent?
    get() = SoftReference.dereference<JComponent?>(myLastFocusOwner)
    set(owner) {
      myLastFocusOwner = if (owner == null) null else WeakReference<JComponent?>(owner)
    }

  fun fireAlert() {
    this.isAlertRequested = true
    changeSupport.firePropertyChange(ALERT_STATUS, null, true)
  }

  fun stopAlerting() {
    this.isAlertRequested = false
    changeSupport.firePropertyChange(ALERT_STATUS, null, false)
  }

  override fun toString(): String = this.text

  fun resetAlertRequest() {
    this.isAlertRequested = false
  }

  var isHidden: Boolean
    get() = myHidden
    set(hidden) {
      val old = myHidden
      myHidden = hidden
      changeSupport.firePropertyChange(HIDDEN, old, myHidden)
    }

  var isEnabled: Boolean
    get() = myEnabled
    set(enabled) {
      val old = myEnabled
      myEnabled = enabled
      changeSupport.firePropertyChange(ENABLED, old, myEnabled)
    }

  fun setDefaultForeground(fg: Color?): EditorGroupTabInfo {
    this.defaultForeground = fg
    myDefaultAttributes = null
    update()
    return this
  }

  private fun update() {
    setText(this.text)
  }

  fun revalidate() {
    myDefaultAttributes = null
    update()
  }

  fun setTooltipText(text: @NlsContexts.Tooltip String?): EditorGroupTabInfo {
    val old = this.tooltipText
    if (old != text) {
      this.tooltipText = text
      changeSupport.firePropertyChange(TEXT, old, this.tooltipText)
    }
    return this
  }

  fun setTabColor(color: Color?): EditorGroupTabInfo {
    val old = this.tabColor
    if (!Comparing.equal<Color?>(color, old)) {
      this.tabColor = color
      changeSupport.firePropertyChange(TAB_COLOR, old, color)
    }
    return this
  }

  override fun putInfo(info: MutableMap<in String, in String>) {
    if (myQueryable != null) {
      myQueryable!!.putInfo(info)
    }
  }

  var previousSelection: EditorGroupTabInfo?
    get() = myPreviousSelection.get()
    set(previousSelection) {
      myPreviousSelection = WeakReference<EditorGroupTabInfo?>(previousSelection)
    }

  interface DragDelegate {
    fun dragStarted(mouseEvent: MouseEvent)

    fun dragFinishedOrCanceled()
  }

  interface DragOutDelegate {
    fun dragOutStarted(mouseEvent: MouseEvent, info: EditorGroupTabInfo)

    fun processDragOut(event: MouseEvent, source: EditorGroupTabInfo)

    fun dragOutFinished(event: MouseEvent, source: EditorGroupTabInfo?)

    fun dragOutCancelled(source: EditorGroupTabInfo?)
  }

  companion object {
    const val ACTION_GROUP: String = "actionGroup"
    const val ICON: String = "icon"
    const val TAB_COLOR: String = "color"
    const val COMPONENT: String = "component"
    const val TEXT: String = "text"
    const val TAB_ACTION_GROUP: String = "tabActionGroup"

    const val ALERT_STATUS: String = "alertStatus"
    const val HIDDEN: String = "hidden"
    const val ENABLED: String = "enabled"

    private val DEFAULT_ALERT_ICON = AlertIcon(AllIcons.Nodes.TabAlert, 0, -JBUI.scale(6))
  }
}
