package krasa.editorGroups.model

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection

@Tag("regexGroups")
class RegexGroupModels : BaseState() {
  @get:XCollection
  var regexGroupModels: MutableList<RegexGroupModel> by list<RegexGroupModel>()

  override fun toString(): String = "RegExpGroupModels{models=$regexGroupModels}"

  fun findFirstMatching(fileName: String?): RegexGroupModel? = regexGroupModels.firstOrNull { it.matches(fileName!!) }

  fun findMatching(fileName: String?): List<RegexGroupModel> = regexGroupModels.filter { it.matches(fileName!!) }

  fun find(substring: String): RegexGroupModel? {
    val deserializedGroup = RegexGroupModel.deserialize(substring) ?: return null
    return regexGroupModels.find { it.isEnabled && it.myRegex == deserializedGroup.myRegex }
  }

  fun findProjectRegexGroups(): List<RegexGroupModel> = regexGroupModels.filter { it.myScope == RegexGroupModel.Scope.WHOLE_PROJECT }
}
