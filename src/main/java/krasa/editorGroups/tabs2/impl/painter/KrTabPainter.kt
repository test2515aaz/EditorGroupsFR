package krasa.editorGroups.tabs2.impl.painter

import krasa.editorGroups.tabs2.EditorGroupsTabsPosition
import krasa.editorGroups.tabs2.impl.themes.EditorGroupCustomTabTheme
import krasa.editorGroups.tabs2.impl.themes.EditorGroupTabTheme
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle

interface KrTabPainter {
  companion object {
    @JvmStatic
    val DEFAULT = KrDefaultTabPainter(EditorGroupCustomTabTheme())
  }

  fun getTabTheme(): EditorGroupTabTheme

  fun getBackgroundColor(): Color

  /** Color that should be painted on top of [EditorGroupTabTheme.background]. */
  fun getCustomBackground(
    tabColor: Color?,
    selected: Boolean,
    active: Boolean,
    hovered: Boolean
  ): Color? = tabColor

  fun paintBorderLine(g: Graphics2D, thickness: Int, from: Point, to: Point)

  fun fillBackground(g: Graphics2D, rect: Rectangle)

  fun paintTab(
    position: EditorGroupsTabsPosition,
    g: Graphics2D,
    rect: Rectangle,
    borderThickness: Int,
    tabColor: Color?,
    active: Boolean,
    hovered: Boolean
  )

  fun paintSelectedTab(
    position: EditorGroupsTabsPosition,
    g: Graphics2D,
    rect: Rectangle,
    borderThickness: Int,
    tabColor: Color?,
    active: Boolean,
    hovered: Boolean
  )

  fun paintUnderline(
    position: EditorGroupsTabsPosition,
    rect: Rectangle,
    borderThickness: Int,
    g: Graphics2D,
    active: Boolean
  )
}
