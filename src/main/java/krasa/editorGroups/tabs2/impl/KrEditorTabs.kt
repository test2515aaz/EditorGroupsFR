// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.ExperimentalUI
import krasa.editorGroups.tabs2.KrEditorTabsBase
import krasa.editorGroups.tabs2.KrTabsPresentation

open class KrEditorTabs : KrTabsImpl, KrEditorTabsBase {
  protected var myDefaultPainter: KrEditorTabsPainter = KrDefaultEditorTabsPainter(this)
  private var myAlphabeticalModeChanged = false
  override val isEditorTabs: Boolean = true

  constructor(project: Project?, focusManager: IdeFocusManager?, parentDisposable: Disposable) : super(
    project,
    parentDisposable
  ) {
    setSupportsCompression(true)
  }

  constructor(project: Project?, parentDisposable: Disposable) : super(project, parentDisposable) {
    setSupportsCompression(true)
  }

  constructor(
    project: Project?,
    actionManager: ActionManager,
    focusManager: IdeFocusManager?,
    parent: Disposable
  ) : this(project, parent)

  override fun uiSettingsChanged(uiSettings: UISettings) {
    resetTabsCache()
    relayout(true, false)

    super.uiSettingsChanged(uiSettings)
  }

  override fun useSmallLabels(): Boolean = UISettings.getInstance().useSmallLabelsOnTabs && !ExperimentalUI.isNewUI()

  override fun isAlphabeticalMode(): Boolean = when {
    myAlphabeticalModeChanged -> super.isAlphabeticalMode()
    else                      -> UISettings.getInstance().sortTabsAlphabetically
  }

  override fun setAlphabeticalMode(value: Boolean): KrTabsPresentation {
    myAlphabeticalModeChanged = true
    return super.setAlphabeticalMode(value)
  }

  fun shouldPaintBottomBorder(): Boolean = true

  companion object {
    @JvmField
    val MARK_MODIFIED_KEY: Key<Boolean> = Key.create("EDITOR_TABS_MARK_MODIFIED")
  }
}
