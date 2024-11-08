package krasa.editorGroups.tabs2.impl.painter

import krasa.editorGroups.tabs2.impl.EditorGroupTabLabel
import krasa.editorGroups.tabs2.impl.KrTabsImpl
import krasa.editorGroups.tabs2.impl.themes.EditorGroupTabTheme
import java.awt.Graphics

interface EditorGroupsTabPainterAdapter {
  val tabPainter: EditorGroupsTabPainter
  fun paintBackground(label: EditorGroupTabLabel, g: Graphics, tabs: KrTabsImpl)
  fun getTabTheme(): EditorGroupTabTheme = tabPainter.getTabTheme()
}
