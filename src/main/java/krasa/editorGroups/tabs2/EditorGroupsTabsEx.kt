package krasa.editorGroups.tabs2

import com.intellij.openapi.actionSystem.DataKey
import org.jetbrains.annotations.Nls
import javax.swing.Icon

interface EditorGroupsTabsEx : EditorGroupsTabsBase {
  companion object {
    @JvmField
    val NAVIGATION_ACTIONS_KEY: DataKey<EditorGroupsTabsEx> = DataKey.create("KrTabs")
  }

  val isEditorTabs: Boolean

  fun updateTabActions(validateNow: Boolean)

  fun addTabSilently(info: KrTabInfo, index: Int): KrTabInfo?

  fun removeTab(info: KrTabInfo, forcedSelectionTransfer: KrTabInfo?)

  fun getToSelectOnRemoveOf(info: KrTabInfo): KrTabInfo?

  fun sortTabs(comparator: Comparator<KrTabInfo>)

  val isEmptyVisible: Boolean

  fun setTitleProducer(titleProducer: (() -> Pair<Icon, @Nls String>)?)

  /** true if tabs and top toolbar should be hidden from a view */
  var isHideTopPanel: Boolean
}
