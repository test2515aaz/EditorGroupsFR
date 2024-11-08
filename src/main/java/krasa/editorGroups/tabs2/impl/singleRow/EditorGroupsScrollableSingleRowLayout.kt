package krasa.editorGroups.tabs2.impl.singleRow

import com.intellij.util.ui.JBUI
import krasa.editorGroups.tabs2.impl.KrTabsImpl
import krasa.editorGroups.tabs2.label.EditorGroupTabInfo
import krasa.editorGroups.tabs2.label.EditorGroupTabLabel
import kotlin.math.max
import kotlin.math.min

class EditorGroupsScrollableSingleRowLayout(tabs: KrTabsImpl) : KrSingleRowLayout(tabs) {
  override var scrollOffset: Int = 0
    private set

  override val isWithScrollBar: Boolean = true

  /** Size of the more button rect. */
  private val moreRectAxisSize: Int
    get() = strategy.getMoreRectAxisSize()

  /** Scroll to X units. */
  override fun scroll(units: Int) {
    this.scrollOffset += units
    clampScrollOffsetToBounds(lastSingleRowLayout)
  }

  override fun checkLayoutLabels(data: KrSingleRowPassInfo): Boolean = true

  /**
   * Clamps the scroll offset to ensure the content is within the bounds.
   *
   * @param data The data object containing the current layout information.
   */
  private fun clampScrollOffsetToBounds(data: KrSingleRowPassInfo?) {
    if (data == null) return

    if (data.requiredLength < data.toFitLength) {
      this.scrollOffset = 0
      return
    }

    var max = data.requiredLength - data.toFitLength + this.moreRectAxisSize
    val actionInsets = tabs.getActionsInsets()
    max += actionInsets.left + actionInsets.right

    this.scrollOffset = max(
      0.0,
      min(scrollOffset.toDouble(), max.toDouble())
    ).toInt()
  }

  /**
   * Scrolls the view to ensure that the selected tab is visible.
   *
   * @param passInfo The layout information for the current row of tabs.
   */
  private fun doScrollToSelectedTab(passInfo: KrSingleRowPassInfo) {
    if (tabs.isMouseInsideTabsArea || tabs.isScrollBarAdjusting() || tabs.isRecentlyActive) return

    var offset = -this.scrollOffset
    for (info in passInfo.myVisibleInfos) {
      val length = getRequiredLength(info)

      if (info === tabs.selectedInfo) {
        if (offset < 0) {
          scroll(offset)
          continue
        }

        var maxLength = passInfo.toFitLength - this.moreRectAxisSize
        val actionInsets = tabs.getActionsInsets()

        if (tabs.entryPointPreferredSize.width == 0) {
          maxLength -= actionInsets.left + actionInsets.right
        }

        if (offset + length > maxLength) {
          // a left side should always be visible
          when {
            length < maxLength -> scroll(offset + length - maxLength)
            else               -> scroll(offset)
          }
        }
        break
      }
      offset += length
    }
  }

  override fun recomputeToLayout(data: KrSingleRowPassInfo) {
    calculateRequiredLength(data)
    clampScrollOffsetToBounds(data)
    doScrollToSelectedTab(data)
    clampScrollOffsetToBounds(data)
  }

  override fun layoutMoreButton(data: KrSingleRowPassInfo) {
    if (data.requiredLength > data.toFitLength) {
      data.moreRect = strategy.getMoreRect(data)
    }
  }

  override fun applyTabLayout(data: KrSingleRowPassInfo, label: EditorGroupTabLabel, length: Int): Boolean {
    var length = length

    if (data.requiredLength > data.toFitLength) {
      length = strategy.getLengthIncrement(label.getPreferredSize())
      var moreRectSize = this.moreRectAxisSize

      if (data.entryPointAxisSize == 0) {
        val insets = tabs.getActionsInsets()
        moreRectSize += insets.left + insets.right
      }

      if (data.position + length > data.toFitLength - moreRectSize) {
        if (strategy.drawPartialOverflowTabs()) {
          val clippedLength = data.toFitLength - data.position - moreRectSize
          val rec = strategy.getLayoutRect(data, data.position, clippedLength)
          tabs.layout(label, rec)
        }

        label.setAlignmentToCenter()
        return false
      }
    }

    return super.applyTabLayout(data, label, length)
  }

  /** Check whether the given tab is hidden (needs scrolling) */
  override fun isTabHidden(info: EditorGroupTabInfo): Boolean {
    val label = tabs.getTabLabel(info)!!
    val bounds = label.bounds
    val deadzone = JBUI.scale(DEADZONE_FOR_DECLARE_TAB_HIDDEN)

    return strategy.getMinPosition(bounds) < -deadzone
      || bounds.width < label.getPreferredSize().width - deadzone
      || bounds.height < label.getPreferredSize().height - deadzone
  }

  override fun findLastVisibleLabel(data: KrSingleRowPassInfo): EditorGroupTabLabel? {
    var i = data.toLayout.size - 1
    while (i >= 0) {
      val info = data.toLayout[i]
      val label = tabs.getTabLabel(info)!!

      if (!label.bounds.isEmpty) return label

      i--
    }
    return null
  }

  companion object {
    const val DEADZONE_FOR_DECLARE_TAB_HIDDEN: Int = 10
  }
}
