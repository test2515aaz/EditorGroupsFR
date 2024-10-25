package krasa.editorGroups.model

class RegexGroupModels {
  var regexGroupModels: MutableList<RegexGroupModel> = MutableList(0) { RegexGroupModel() }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false

    val that = other as RegexGroupModels

    return regexGroupModels == that.regexGroupModels
  }

  override fun hashCode(): Int = regexGroupModels.hashCode()

  override fun toString(): String = "RegExpGroupModels{models=$regexGroupModels}"

  fun findFirstMatching(fileName: String?): RegexGroupModel? = regexGroupModels.firstOrNull { it.matches(fileName!!) }

  fun findMatching(fileName: String?): List<RegexGroupModel> = regexGroupModels.filter { it.matches(fileName!!) }

  fun find(substring: String): RegexGroupModel? {
    val deserializedGroup = RegexGroupModel.deserialize(substring) ?: return null
    return regexGroupModels.find { it.isEnabled && it.regex == deserializedGroup.regex }
  }

  fun findProjectRegexGroups(): List<RegexGroupModel> = regexGroupModels.filter { it.scope == RegexGroupModel.Scope.WHOLE_PROJECT }
}
