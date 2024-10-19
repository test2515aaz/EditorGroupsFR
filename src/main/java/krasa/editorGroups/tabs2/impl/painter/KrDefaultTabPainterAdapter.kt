package krasa.editorGroups.tabs2.impl.painter

import krasa.editorGroups.tabs2.KrTabsPosition
import krasa.editorGroups.tabs2.impl.KrTabLabel
import krasa.editorGroups.tabs2.impl.KrTabsImpl
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle

/** Holds a tabPainter to paint the tab background. */
class KrDefaultTabPainterAdapter() : KrTabPainterAdapter {
  override val tabPainter: KrTabPainter
    get() = KrTabPainter.DEFAULT

  private val magicOffset = 1

  override fun paintBackground(label: KrTabLabel, g: Graphics, tabs: KrTabsImpl) {
    val info = label.info
    val isSelected = info == tabs.selectedInfo
    val isHovered = tabs.isHoveredTab(label)

    val rect = Rectangle(0, 0, label.width, label.height)
    val g2d = g as Graphics2D

    when {
      isSelected -> {
        tabPainter.paintSelectedTab(
          position = tabs.position,
          g = g2d,
          rect = rect,
          borderThickness = tabs.borderThickness,
          tabColor = info.tabColor,
          active = tabs.isActiveTabs(info),
          hovered = isHovered
        )
      }

      else       -> {
        if (isHovered && tabs.tabsPosition == KrTabsPosition.top) rect.height -= magicOffset

        tabPainter.paintTab(
          position = tabs.position,
          g = g2d,
          rect = rect,
          borderThickness = tabs.borderThickness,
          tabColor = info.tabColor,
          active = tabs.isActiveTabs(info),
          hovered = isHovered
        )
      }
    }
  }

}
