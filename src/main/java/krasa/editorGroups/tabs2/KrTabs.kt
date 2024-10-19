package krasa.editorGroups.tabs2

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.ActiveRunnable
import com.intellij.ui.DropAreaAware
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.Image
import java.awt.Insets
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.util.function.Supplier
import javax.swing.JComponent

interface KrTabs : DropAreaAware {
  val selectedInfo: KrTabInfo?

  val tabs: List<KrTabInfo>

  val tabCount: Int

  fun addTab(info: KrTabInfo, index: Int): KrTabInfo

  fun addTab(info: KrTabInfo): KrTabInfo

  fun removeTab(info: KrTabInfo?): ActionCallback

  fun removeAllTabs()

  fun select(info: KrTabInfo, requestFocus: Boolean): ActionCallback

  fun getTabAt(tabIndex: Int): KrTabInfo

  fun getPresentation(): KrTabsPresentation

  fun setDataProvider(dataProvider: DataProvider): KrTabs?

  fun getTargetInfo(): KrTabInfo?

  fun addTabMouseListener(listener: MouseListener): KrTabs

  fun addListener(listener: KrTabsListener): KrTabs?

  fun addListener(listener: KrTabsListener, disposable: Disposable?): KrTabs?

  fun setSelectionChangeHandler(handler: SelectionChangeHandler): KrTabs?

  fun getComponent(): JComponent

  fun findInfo(event: MouseEvent): KrTabInfo?

  fun findInfo(`object`: Any): KrTabInfo?

  fun findInfo(component: Component): KrTabInfo?

  fun getIndexOf(tabInfo: KrTabInfo?): Int

  fun requestFocus()

  fun setNavigationActionBinding(prevActionId: String, nextActionId: String)

  fun setPopupGroup(popupGroup: ActionGroup, place: String, addNavigationGroup: Boolean): KrTabs

  fun setPopupGroupWithSupplier(supplier: Supplier<out ActionGroup?>, place: String, addNavigationGroup: Boolean): KrTabs

  fun resetDropOver(tabInfo: KrTabInfo)

  fun startDropOver(tabInfo: KrTabInfo, point: RelativePoint): Image?

  fun processDropOver(over: KrTabInfo, point: RelativePoint)

  fun getTabLabel(tabInfo: KrTabInfo): Component?

  override fun getDropArea(): Rectangle {
    val r = Rectangle(getComponent().bounds)
    if (tabCount > 0) {
      val insets: Insets = JBUI.emptyInsets()
      val bounds = getTabLabel(getTabAt(0))!!.bounds
      when (getPresentation().getTabsPosition()) {
        KrTabsPosition.top    -> insets.top = bounds.height
        KrTabsPosition.left   -> insets.left = bounds.width
        KrTabsPosition.bottom -> insets.bottom = bounds.height
        KrTabsPosition.right  -> insets.right = bounds.width
      }
      JBInsets.removeFrom(r, insets)
    }
    return r
  }

  interface SelectionChangeHandler {
    fun execute(info: KrTabInfo, requestFocus: Boolean, doChangeSelection: ActiveRunnable): ActionCallback
  }
}
