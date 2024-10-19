package krasa.editorGroups.tabs2.impl

import com.intellij.util.ui.JBUI
import krasa.editorGroups.tabs2.KrTabsBorder
import krasa.editorGroups.tabs2.KrTabsPosition
import java.awt.*

class KrEditorTabsBorder(tabs: KrTabsImpl) : KrTabsBorder(tabs) {
  override val effectiveBorder: Insets
    get() = JBUI.insetsTop(thickness)

  override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
    g as Graphics2D
    if (tabs.isEmptyVisible || tabs.isHideTabs) return

    val firstLabel = tabs.getTabLabel(tabs.getVisibleInfos().first()) ?: return

    when (tabs.position) {
      KrTabsPosition.top    -> {
        val highlightThickness = thickness
        val startY = firstLabel.y - highlightThickness
        val startRow = 1
        val lastRow = tabs.lastLayoutPass!!.rowCount

        for (eachRow in startRow until lastRow) {
          val yl = eachRow * tabs.headerFitSize!!.height + startY
          tabs.tabPainter.paintBorderLine(g, thickness, Point(x, yl), Point(x + width, yl))
        }

        if ((tabs as? KrEditorTabs)?.shouldPaintBottomBorder() == true) {
          val yl = lastRow * tabs.headerFitSize!!.height + startY
          tabs.tabPainter.paintBorderLine(g, thickness, Point(x, yl), Point(x + width, yl))
        }
      }

      KrTabsPosition.bottom -> {
        val rowCount = tabs.lastLayoutPass!!.rowCount
        for (rowInd in 0 until rowCount) {
          val curY = height - (rowInd + 1) * tabs.headerFitSize!!.height
          tabs.tabPainter.paintBorderLine(g, thickness, Point(x, curY), Point(x + width, curY))
        }
      }

      else                  -> return
    }

    val selectedLabel = tabs.selectedLabel ?: return
    tabs.tabPainter.paintUnderline(
      position = tabs.position,
      rect = selectedLabel.bounds,
      borderThickness = thickness,
      g = g,
      active = tabs.isActiveTabs(tabs.selectedInfo)
    )
  }
}
