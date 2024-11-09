package krasa.editorGroups.tabs2.impl.singleRow

import krasa.editorGroups.tabs2.label.EditorGroupTabInfo
import java.awt.Rectangle

abstract class EditorGroupsLayoutPassInfo protected constructor(
  @JvmField val visibleTabInfos: MutableList<EditorGroupTabInfo>
) {
  @JvmField
  var entryPointRect: Rectangle = Rectangle()

  @JvmField
  var moreRect: Rectangle = Rectangle()

  @JvmField
  var titleRect: Rectangle = Rectangle()

  abstract val headerRectangle: Rectangle?

  abstract val requiredLength: Int

  abstract val scrollExtent: Int
}
