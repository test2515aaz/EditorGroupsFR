package krasa.editorGroups.tabs2

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.ActiveRunnable
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.util.function.Supplier
import javax.swing.JComponent

interface KrTabs {
  /** Selected tab. */
  val selectedInfo: KrTabInfo?

  /** List of tabs. */
  val tabs: List<KrTabInfo>

  /** Tab Count. */
  val tabCount: Int

  /** Add a tab at the given index. */
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

  fun getTabLabel(tabInfo: KrTabInfo): Component?

  interface SelectionChangeHandler {
    fun execute(info: KrTabInfo, requestFocus: Boolean, doChangeSelection: ActiveRunnable): ActionCallback
  }
}
