package krasa.editorGroups.tabs2.impl.painter

import krasa.editorGroups.tabs2.impl.EditorGroupTabLabel
import krasa.editorGroups.tabs2.impl.KrTabsImpl
import krasa.editorGroups.tabs2.impl.themes.EditorGroupTabTheme
import java.awt.Graphics

interface KrTabPainterAdapter {
  fun paintBackground(label: EditorGroupTabLabel, g: Graphics, tabs: KrTabsImpl)
  val tabPainter: KrTabPainter
  fun getTabTheme(): EditorGroupTabTheme = tabPainter.getTabTheme()
}
