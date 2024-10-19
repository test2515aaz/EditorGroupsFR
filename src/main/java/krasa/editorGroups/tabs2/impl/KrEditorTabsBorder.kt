package krasa.editorGroups.tabs2.impl

import com.intellij.openapi.rd.paint2DLine
import com.intellij.ui.NewUI
import com.intellij.ui.paint.LinePainter2D
import com.intellij.util.animation.Easing
import com.intellij.util.animation.JBAnimator
import com.intellij.util.animation.animation
import com.intellij.util.ui.JBUI
import krasa.editorGroups.tabs2.KrTabInfo
import krasa.editorGroups.tabs2.KrTabsBorder
import krasa.editorGroups.tabs2.KrTabsListener
import krasa.editorGroups.tabs2.KrTabsPosition
import krasa.editorGroups.tabs2.impl.painter.KrTabPainter
import java.awt.*

class KrEditorTabsBorder(tabs: KrTabsImpl) : KrTabsBorder(tabs) {
  private val animator = JBAnimator()
  private var start: Int = -1
  private var end: Int = -1
  private var animationId = -1L

  init {
    tabs.addListener(object : KrTabsListener {
      override fun selectionChanged(oldSelection: KrTabInfo?, newSelection: KrTabInfo?) {
        val from = bounds(oldSelection) ?: return
        val to = bounds(newSelection) ?: return
        var duration = DURATION
        var delay = DELAY

        val start1 = from.x
        val start2 = to.x
        val delta1 = if (start1 > start2) 0 else delay

        val end1 = from.x + from.width
        val end2 = to.x + to.width
        val delta2 = if (end1 > end2) delay else 0

        animationId = animator.animate(
          animation(start1, start2) {
            start = it
            tabs.component.repaint()
          }.apply {
            duration = duration - delta1
            delay = delta1
            easing = if (delta1 != 0) Easing.EASE_OUT else Easing.LINEAR
          },

          animation(end1, end2) {
            end = it
            tabs.component.repaint()
          }.apply {
            duration = duration - delta2
            delay = delta2
            easing = if (delta2 != 0) Easing.EASE_OUT else Easing.LINEAR
          }
        )
      }

      private fun bounds(tabInfo: KrTabInfo?): Rectangle? {
        return tabs.infoToLabel.get(tabInfo ?: return null)?.bounds
      }
    })
  }

  override val effectiveBorder: Insets
    get() = JBUI.insetsTop(thickness)

  override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
    g as Graphics2D

    if (NewUI.isEnabled()) {
      g.paint2DLine(
        from = Point(x, y),
        to = Point(x + width, y),
        strokeType = LinePainter2D.StrokeType.INSIDE,
        strokeWidth = thickness.toDouble(),
        color = JBUI.CurrentTheme.EditorTabs.borderColor()
      )
    } else {
      KrTabPainter.paintBorderLine(
        g = g,
        thickness = thickness,
        from = Point(x, y),
        to = Point(x + width, y)
      )
    }

    if (tabs.isEmptyVisible || tabs.isHideTabs) return

    val myInfo2Label = tabs.infoToLabel
    val firstLabel = myInfo2Label[tabs.getVisibleInfos()[0]] ?: return

    when (tabs.position) {
      KrTabsPosition.top -> {
        val highlightThickness = if (tabs.position == KrTabsPosition.bottom) 0 else thickness
        val startY = firstLabel.y - highlightThickness
        val startRow = if (NewUI.isEnabled()) 1 else 0
        val lastRow = tabs.lastLayoutPass!!.rowCount

        for (eachRow in startRow until lastRow) {
          val yl = eachRow * tabs.headerFitSize!!.height + startY
          KrTabPainter.paintBorderLine(g, thickness, Point(x, yl), Point(x + width, yl))
        }

        if (!NewUI.isEnabled() || (tabs as? KrEditorTabs)?.shouldPaintBottomBorder() == true) {
          val yl = lastRow * tabs.headerFitSize!!.height + startY
          KrTabPainter.paintBorderLine(g, thickness, Point(x, yl), Point(x + width, yl))
        }
      }

      KrTabsPosition.bottom -> {
        val rowCount = tabs.lastLayoutPass!!.rowCount
        for (rowInd in 0 until rowCount) {
          val curY = height - (rowInd + 1) * tabs.headerFitSize!!.height
          KrTabPainter.paintBorderLine(g, thickness, Point(x, curY), Point(x + width, curY))
        }
      }

      KrTabsPosition.right -> {
        val lx = firstLabel.x
        KrTabPainter.paintBorderLine(g, thickness, Point(lx, y), Point(lx, y + height))
      }

      KrTabsPosition.left -> {
        val bounds = firstLabel.bounds
        val i = bounds.x + bounds.width - thickness
        KrTabPainter.paintBorderLine(g, thickness, Point(i, y), Point(i, y + height))
      }
    }

    if (hasAnimation()) {
      KrTabPainter.paintUnderline(
        tabs.position,
        calcRectangle() ?: return,
        thickness,
        g,
        tabs.isActiveTabs(tabs.selectedInfo)
      )
    } else {
      val selectedLabel = tabs.selectedLabel ?: return
      KrTabPainter.paintUnderline(
        tabs.position,
        selectedLabel.bounds,
        thickness,
        g,
        tabs.isActiveTabs(tabs.selectedInfo)
      )
    }
  }

  private fun calcRectangle(): Rectangle? {
    val selectedLabel = tabs.selectedLabel ?: return null
    if (animator.isRunning(animationId)) return Rectangle(start, selectedLabel.y, end - start, selectedLabel.height)
    return selectedLabel.bounds
  }

  companion object {
    const val DURATION = 100
    const val DELAY = 50
    internal fun hasAnimation(): Boolean = true
  }
}
