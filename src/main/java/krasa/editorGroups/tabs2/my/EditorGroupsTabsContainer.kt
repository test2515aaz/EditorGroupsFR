package krasa.editorGroups.tabs2.my

import com.intellij.ide.IdeEventQueue
import com.intellij.ide.IdeEventQueue.BlockMode
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.EditorGroupManager.Companion.getInstance
import krasa.editorGroups.EditorGroupPanel.EditorGroupTabInfo
import krasa.editorGroups.tabs2.EditorGroupsPanelTabs
import krasa.editorGroups.tabs2.KrTabInfo
import krasa.editorGroups.tabs2.impl.KrTabLabel
import krasa.editorGroups.tabs2.impl.singleRow.KrScrollableSingleRowLayout
import krasa.editorGroups.tabs2.impl.singleRow.KrSingleRowLayout
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

class EditorGroupsTabsContainer(private val project: Project, parent: Disposable, private val file: VirtualFile) :
  EditorGroupsPanelTabs(project, parent) {
  /** The single row layout that will contain our component. */
  private val mySingleRowLayout = createSingleRowLayout()

  var bulkUpdate: Boolean = false

  val scrollOffset: Int = this.mySingleRowLayout.scrollOffset

  /** Returns the selected EditorGroupTabInfo. */
  override val selectedInfo: KrTabInfo?
    get() {
      val selectedInfo = super.selectedInfo
      if (selectedInfo !is EditorGroupTabInfo) return selectedInfo

      val selectable = selectedInfo.selectable
      if (!selectable) return null

      return selectedInfo
    }

  init {
    patchMouseListener(this)
  }

  /** Create a [KrTabLabel] from a [KrTabInfo]. */
  override fun createTabLabel(info: KrTabInfo): KrTabLabel {
    val tabLabel = KrTabLabel(this, info)
    patchMouseListener(tabLabel)

    return tabLabel
  }

  /** Re-add mouse listeners. */
  private fun patchMouseListener(tabLabel: Component) {
    val mouseListeners = tabLabel.mouseListeners
    for (mouseListener in mouseListeners) {
      tabLabel.removeMouseListener(mouseListener)
    }

    for (mouseListener in mouseListeners) {
      tabLabel.addMouseListener(MyMouseAdapter(mouseListener))
    }
  }

  /** Remove the provided component. */
  override fun remove(comp: Component?) {
    if (comp == null) return

    super.remove(comp)
  }

  override fun revalidateAndRepaint(layoutNow: Boolean) {
    // performance optimization
    if (bulkUpdate) return
    super.revalidateAndRepaint(layoutNow)
  }

  /**
   * com.intellij.util.IncorrectOperationException: Sorry but parent: EditorGroups.KrJBEditorTabs visible=[] selected=null
   * has already been disposed (see the cause for stacktrace) so the child: Animator 'KrTabs Attractions' @1845106519
   * (stopped) will never be disposed at com.intellij.openapi.util.objectTree.ObjectTree.register(ObjectTree.java:61)
   * at com.intellij.openapi.util.Disposer.register(Disposer.java:92) at
   * krasa.editorGroups.tabs2.impl.JBTabsImpl$7.initialize(JBTabsImpl.java:340) at
   * krasa.editorGroups.tabs2.impl.JBTabsImpl$7.initialize(JBTabsImpl.java:333)
   */
  override fun createSingleRowLayout(): KrSingleRowLayout = KrScrollableSingleRowLayout(this)

  /** Do not handle inactive tabs. */
  override fun isActiveTabs(info: KrTabInfo?): Boolean = true

  override fun doLayout() {
    adjustScroll()
    super.doLayout()
  }

  /** fixes flicker when switching to an already opened tab, #scroll is too late, but is necessary anyway for some reason */
  fun adjustScroll() {
    if (project.isDisposed) return

    val switchingRequest = getInstance(project).getSwitchingRequest(file)
    if (switchingRequest != null) {
      val myScrollOffset = switchingRequest.myScrollOffset
      val relativeScroll = myScrollOffset - this.scrollOffset
      mySingleRowLayout.scroll(relativeScroll)
    }
  }

  /** Scroll the tabs to the given position. */
  fun scroll(myScrollOffset: Int) {
    if (mySingleRowLayout.lastSingleRowLayout == null) return

    val relativeScroll = myScrollOffset - this.scrollOffset
    mySingleRowLayout.scroll(relativeScroll)
    revalidateAndRepaint(false)
  }

  fun setTabInfo(tabInfo: KrTabInfo?) {
    this.popupInfo = tabInfo
  }

  override fun toString(): String = "EditorGroups.KrJBEditorTabs visible=${getVisibleInfos()} selected=$selectedInfo"

  private class MyMouseAdapter(private val mouseListener: MouseListener) : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent) {
      // fix for - Ctrl + Mouse Click events are also consumed by the editor
      IdeEventQueue.getInstance().blockNextEvents(e, BlockMode.ACTIONS)
      mouseListener.mouseClicked(e)
    }

    override fun mousePressed(e: MouseEvent) {
      IdeEventQueue.getInstance().blockNextEvents(e, BlockMode.ACTIONS)
      mouseListener.mousePressed(e)
    }

    override fun mouseReleased(e: MouseEvent) {
      IdeEventQueue.getInstance().blockNextEvents(e, BlockMode.ACTIONS)
      mouseListener.mouseReleased(e)
    }

    override fun mouseEntered(e: MouseEvent?) {
      super.mouseEntered(e)
      mouseListener.mouseEntered(e)
    }

    override fun mouseExited(e: MouseEvent?) {
      super.mouseExited(e)
      mouseListener.mouseExited(e)
    }
  }
}
