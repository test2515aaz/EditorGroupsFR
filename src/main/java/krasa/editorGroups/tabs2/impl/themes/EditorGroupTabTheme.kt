package krasa.editorGroups.tabs2.impl.themes

import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Font

interface EditorGroupTabTheme {
  val topBorderThickness: Int
    get() = JBUI.scale(1)

  val background: Color
  val borderColor: Color
  val underlineColor: Color
  val inactiveUnderlineColor: Color
  val hoverBackground: Color

  val hoverSelectedBackground: Color
    get() = hoverBackground

  val hoverSelectedInactiveBackground: Color
    get() = hoverBackground

  val hoverInactiveBackground: Color?

  val underlinedTabBackground: Color?
  val underlinedTabForeground: Color
  val underlineHeight: Int

  val underlineArc: Int
    get() = 0

  val underlinedTabInactiveBackground: Color?
  val underlinedTabInactiveForeground: Color?
  val inactiveColoredTabBackground: Color?

  val fontSizeOffset: Int
    get() = 0

  val font: Font?

  val tabHeight: Int
  val compactTabHeight: Int
}
