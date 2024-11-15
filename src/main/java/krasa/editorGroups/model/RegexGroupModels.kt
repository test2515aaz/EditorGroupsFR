package krasa.editorGroups.model

import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection

@Tag("regexGroups")
class RegexGroupModels : BaseState() {
  @get:XCollection
  var regexModels: MutableList<RegexGroupModel> by list<RegexGroupModel>()

  override fun toString(): String = "RegExpGroupModels{models=$regexModels}"

  fun findFirstMatching(fileName: String?): RegexGroupModel? = regexModels.firstOrNull { it.matches(fileName!!) }

  fun findMatching(fileName: String?): List<RegexGroupModel> = regexModels.filter { it.matches(fileName!!) }

  fun find(substring: String): RegexGroupModel? {
    val deserializedGroup = RegexGroupModel.deserialize(substring) ?: return null
    return regexModels.find { it.isEnabled && it.myRegex == deserializedGroup.myRegex }
  }

  fun findProjectRegexGroups(): List<RegexGroupModel> = regexModels.filter {
    it.myScope == Scope.WHOLE_PROJECT
  }
}
