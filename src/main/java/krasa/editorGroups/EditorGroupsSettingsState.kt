package krasa.editorGroups

import com.intellij.ui.JBColor
import com.intellij.util.xmlb.annotations.Transient
import krasa.editorGroups.EditorGroupsSettings.Companion.instance
import krasa.editorGroups.model.RegexGroupModels
import java.awt.Color

class EditorGroupsSettingsState {
  @JvmField
  var tabs: Tabs = Tabs()

  @JvmField
  var regexGroupModels: RegexGroupModels = RegexGroupModels()

  var isSelectRegexGroup: Boolean = true
  var isAutoFolders: Boolean = true
  var isAutoSameName: Boolean = true
  var isForceSwitch: Boolean = true
  var isHideEmpty: Boolean = true
  var isShowSize: Boolean = false
  var isContinuousScrolling: Boolean = false
  var isInitializeSynchronously: Boolean = false
  var isIndexOnlyEditorGroupsFiles: Boolean = false
  var isExcludeEditorGroupsFiles: Boolean = false
  var isCompactTabs: Boolean = false
  var tabBgColor: Int? = null
  var isTabBgColorEnabled: Boolean = false
  var tabFgColor: Int? = null
  var isTabFgColorEnabled: Boolean = false
  var isRememberLastGroup: Boolean = true
  var isGroupSwitchGroupAction: Boolean = false
  var isShowPanel: Boolean = true
  var groupSizeLimitInt: Int = 10000
  var tabSizeLimitInt: Int = 50

  @get:Transient
  val tabBgColorAsColor: Color?
    get() = toColor(tabBgColor)

  @Transient
  fun setTabBgColorAsColor(color: Color?) {
    if (color != null) {
      this.tabBgColor = color.rgb
    }
  }

  @get:Transient
  val tabFgColorAsColor: Color?
    get() = toColor(tabFgColor)

  @Transient
  fun setTabFgColorAsColor(color: Color?) {
    if (color != null) {
      this.tabFgColor = color.rgb
    }
  }

  @Transient
  fun getGroupSizeLimit(): String = groupSizeLimitInt.toString()

  @Transient
  fun setGroupSizeLimit(groupSizeLimit: String) {
    this.groupSizeLimitInt = groupSizeLimit.toInt()
  }

  @Transient
  fun getTabSizeLimit(): String = tabSizeLimitInt.toString()

  @Transient
  fun setTabSizeLimit(tabSizeLimit: String) {
    this.tabSizeLimitInt = tabSizeLimit.toInt()
  }

  class Tabs {
    var isPatchPainter: Boolean = false

    @JvmField
    var mask: Int = DEFAULT_MASK.rgb

    @JvmField
    var opacity: Int = DEFAULT_OPACITY

    @JvmField
    var darkMask: Int = DEFAULT_DARK_MASK.rgb

    @JvmField
    var darkOpacity: Int = DEFAULT_DARK_OPACITY

    @Transient
    fun setOpacity(opacityString: String) {
      try {
        opacity = opacityString.toInt()
        when {
          opacity > 100 -> opacity = 100
          opacity < 0   -> opacity = 0
        }
      } catch (e: Exception) {
        opacity = DEFAULT_OPACITY
      }
    }

    @Transient
    fun setDarkOpacity(opacityString: String) {
      try {
        darkOpacity = opacityString.toInt()
        when {
          darkOpacity > 100 -> darkOpacity = 100
          darkOpacity < 0   -> darkOpacity = 0
        }
      } catch (e: Exception) {
        darkOpacity = DEFAULT_DARK_OPACITY
      }
    }

    companion object {
      @JvmField
      val DEFAULT_MASK: Color = JBColor(Color(0x262626), Color(0x262626))

      const val DEFAULT_OPACITY: Int = 20

      @JvmField
      val DEFAULT_TAB_COLOR: Color = JBColor.WHITE

      @JvmField
      val DEFAULT_DARK_MASK: Color = JBColor(Color(0x262626), Color(0x262626))

      const val DEFAULT_DARK_OPACITY: Int = 50

      @JvmField
      val DEFAULT_DARK_TAB_COLOR: Color = JBColor(Color(0x515658), Color(0x262626))
    }
  }

  companion object {
    // TODO move this file in the EditorGroupsSettings.kt file
    @JvmStatic
    fun state(): EditorGroupsSettingsState = instance.state

    private fun toColor(color: Int?): Color? {
      if (color == null) return null
      return JBColor(Color(color), Color(color))
    }
  }
}
