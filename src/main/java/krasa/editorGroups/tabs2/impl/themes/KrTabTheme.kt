package krasa.editorGroups.tabs2.impl.themes

import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Color

interface KrTabTheme {
  val topBorderThickness: Int
    get() = JBUI.scale(1)
  val background: Color?
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
}

open class KrDefaultTabTheme : KrTabTheme {
  override val background: Color? get() = JBUI.CurrentTheme.DefaultTabs.background()
  override val borderColor: Color get() = JBUI.CurrentTheme.DefaultTabs.borderColor()
  override val underlineColor: Color get() = JBUI.CurrentTheme.DefaultTabs.underlineColor()
  override val inactiveUnderlineColor: Color get() = JBUI.CurrentTheme.DefaultTabs.inactiveUnderlineColor()
  override val hoverBackground: Color get() = JBUI.CurrentTheme.DefaultTabs.hoverBackground()
  override val underlinedTabBackground: Color? get() = JBUI.CurrentTheme.DefaultTabs.underlinedTabBackground()
  override val underlinedTabForeground: Color get() = JBUI.CurrentTheme.DefaultTabs.underlinedTabForeground()
  override val underlineHeight: Int get() = JBUI.CurrentTheme.DefaultTabs.underlineHeight()
  override val hoverInactiveBackground: Color?
    get() = hoverBackground
  override val underlinedTabInactiveBackground: Color?
    get() = underlinedTabBackground
  override val underlinedTabInactiveForeground: Color
    get() = underlinedTabForeground
  override val inactiveColoredTabBackground: Color?
    get() = JBUI.CurrentTheme.DefaultTabs.inactiveColoredTabBackground()
}
