// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.editorGroups.tabs2.impl

import krasa.editorGroups.tabs2.label.EditorGroupTabInfo

abstract class EditorGroupsTabLayout {
  open val scrollOffset: Int
    get() = 0

  open fun scroll(units: Int): Unit = Unit

  open fun isTabHidden(info: EditorGroupTabInfo): Boolean = false
}
