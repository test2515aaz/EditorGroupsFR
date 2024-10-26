package krasa.editorGroups

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import krasa.editorGroups.EditorGroupsSettings.EditorGroupsSettingsState
import krasa.editorGroups.model.RegexGroupModels
import krasa.editorGroups.settings.EditorGroupSetting

@Service(Service.Level.APP)
@State(name = "EditorGroups", storages = [Storage(value = "EditorGroups.xml")], category = SettingsCategory.UI)
class EditorGroupsSettings : SimplePersistentStateComponent<EditorGroupsSettingsState>(EditorGroupsSettingsState()) {
  class EditorGroupsSettingsState : BaseState() {
    @EditorGroupSetting([EditorGroupSetting.Category.REGEX])
    var regexGroupModels by property(RegexGroupModels())

    // Select first matching regex group if no group matches
    @EditorGroupSetting([EditorGroupSetting.Category.REGEX, EditorGroupSetting.Category.GROUPS])
    var isSelectRegexGroup by property(false)

    // Select current folder group if no other group matches
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
    var isAutoFolders by property(true)

    // Select same name group if no other group matches
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
    var isAutoSameName by property(true)

    // Refresh button switches to a manual group if exists
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
    var isForceSwitch by property(true)

    // Hide the panel if no group matches
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
    var isHideEmpty by property(true)

    // Show group size at title
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS, EditorGroupSetting.Category.UI])
    var isShowSize by property(false)

    // Color tabs of the current group
    @EditorGroupSetting([EditorGroupSetting.Category.TABS, EditorGroupSetting.Category.UI])
    var isColorTabs by property(true)

    // Continuous scrolling
    @EditorGroupSetting([EditorGroupSetting.Category.TABS])
    var isContinuousScrolling by property(false)

    // Index synchronously - less flicker
    @EditorGroupSetting([EditorGroupSetting.Category.PERFORMANCE])
    var isInitializeSynchronously by property(false)

    // Index only egroups file for performance
    @EditorGroupSetting([EditorGroupSetting.Category.EGROUPS, EditorGroupSetting.Category.PERFORMANCE])
    var isIndexOnlyEditorGroupsFiles by property(false)

    // Exclude egroups files from indexing
    @EditorGroupSetting([EditorGroupSetting.Category.EGROUPS, EditorGroupSetting.Category.PERFORMANCE])
    var isExcludeEditorGroupsFiles by property(false)

    // Remember last group
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
    var isRememberLastGroup by property(true)

    // Compact tabs
    @EditorGroupSetting([EditorGroupSetting.Category.TABS, EditorGroupSetting.Category.UI])
    var isCompactTabs by property(false)

    // Split the list in groups
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS, EditorGroupSetting.Category.UI])
    var isGroupSwitchGroupAction by property(false)

    // Show the panel at all
    @EditorGroupSetting([EditorGroupSetting.Category.UI])
    var isShowPanel by property(true)

    // Limit the number of elements in a group
    @EditorGroupSetting([EditorGroupSetting.Category.GROUPS, EditorGroupSetting.Category.PERFORMANCE])
    var groupSizeLimitInt: Int by property(DEFAULT_GROUP_SIZE_LIMIT)

    // Limit the number of tabs in a group
    @EditorGroupSetting([EditorGroupSetting.Category.TABS, EditorGroupSetting.Category.PERFORMANCE])
    var tabSizeLimitInt: Int by property(DEFAULT_TAB_SIZE_LIMIT)
  }

  @EditorGroupSetting([EditorGroupSetting.Category.REGEX, EditorGroupSetting.Category.GROUPS])
  var isSelectRegexGroup: Boolean
    get() = state.isSelectRegexGroup
    set(value) {
      state.isSelectRegexGroup = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
  var isAutoFolders: Boolean
    get() = state.isAutoFolders
    set(value) {
      state.isAutoFolders = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
  var isAutoSameName: Boolean
    get() = state.isAutoSameName
    set(value) {
      state.isAutoSameName = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
  var isForceSwitch: Boolean
    get() = state.isForceSwitch
    set(value) {
      state.isForceSwitch = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
  var isHideEmpty: Boolean
    get() = state.isHideEmpty
    set(value) {
      state.isHideEmpty = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS, EditorGroupSetting.Category.UI])
  var isShowSize: Boolean
    get() = state.isShowSize
    set(value) {
      state.isShowSize = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.TABS, EditorGroupSetting.Category.UI])
  var isColorTabs: Boolean
    get() = state.isColorTabs
    set(value) {
      state.isColorTabs = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.TABS])
  var isContinuousScrolling: Boolean
    get() = state.isContinuousScrolling
    set(value) {
      state.isContinuousScrolling = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.PERFORMANCE])
  var isInitializeSynchronously: Boolean
    get() = state.isInitializeSynchronously
    set(value) {
      state.isInitializeSynchronously = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.EGROUPS, EditorGroupSetting.Category.PERFORMANCE])
  var isIndexOnlyEditorGroupsFiles: Boolean
    get() = state.isIndexOnlyEditorGroupsFiles
    set(value) {
      state.isIndexOnlyEditorGroupsFiles = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.EGROUPS, EditorGroupSetting.Category.PERFORMANCE])
  var isExcludeEditorGroupsFiles: Boolean
    get() = state.isExcludeEditorGroupsFiles
    set(value) {
      state.isExcludeEditorGroupsFiles = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS])
  var isRememberLastGroup: Boolean
    get() = state.isRememberLastGroup
    set(value) {
      state.isRememberLastGroup = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.TABS, EditorGroupSetting.Category.UI])
  var isCompactTabs: Boolean
    get() = state.isCompactTabs
    set(value) {
      state.isCompactTabs = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS, EditorGroupSetting.Category.UI])
  var isGroupSwitchGroupAction: Boolean
    get() = state.isGroupSwitchGroupAction
    set(value) {
      state.isGroupSwitchGroupAction = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.UI])
  var isShowPanel: Boolean
    get() = state.isShowPanel
    set(value) {
      state.isShowPanel = value
    }

  @EditorGroupSetting([EditorGroupSetting.Category.GROUPS, EditorGroupSetting.Category.PERFORMANCE])
  var groupSizeLimit: Int
    get() = state.groupSizeLimitInt
    set(value) {
      state.groupSizeLimitInt = value.toInt().coerceIn(MIN_GROUP_SIZE_LIMIT, MAX_GROUP_SIZE_LIMIT)
    }

  @EditorGroupSetting([EditorGroupSetting.Category.TABS, EditorGroupSetting.Category.PERFORMANCE])
  var tabSizeLimit: Int
    get() = state.tabSizeLimitInt
    set(value) {
      state.tabSizeLimitInt = value.toInt().coerceIn(MIN_TAB_SIZE_LIMIT, MAX_TAB_SIZE_LIMIT)
    }

  @EditorGroupSetting([EditorGroupSetting.Category.REGEX, EditorGroupSetting.Category.GROUPS])
  var regexGroupModels: RegexGroupModels
    get() = state.regexGroupModels
    set(value) {
      state.regexGroupModels = value
    }

  companion object {
    const val DEFAULT_GROUP_SIZE_LIMIT: Int = 10_000
    const val MIN_GROUP_SIZE_LIMIT: Int = 1
    const val MAX_GROUP_SIZE_LIMIT: Int = 10_000

    const val DEFAULT_TAB_SIZE_LIMIT: Int = 50
    const val MIN_TAB_SIZE_LIMIT: Int = 1
    const val MAX_TAB_SIZE_LIMIT: Int = 100

    @JvmStatic
    val instance by lazy { service<EditorGroupsSettings>() }
  }
}
