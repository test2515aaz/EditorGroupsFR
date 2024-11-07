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

  fun addTabSilently(info: EditorGroupTabInfo, index: Int): EditorGroupTabInfo?

  fun removeTab(info: EditorGroupTabInfo, forcedSelectionTransfer: EditorGroupTabInfo?)

  fun getToSelectOnRemoveOf(info: EditorGroupTabInfo): EditorGroupTabInfo?

  fun sortTabs(comparator: Comparator<EditorGroupTabInfo>)

  val isEmptyVisible: Boolean

  fun setTitleProducer(titleProducer: (() -> Pair<Icon, @Nls String>)?)

}
