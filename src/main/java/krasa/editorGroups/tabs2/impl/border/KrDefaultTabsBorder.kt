package krasa.editorGroups.tabs2.impl.border

import krasa.editorGroups.tabs2.impl.KrTabsImpl
import java.awt.*

open class KrDefaultTabsBorder(tabs: KrTabsImpl) : KrTabsBorder(tabs) {
  override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
    if (tabs.isEmptyVisible) return
    g as Graphics2D

    val rect = Rectangle(x, y, width, height)
    val firstLabel = tabs.getTabLabel(tabs.getVisibleInfos().first()) ?: return
    val maxY = firstLabel.bounds.maxY.toInt() - thickness

    tabs.tabPainter.paintBorderLine(
      g = g,
      thickness = thickness,
      from = Point(rect.x, maxY),
      to = Point(rect.maxX.toInt(), maxY)
    )
  }
}
