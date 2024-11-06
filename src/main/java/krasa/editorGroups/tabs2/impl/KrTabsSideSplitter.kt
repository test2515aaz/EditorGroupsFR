// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl

import com.intellij.openapi.ui.OnePixelDivider
import com.intellij.openapi.ui.Splittable
import com.intellij.ui.ClientProperty
import java.awt.Component
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import kotlin.math.max
import kotlin.math.min

internal class KrTabsSideSplitter(private val myTabs: KrTabsImpl) : Splittable, PropertyChangeListener {
  private var mySideTabsLimit = KrTabsImpl.DEFAULT_MAX_TAB_WIDTH
  private var myDragging = false
  val divider: OnePixelDivider

  var sideTabsLimit: Int
    get() = mySideTabsLimit
    set(sideTabsLimit) {
      if (mySideTabsLimit != sideTabsLimit) {
        mySideTabsLimit = sideTabsLimit
        myTabs.putClientProperty(KrTabsImpl.SIDE_TABS_SIZE_LIMIT_KEY, mySideTabsLimit)
        myTabs.resetLayout(true)
        myTabs.doLayout()
        myTabs.repaint()

        val info = myTabs.selectedInfo
        val page = info?.component
        if (page != null) {
          page.revalidate()
          page.repaint()
        }
      }
    }

  init {
    myTabs.addPropertyChangeListener(KrTabsImpl.SIDE_TABS_SIZE_LIMIT_KEY.toString(), this)
    divider = OnePixelDivider(false, this)
  }

  override fun getMinProportion(first: Boolean): Float {
    return min(
      0.5,
      (KrTabsImpl.MIN_TAB_WIDTH.toFloat() / max(1.0, myTabs.width.toDouble())).toDouble()
    ).toFloat()
  }

  override fun setProportion(proportion: Float) {
    val width = myTabs.width
    sideTabsLimit = width
  }

  override fun getOrientation(): Boolean = false

  override fun setOrientation(verticalSplit: Boolean) = Unit

  override fun setDragging(dragging: Boolean) {
    myDragging = dragging
  }

  fun isDragging(): Boolean = myDragging

  override fun asComponent(): Component = myTabs

  override fun propertyChange(evt: PropertyChangeEvent) {
    if (evt.source !== myTabs) return
    var limit = ClientProperty.get(myTabs, KrTabsImpl.SIDE_TABS_SIZE_LIMIT_KEY)
    if (limit == null) limit = KrTabsImpl.DEFAULT_MAX_TAB_WIDTH
    sideTabsLimit = limit
  }
}
