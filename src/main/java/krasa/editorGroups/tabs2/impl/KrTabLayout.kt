// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.ui.JBUI
import krasa.editorGroups.tabs2.label.EditorGroupTabInfo
import java.awt.Rectangle

abstract class KrTabLayout {
  open val isSideComponentOnTabs: Boolean
    get() = false

  open val isScrollable: Boolean
    get() = true

  open val isWithScrollBar: Boolean
    get() = true

  open val scrollOffset: Int
    get() = 0

  open fun createShapeTransform(rectangle: Rectangle?): KrShapeTransform = KrShapeTransform.Top(rectangle)

  open fun scroll(units: Int): Unit = Unit

  open fun isTabHidden(info: EditorGroupTabInfo): Boolean = false

  companion object {

    @JvmStatic
    val maxPinnedTabWidth: Int
      get() = JBUI.scale(Registry.intValue("ide.editor.max.pinned.tab.width", 2000))

    @JvmStatic
    protected val minTabWidth: Int
      get() = JBUI.scale(50)

    const val DEADZONE_FOR_DECLARE_TAB_HIDDEN: Int = 10

    @JvmStatic
    fun showPinnedTabsSeparately(): Boolean {
      return UISettings.getInstance().state.showPinnedTabsInASeparateRow &&
        AdvancedSettings.getBoolean("editor.keep.pinned.tabs.on.left")
    }
  }
}
