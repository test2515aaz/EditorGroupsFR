// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle

class KrDefaultEditorTabsPainter(tabs: KrEditorTabs) : KrEditorTabsPainter(tabs) {
  protected val defaultTabColor: Color
    get() = myDefaultTabColor ?: DEFAULT_TAB_COLOR

  protected val inactiveMaskColor: Color = INACTIVE_MASK_COLOR

  override val backgroundColor: Color = BORDER_COLOR

  override fun doPaintInactive(
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
  ) {
    g2d.color = tabColor
    g2d.fillRect(x, y, w, h)
    g2d.color = inactiveMaskColor
    g2d.fillRect(x, y, w, h)
  }

  override fun doPaintBackground(g: Graphics2D, clip: Rectangle, vertical: Boolean, rectangle: Rectangle) {
    g.color = backgroundColor
    g.fill(clip)
  }
}
