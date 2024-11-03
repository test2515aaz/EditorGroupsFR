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

@Suppress("unused", "HardCodedStringLiteral")
interface EditorGroupsTabs {
  /** Selected tab. */
  val selectedInfo: KrTabInfo?

  /** List of tabs. */
  val tabs: List<KrTabInfo>

  /** Tab Count. */
  val tabCount: Int

  /** Add a tab at the given index. */
  fun addTab(info: KrTabInfo, index: Int): KrTabInfo

  /** Adds a tab at the end. */
  fun addTab(info: KrTabInfo): KrTabInfo

  /** Removes a tab. */
  fun removeTab(info: KrTabInfo?): ActionCallback

  /** Removes all tabs. */
  fun removeAllTabs()

  /** Selects a tab, optionally requesting focus. */
  fun select(info: KrTabInfo, requestFocus: Boolean): ActionCallback

  /** Get Tab at index. */
  fun getTabAt(tabIndex: Int): KrTabInfo

  /** The tab presentation. */
  fun getPresentation(): KrTabsPresentation

  fun setDataProvider(dataProvider: DataProvider): EditorGroupsTabs?

  fun getTargetInfo(): KrTabInfo?

  fun addTabMouseListener(listener: MouseListener): EditorGroupsTabs

  fun addListener(listener: KrTabsListener): EditorGroupsTabs?

  fun addListener(listener: KrTabsListener, disposable: Disposable?): EditorGroupsTabs?

  fun setSelectionChangeHandler(handler: SelectionChangeHandler): EditorGroupsTabs?

  fun getComponent(): JComponent

  fun findInfo(event: MouseEvent): KrTabInfo?

  fun findInfo(`object`: Any): KrTabInfo?

  fun findInfo(component: Component): KrTabInfo?

  fun getIndexOf(tabInfo: KrTabInfo?): Int

  fun requestFocus()

  fun setNavigationActionBinding(prevActionId: String, nextActionId: String)

  fun setPopupGroup(popupGroup: ActionGroup, place: String, addNavigationGroup: Boolean): EditorGroupsTabs

  fun setPopupGroupWithSupplier(supplier: Supplier<out ActionGroup?>, place: String, addNavigationGroup: Boolean): EditorGroupsTabs

  fun resetDropOver(tabInfo: KrTabInfo)

  fun getTabLabel(tabInfo: KrTabInfo): Component?

  interface SelectionChangeHandler {
    fun execute(info: KrTabInfo, requestFocus: Boolean, doChangeSelection: ActiveRunnable): ActionCallback
  }
}
