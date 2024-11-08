package krasa.editorGroups.tabs2.label

import krasa.editorGroups.tabs2.impl.EditorGroupTabLabel
import java.awt.Insets
import java.util.function.Function

interface TabUiDecorator {
  fun getDecoration(): TabUiDecoration

  data class TabUiDecoration @JvmOverloads constructor(
    val labelInsets: Insets? = null,
    val contentInsetsSupplier: Function<EditorGroupTabLabel.ActionsPosition, Insets>? = null,
    val iconTextGap: Int? = null
  )
}
