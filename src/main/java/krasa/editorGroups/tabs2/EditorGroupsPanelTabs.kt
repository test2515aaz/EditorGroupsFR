package krasa.editorGroups.tabs2

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import krasa.editorGroups.tabs2.impl.EditorGroupsTabListOptions
import krasa.editorGroups.tabs2.impl.KrTabsImpl

open class EditorGroupsPanelTabs : KrTabsImpl, EditorGroupsTabsBase {
  override val isEditorTabs: Boolean = true

  constructor(project: Project?, parentDisposable: Disposable) : super(
    project = project,
    parentDisposable = parentDisposable,
    tabListOptions = EditorGroupsTabListOptions(),
  )

  override fun uiSettingsChanged(uiSettings: UISettings) {
    resetTabsCache()
    relayout(forced = true, layoutNow = false)

    super.uiSettingsChanged(uiSettings)
  }

}
