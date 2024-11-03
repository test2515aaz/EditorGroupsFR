package krasa.editorGroups.tabs2.impl

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.IdeFocusManager
import krasa.editorGroups.settings.EditorGroupsSettings
import krasa.editorGroups.tabs2.EditorGroupsTabs
import krasa.editorGroups.tabs2.KrTabsPresentation

open class KrEditorTabs : KrTabsImpl, EditorGroupsTabs {
  private var isAlphabeticalModeChanged = false

  override val isEditorTabs: Boolean = true

  constructor(project: Project?, focusManager: IdeFocusManager?, parentDisposable: Disposable) : super(
    project,
    parentDisposable
  )

  constructor(project: Project?, parentDisposable: Disposable) : super(project, parentDisposable)

  constructor(
    project: Project?,
    actionManager: ActionManager,
    focusManager: IdeFocusManager?,
    parent: Disposable
  ) : this(project, parent)

  override fun uiSettingsChanged(uiSettings: UISettings) {
    resetTabsCache()
    relayout(forced = true, layoutNow = false)

    super.uiSettingsChanged(uiSettings)
  }

  override fun useSmallLabels(): Boolean = EditorGroupsSettings.instance.isSmallLabels

  override fun setAlphabeticalMode(value: Boolean): KrTabsPresentation {
    isAlphabeticalModeChanged = true
    return super.setAlphabeticalMode(value)
  }

  fun shouldPaintBottomBorder(): Boolean = true

  companion object {
    @JvmField
    val MARK_MODIFIED_KEY: Key<Boolean> = Key.create("EDITOR_TABS_MARK_MODIFIED")
  }
}
