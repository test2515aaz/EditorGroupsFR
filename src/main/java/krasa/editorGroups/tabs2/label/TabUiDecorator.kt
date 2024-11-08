package krasa.editorGroups.tabs2.label

import java.awt.Insets

interface TabUiDecorator {
  val decoration: TabUiDecoration

  data class TabUiDecoration @JvmOverloads constructor(
    val labelInsets: Insets? = null,
    val contentInsetsSupplier: Insets? = null,
    val iconTextGap: Int? = null
  )
}
