// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl

import com.intellij.ui.ColorUtil
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle

abstract class KrEditorTabsPainter(protected val myTabs: KrEditorTabs) {

  protected var myDefaultTabColor: Color? = null
  abstract val backgroundColor: Color?

  val emptySpaceColor: Color
    get() = UIUtil.getPanelBackground()

  abstract fun doPaintInactive(
    g2d: Graphics2D,
    effectiveBounds: Rectangle,
    x: Int,
    y: Int,
    w: Int,
    h: Int,
    tabColor: Color?,
    row: Int,
    column: Int,
    vertical: Boolean
  )

  abstract fun doPaintBackground(g: Graphics2D, clip: Rectangle, vertical: Boolean, rectangle: Rectangle)

  fun setDefaultTabColor(color: Color?) {
    myDefaultTabColor = color
  }

  companion object {
    @JvmStatic
    protected val BORDER_COLOR: Color = JBColor.namedColor("EditorTabs.borderColor", UIUtil.CONTRAST_BORDER_COLOR)

    @JvmStatic
    protected val UNDERLINE_COLOR: Color = JBColor.namedColor("EditorTabs.underlineColor", 0x439EB8)

    @JvmStatic
    protected val DEFAULT_TAB_COLOR: Color = JBColor.namedColor("EditorTabs.selectedBackground", JBColor(0xFFFFFF, 0x515658))

    @JvmStatic
    protected val INACTIVE_MASK_COLOR: Color = JBColor.namedColor(
      "EditorTabs.inactiveMaskColor",
      JBColor(
        ColorUtil.withAlpha(Gray.x26, .2),
        ColorUtil.withAlpha(Gray.x26, .5)
      )
    )
  }
}
