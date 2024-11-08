package krasa.editorGroups.tabs2.impl.singleRow

import com.intellij.util.ui.JBUI
import krasa.editorGroups.tabs2.impl.KrShapeTransform
import krasa.editorGroups.tabs2.impl.KrTabsImpl
import java.awt.Dimension
import java.awt.Rectangle
import kotlin.math.max
import kotlin.math.sign

abstract class KrSingleRowLayoutStrategy protected constructor(myLayout: KrSingleRowLayout) {
  val myTabs: KrTabsImpl = myLayout.tabs

  abstract val moreRectAxisSize: Int

  abstract val entryPointAxisSize: Int

  abstract val additionalLength: Int

  abstract val isToCenterTextWhenStretched: Boolean

  abstract fun getStartPosition(data: EditorGroupsSingleRowPassInfo): Int

  abstract fun getToFitLength(data: EditorGroupsSingleRowPassInfo?): Int

  abstract fun getLengthIncrement(dimension: Dimension): Int

  abstract fun getMinPosition(bounds: Rectangle): Int

  abstract fun getMaxPosition(bounds: Rectangle): Int

  protected abstract fun getFixedFitLength(data: EditorGroupsSingleRowPassInfo?): Int

  fun getLayoutRect(data: EditorGroupsSingleRowPassInfo, position: Int, length: Int): Rectangle =
    getLayoutRec(
      data = data,
      position = position,
      fixedPos = getFixedPosition(data),
      length = length,
      fixedFitLength = getFixedFitLength(data)
    )

  protected abstract fun getLayoutRec(
    data: EditorGroupsSingleRowPassInfo?,
    position: Int,
    fixedPos: Int,
    length: Int,
    fixedFitLength: Int
  ): Rectangle

  protected abstract fun getFixedPosition(data: EditorGroupsSingleRowPassInfo): Int

  abstract fun getTitleRect(data: EditorGroupsSingleRowPassInfo): Rectangle?

  abstract fun getMoreRect(data: EditorGroupsSingleRowPassInfo): Rectangle

  abstract fun getEntryPointRect(data: EditorGroupsSingleRowPassInfo): Rectangle?

  abstract fun createShapeTransform(rectangle: Rectangle?): KrShapeTransform?

  abstract fun layoutComp(data: EditorGroupsSingleRowPassInfo)

  /**
   * Whether a tab that didn't fit completely on the right/bottom side in scrollable layout should be clipped or hidden altogether.
   *
   * @return true if the tab should be clipped, false if hidden.
   */
  abstract fun drawPartialOverflowTabs(): Boolean

  internal abstract class Horizontal protected constructor(layout: KrSingleRowLayout) : KrSingleRowLayoutStrategy(layout) {
    override val isToCenterTextWhenStretched: Boolean
      get() = true

    override val moreRectAxisSize: Int
      get() = myTabs.moreToolbarPreferredSize.width

    override val entryPointAxisSize: Int
      get() = myTabs.entryPointPreferredSize.width

    override val additionalLength: Int
      get() = 0

    override fun getToFitLength(data: EditorGroupsSingleRowPassInfo?): Int {
      if (data == null) return 0

      val hToolbar = data.hToolbar!!.get()
      var length: Int = when {
        hToolbar != null -> myTabs.width - data.insets!!.left - data.insets!!.right - hToolbar.minimumSize.width
        else             -> myTabs.width - data.insets!!.left - data.insets!!.right
      }

      length += getStartPosition(data)

      val entryPointWidth = myTabs.entryPointPreferredSize.width
      val toolbarInsets = myTabs.getActionsInsets()
      val insets = toolbarInsets.left + toolbarInsets.right

      length = (length - (entryPointWidth + insets * sign(entryPointWidth.toDouble()))).toInt()

      return length
    }

    override fun getLengthIncrement(labelPrefSize: Dimension): Int = when {
      myTabs.isEditorTabs -> max(labelPrefSize.width, MIN_TAB_WIDTH)
      else                -> labelPrefSize.width
    }

    override fun getMinPosition(bounds: Rectangle): Int = bounds.x

    override fun getMaxPosition(bounds: Rectangle): Int = bounds.maxX.toInt()

    override fun getFixedFitLength(data: EditorGroupsSingleRowPassInfo?): Int = myTabs.headerFitSize!!.height

    override fun getLayoutRec(
      data: EditorGroupsSingleRowPassInfo?,
      position: Int,
      fixedPos: Int,
      length: Int,
      fixedFitLength: Int
    ): Rectangle = Rectangle(
      /* x = */ position,
      /* y = */ fixedPos,
      /* width = */ length,
      /* height = */ fixedFitLength
    )

    override fun getStartPosition(data: EditorGroupsSingleRowPassInfo): Int = data.insets!!.left

    override fun drawPartialOverflowTabs(): Boolean = true
  }

  internal class Top(layout: KrSingleRowLayout) : Horizontal(layout) {

    override fun createShapeTransform(labelRec: Rectangle?): KrShapeTransform = KrShapeTransform.Top(labelRec)

    override fun getFixedPosition(data: EditorGroupsSingleRowPassInfo): Int = data.insets!!.top

    override fun getEntryPointRect(data: EditorGroupsSingleRowPassInfo): Rectangle {
      val x: Int = when {
        myTabs.isEditorTabs -> data.layoutSize.width - myTabs.getActionsInsets().right - data.entryPointAxisSize
        else                -> data.position
      }
      return Rectangle(x, 1, data.entryPointAxisSize, myTabs.headerFitSize!!.height)
    }

    override fun getMoreRect(data: EditorGroupsSingleRowPassInfo): Rectangle {
      var x: Int = when {
        myTabs.isEditorTabs -> data.layoutSize.width - myTabs.getActionsInsets().right - data.moreRectAxisSize
        else                -> data.position
      }
      x -= data.entryPointAxisSize

      return Rectangle(
        /* x = */ x,
        /* y = */ 1,
        /* width = */ data.moreRectAxisSize,
        /* height = */ myTabs.headerFitSize!!.height
      )
    }

    override fun getTitleRect(data: EditorGroupsSingleRowPassInfo): Rectangle =
      Rectangle(
        /* x = */ 0,
        /* y = */ 0,
        /* width = */ myTabs.titleWrapper.getPreferredSize().width,
        /* height = */ myTabs.headerFitSize!!.height
      )

    override fun layoutComp(data: EditorGroupsSingleRowPassInfo) {
      if (myTabs.isHideTabs()) {
        myTabs.layoutComp(data, 0, 0, 0, 0)
        return
      }

      val vToolbar = data.vToolbar!!.get()
      val vToolbarWidth = if (vToolbar != null) vToolbar.getPreferredSize().width else 0
      val vSeparatorWidth = if (vToolbarWidth > 0) myTabs.separatorWidth else 0
      val x = if (vToolbarWidth > 0) vToolbarWidth + vSeparatorWidth else 0
      val hToolbar = data.hToolbar!!.get()
      val hToolbarHeight = if (!myTabs.isSideComponentOnTabs && hToolbar != null) hToolbar.getPreferredSize().height else 0
      val y: Int = myTabs.headerFitSize!!.height + max(hToolbarHeight, 0)

      val comp = data.component!!.get()

      when {
        hToolbar != null -> {
          val compBounds = myTabs.layoutComp(x, y, comp!!, 0, 0)

          if (myTabs.isSideComponentOnTabs) {
            val toolbarX = when {
              !data.moreRect.isEmpty -> data.moreRect.maxX.toInt()
              else                   -> data.position
            } + myTabs.toolbarInset

            val rec = Rectangle(
              toolbarX, data.insets!!.top,
              myTabs.size.width - data.insets!!.left - toolbarX,  // reduce toolbar height by 1 pixel to properly paint the border between tabs and the content
              myTabs.headerFitSize!!.height - JBUI.scale(1)
            )
            myTabs.layout(hToolbar, rec)
          } else {
            val toolbarHeight = hToolbar.getPreferredSize().height
            myTabs.layout(hToolbar, compBounds.x, compBounds.y - toolbarHeight, compBounds.width, toolbarHeight)
          }
        }

        vToolbar != null -> when {
          myTabs.isSideComponentBefore -> {
            val compBounds = myTabs.layoutComp(x, y, comp!!, 0, 0)
            myTabs.layout(vToolbar, compBounds.x - vToolbarWidth - vSeparatorWidth, compBounds.y, vToolbarWidth, compBounds.height)
          }

          else                         -> {
            val width = if (vToolbarWidth > 0) myTabs.getWidth() - vToolbarWidth - vSeparatorWidth else myTabs.getWidth()
            val compBounds = myTabs.layoutComp(Rectangle(0, y, width, myTabs.getHeight()), comp!!, 0, 0)
            myTabs.layout(vToolbar, compBounds.x + compBounds.width + vSeparatorWidth, compBounds.y, vToolbarWidth, compBounds.height)
          }
        }

        else             -> myTabs.layoutComp(x, y, comp!!, 0, 0)
      }
    }
  }

  internal class Bottom(layout: KrSingleRowLayout) : Horizontal(layout) {
    override fun layoutComp(data: EditorGroupsSingleRowPassInfo) {
      when {
        myTabs.isHideTabs() -> myTabs.layoutComp(data, 0, 0, 0, 0)
        else                -> myTabs.layoutComp(data, 0, 0, 0, -myTabs.headerFitSize!!.height)
      }
    }

    override fun getFixedPosition(data: EditorGroupsSingleRowPassInfo): Int =
      myTabs.size.height - data.insets!!.bottom - myTabs.headerFitSize!!.height

    override fun getEntryPointRect(data: EditorGroupsSingleRowPassInfo): Rectangle {
      val x: Int = when {
        myTabs.isEditorTabs -> data.layoutSize.width - myTabs.getActionsInsets().right - data.entryPointAxisSize
        else                -> data.position
      }

      return Rectangle(
        /* x = */ x,
        /* y = */ getFixedPosition(data),
        /* width = */ data.entryPointAxisSize,
        /* height = */ myTabs.headerFitSize!!.height
      )
    }

    override fun getMoreRect(data: EditorGroupsSingleRowPassInfo): Rectangle {
      var x: Int = when {
        myTabs.isEditorTabs -> data.layoutSize.width - myTabs.getActionsInsets().right - data.moreRectAxisSize
        else                -> data.position
      }
      x -= data.entryPointAxisSize
      return Rectangle(
        /* x = */ x,
        /* y = */ getFixedPosition(data),
        /* width = */ data.moreRectAxisSize,
        /* height = */ myTabs.headerFitSize!!.height
      )
    }

    override fun getTitleRect(data: EditorGroupsSingleRowPassInfo): Rectangle =
      Rectangle(
        /* x = */ 0,
        /* y = */ getFixedPosition(data),
        /* width = */ myTabs.titleWrapper.getPreferredSize().width,
        /* height = */ myTabs.headerFitSize!!.height
      )

    override fun createShapeTransform(labelRec: Rectangle?): KrShapeTransform = KrShapeTransform.Bottom(labelRec)
  }

  companion object {
    private const val MIN_TAB_WIDTH = 50
  }
}
