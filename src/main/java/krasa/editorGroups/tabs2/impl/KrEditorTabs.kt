// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.IdeFocusManager
import krasa.editorGroups.tabs2.KrEditorTabsBase
import krasa.editorGroups.tabs2.KrTabsPresentation

open class KrEditorTabs : KrTabsImpl, KrEditorTabsBase {
  private var isAlphabeticalModeChanged = false

  override val isEditorTabs: Boolean = true

  @Suppress("UNUSED_PARAMETER")
  constructor(project: Project?, focusManager: IdeFocusManager?, parentDisposable: Disposable) : super(
    project,
    parentDisposable
  )

  constructor(project: Project?, parentDisposable: Disposable) : super(project, parentDisposable)

  @Deprecated("Use {@link #JBEditorTabs(Project, Disposable)}", level = DeprecationLevel.ERROR)
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

  override fun useSmallLabels(): Boolean = UISettings.getInstance().useSmallLabelsOnTabs

  override fun isAlphabeticalMode(): Boolean = when {
    isAlphabeticalModeChanged -> super.isAlphabeticalMode()
    else                      -> UISettings.getInstance().sortTabsAlphabetically
  }

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
