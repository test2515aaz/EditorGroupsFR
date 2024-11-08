package krasa.editorGroups.tabs2.impl.themes

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Font
import javax.swing.UIManager

object EditorGroupsUI {
  val defaultTheme: EditorGroupDefaultTabTheme = EditorGroupDefaultTabTheme()

  fun underlineColor(): Color = JBColor.namedColor(
    "EditorGroupsTabs.underlineColor",
    defaultTheme.underlineColor
  )

  fun underlineHeight(): Int = JBUI.getInt(
    "EditorGroupsTabs.underlineHeight",
    defaultTheme.underlineHeight
  )

  fun inactiveUnderlineColor(): Color = JBColor.namedColor(
    "EditorGroupsTabs.inactiveUnderlineColor",
    defaultTheme.inactiveUnderlineColor
  )

  fun borderColor(): Color = JBColor.namedColor(
    "EditorGroupsTabs.borderColor",
    defaultTheme.borderColor
  )

  fun background(): Color = JBColor.namedColor(
    "EditorGroupsTabs.background",
    defaultTheme.background
  )

  fun hoverBackground(): Color = JBColor.namedColor(
    "EditorGroupsTabs.hoverBackground",
    defaultTheme.hoverBackground
  )

  fun hoverInactiveBackground(): Color = JBColor.namedColor(
    "EditorGroupsTabs.hoverInactiveBackground",
    defaultTheme.hoverInactiveBackground
  )

  fun hoverSelectedBackground(): Color = JBColor.namedColor(
    "EditorGroupsTabs.hoverSelectedBackground",
    defaultTheme.hoverSelectedBackground
  )

  fun hoverSelectedInactiveBackground(): Color = JBColor.namedColor(
    "EditorGroupsTabs.hoverSelectedInactiveBackground",
    defaultTheme.hoverSelectedInactiveBackground
  )

  fun underlinedTabBackground(): Color? = JBColor.namedColor(
    "EditorGroupsTabs.underlinedTabBackground",
    defaultTheme.underlinedTabBackground
  )

  fun underlinedTabForeground(): Color = JBColor.namedColor(
    "EditorGroupsTabs.underlinedTabForeground",
    defaultTheme.underlinedTabForeground
  )

  fun underlineArc(): Int = JBUI.getInt(
    "EditorGroupsTabs.underlineArc",
    defaultTheme.underlineArc
  )

  fun font(): Font = defaultFont()
    .biggerOn(
      JBUI.getInt("EditorGroupsTabs.fontSizeOffset", 0).toFloat()
    )

  private fun defaultFont(): JBFont {
    val font = UIManager.getFont("EditorGroupsTabs.font") ?: JBUI.CurrentTheme.EditorTabs.font()

    return JBFont.create(font, false)
  }
}
