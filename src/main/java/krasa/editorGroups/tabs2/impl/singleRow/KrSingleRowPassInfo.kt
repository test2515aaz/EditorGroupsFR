package krasa.editorGroups.tabs2.impl.singleRow

import krasa.editorGroups.tabs2.impl.KrTabsImpl
import krasa.editorGroups.tabs2.label.EditorGroupTabInfo
import java.awt.Dimension
import java.awt.Insets
import java.awt.Rectangle
import java.lang.ref.WeakReference
import javax.swing.JComponent

class KrSingleRowPassInfo(
  layout: KrSingleRowLayout,
  visibleInfos: MutableList<EditorGroupTabInfo>
) : KrLayoutPassInfo(visibleInfos) {
  private val tabs: KrTabsImpl = layout.tabs

  @JvmField
  val layoutSize: Dimension = tabs.size

  @JvmField
  val contentCount: Int = tabs.tabCount

  @JvmField
  var position: Int = 0

  override var requiredLength: Int = 0

  @JvmField
  var toFitLength: Int = 0

  @JvmField
  val toLayout: MutableList<EditorGroupTabInfo> = mutableListOf<EditorGroupTabInfo>()

  @JvmField
  val toDrop: MutableList<EditorGroupTabInfo> = mutableListOf<EditorGroupTabInfo>()

  @JvmField
  val entryPointAxisSize: Int = layout.strategy.getEntryPointAxisSize()

  @JvmField
  val moreRectAxisSize: Int = layout.strategy.getMoreRectAxisSize()

  @JvmField
  var hToolbar: WeakReference<JComponent?>? = null

  @JvmField
  var vToolbar: WeakReference<JComponent?>? = null

  @JvmField
  var insets: Insets? = null

  @JvmField
  var component: WeakReference<JComponent?>? = null

  @JvmField
  var tabRectangle: Rectangle? = null

  @JvmField
  val scrollOffset: Int = layout.scrollOffset

  override val headerRectangle: Rectangle?
    get() = tabRectangle?.clone() as? Rectangle

  override val scrollExtent: Int
    get() = when {
      !moreRect.isEmpty       -> moreRect.x - tabs.getActionsInsets().left
      !entryPointRect.isEmpty -> entryPointRect.x - tabs.getActionsInsets().left
      else                    -> layoutSize.width
    }

}
