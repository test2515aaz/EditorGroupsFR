package krasa.editorGroups.tabs2.label

import krasa.editorGroups.tabs2.impl.KrTabLabel
import java.awt.Font
import java.awt.Insets
import java.util.function.Function

interface TabUiDecorator {
  fun getDecoration(): TabUiDecoration

  data class TabUiDecoration @JvmOverloads constructor(
    val labelFont: Font? = null,
    val labelInsets: Insets? = null,
    val contentInsetsSupplier: Function<KrTabLabel.ActionsPosition, Insets>? = null,
    val iconTextGap: Int? = null
  )
}
