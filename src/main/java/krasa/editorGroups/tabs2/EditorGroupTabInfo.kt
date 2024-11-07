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
import com.intellij.ui.ClientProperty
import com.intellij.ui.PlaceProvider
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.SimpleTextAttributes.StyleAttributeConstant
import com.intellij.ui.content.AlertIcon
import com.intellij.util.ui.JBUI
import krasa.editorGroups.tabs2.impl.KrTabsImpl
import org.jetbrains.annotations.NonNls
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import java.beans.PropertyChangeSupport
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import javax.swing.Icon
import javax.swing.JComponent

open class EditorGroupTabInfo(var component: JComponent?) : Queryable, PlaceProvider {
  private var myPreferredFocusableComponent: JComponent?

  var group: ActionGroup? = null
    private set

  val changeSupport: PropertyChangeSupport = PropertyChangeSupport(this)

  var icon: Icon? = null
    private set

  private var myPlace: @NonNls String? = null

  var `object`: Any? = null
    private set
  var sideComponent: JComponent? = null
    private set
  private var myLastFocusOwner: Reference<JComponent?>? = null

  var tabLabelActions: ActionGroup? = null
    private set

  var tabPaneActions: ActionGroup? = null
    private set

  var tabActionPlace: String? = null
    private set

  private var myAlertIcon: AlertIcon? = null

  var blinkCount: Int = 0

  var isAlertRequested: Boolean = false
    private set

  private var myHidden = false

  var actionsContextComponent: JComponent? = null
    private set

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

  var dragDelegate: DragDelegate? = null

  /**
   * The tab which was selected before the mouse was pressed on this tab. Focus will be transferred to that tab if this tab is dragged out
   * of its container. (IDEA-61536)
   */
  private var myPreviousSelection = WeakReference<EditorGroupTabInfo?>(null)

  init {
    myPreferredFocusableComponent = component
  }

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

  fun setComponent(c: Component?): EditorGroupTabInfo {
    if (this.component !== c) {
      val old = this.component
      this.component = c as JComponent?
      changeSupport.firePropertyChange(COMPONENT, old, this.component)
    }
    return this
  }

  val isPinned: Boolean
    get() = ClientProperty.isTrue(this.component, KrTabsImpl.PINNED)

  val text: @TabTitle String
    get() = coloredText.toString()

  override fun getPlace(): String? {
    return myPlace
  }

  fun setSideComponent(comp: JComponent?): EditorGroupTabInfo {
    this.sideComponent = comp
    return this
  }

  fun setActions(group: ActionGroup?, place: @NonNls String?): EditorGroupTabInfo {
    val old = this.group
    this.group = group
    myPlace = place
    changeSupport.firePropertyChange(ACTION_GROUP, old, this.group)
    return this
  }

  fun setActionsContextComponent(c: JComponent?): EditorGroupTabInfo {
    this.actionsContextComponent = c
    return this
  }

  fun setObject(`object`: Any?): EditorGroupTabInfo {
    this.`object` = `object`
    return this
  }

  val preferredFocusableComponent: JComponent?
    get() = if (myPreferredFocusableComponent != null) myPreferredFocusableComponent else this.component

  fun setPreferredFocusableComponent(component: JComponent?): EditorGroupTabInfo {
    myPreferredFocusableComponent = component
    return this
  }

  fun setTabLabelActions(tabActions: ActionGroup?, place: String): EditorGroupTabInfo {
    val old = this.tabLabelActions
    this.tabLabelActions = tabActions
    this.tabActionPlace = place
    changeSupport.firePropertyChange(TAB_ACTION_GROUP, old, this.tabLabelActions)
    return this
  }

  /** Sets the actions that will be displayed on the right side of the tabs. */
  fun setTabPaneActions(tabPaneActions: ActionGroup?): EditorGroupTabInfo {
    this.tabPaneActions = tabPaneActions
    return this
  }

  var lastFocusOwner: JComponent?
    get() = SoftReference.dereference<JComponent?>(myLastFocusOwner)
    set(owner) {
      myLastFocusOwner = if (owner == null) null else WeakReference<JComponent?>(owner)
    }

  fun setAlertIcon(alertIcon: AlertIcon?): EditorGroupTabInfo {
    val old = myAlertIcon
    myAlertIcon = alertIcon
    changeSupport.firePropertyChange(ALERT_ICON, old, myAlertIcon)
    return this
  }

  fun fireAlert() {
    this.isAlertRequested = true
    changeSupport.firePropertyChange(ALERT_STATUS, null, true)
  }

  fun stopAlerting() {
    this.isAlertRequested = false
    changeSupport.firePropertyChange(ALERT_STATUS, null, false)
  }

  override fun toString(): String {
    return this.text
  }

  val alertIcon: AlertIcon
    get() = (if (myAlertIcon == null) EditorGroupTabInfo.Companion.DEFAULT_ALERT_ICON else myAlertIcon)!!

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

  fun setDefaultStyle(@StyleAttributeConstant style: Int): EditorGroupTabInfo {
    myDefaultStyle = style
    myDefaultAttributes = null
    update()
    return this
  }

  fun setDefaultForeground(fg: Color?): EditorGroupTabInfo {
    this.defaultForeground = fg
    myDefaultAttributes = null
    update()
    return this
  }

  fun setDefaultAttributes(attributes: TextAttributes?): EditorGroupTabInfo {
    editorAttributes = attributes
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

  fun setTestableUi(queryable: Queryable?): EditorGroupTabInfo {
    myQueryable = queryable
    return this
  }

  override fun putInfo(info: MutableMap<in String, in String>) {
    if (myQueryable != null) {
      myQueryable!!.putInfo(info)
    }
  }

  fun setDragOutDelegate(delegate: DragOutDelegate?): EditorGroupTabInfo {
    this.dragOutDelegate = delegate
    return this
  }

  fun canBeDraggedOut(): Boolean {
    return this.dragOutDelegate != null
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
    const val ALERT_ICON: String = "alertIcon"

    const val ALERT_STATUS: String = "alertStatus"
    const val HIDDEN: String = "hidden"
    const val ENABLED: String = "enabled"

    private val DEFAULT_ALERT_ICON = AlertIcon(AllIcons.Nodes.TabAlert, 0, -JBUI.scale(6))
  }
}
