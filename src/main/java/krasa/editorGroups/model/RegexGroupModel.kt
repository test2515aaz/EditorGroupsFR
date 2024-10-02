package krasa.editorGroups.model

import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

/** Regex group model. */
class RegexGroupModel {
  /** Is enabled. */
  var isEnabled: Boolean = true

  /** Regex. */
  var regex: String? = null
    set(value) {
      field = value
      regexPattern = null
    }

  /** The regex group matches to avoid comparing. */
  var notComparingGroups: String? = ""
    set(value) {
      if (value != null && value.contains("|")) throw IllegalArgumentException("notComparingGroups must not contain '|'")
      field = value
      notComparingGroupsIntArray = null
    }

  /** Scope. */
  var scope: Scope? = Scope.CURRENT_FOLDER
    set(value) {
      field = value ?: Scope.CURRENT_FOLDER
    }

  @Transient
  var regexPattern: Pattern? = null
    get() {
      if (field == null) field = regex?.let { Pattern.compile(it) }
      return field
    }

  @Transient
  private var notComparingGroupsIntArray: IntArray? = null

  constructor()

  constructor(regex: String?, scope: Scope?, notComparingGroups: String?) {
    this.regex = regex
    this.scope = scope
    this.notComparingGroups = notComparingGroups
  }

  fun serialize(): String = "v1|$scope|$notComparingGroups|$regex"

  fun matches(name: String): Boolean {
    try {
      return regexPattern?.matcher(name)?.matches() ?: false
    } catch (e: Exception) {
      LOG.error(e)
    }
    return false
  }

  fun copy(): RegexGroupModel = RegexGroupModel(regex, scope, notComparingGroups)

  fun isComparingGroup(groupIndex: Int): Boolean {
    if (notComparingGroupsIntArray == null) notComparingGroupsIntArray = getNotComparingGroupsAsIntArray()

    return groupIndex !in notComparingGroupsIntArray!!
  }

  /**
   * Split the not comparing groups string and convert it to an int array.
   * Ex: 1,2 -> [1,2] or 1,,2 -> [1,-1,2]
   *
   * @return the int array
   */
  private fun getNotComparingGroupsAsIntArray(): IntArray {
    if (StringUtils.isBlank(notComparingGroups)) return IntArray(0)

    val split = notComparingGroups!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val size = split.size
    val arr = IntArray(size)

    for (i in 0 until size) {
      try {
        var s = split[i]
        if (StringUtils.isBlank(s)) s = "-1"
        arr[i] = s.toInt()
      } catch (e: Exception) {
        LOG.error(e)
        arr[i] = -1
      }
    }
    return arr
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false

    val that = other as RegexGroupModel

    if (isEnabled != that.isEnabled) return false
    if (regex != that.regex) return false
    if (notComparingGroups != that.notComparingGroups) return false
    return scope == that.scope
  }

  override fun hashCode(): Int {
    var result = if (regex != null) regex.hashCode() else 0
    result = 31 * result + (if (notComparingGroups != null) notComparingGroups.hashCode() else 0)
    result = 31 * result + (if (scope != null) scope.hashCode() else 0)
    result = 31 * result + (if (isEnabled) 1 else 0)
    return result
  }

  override fun toString(): String {
    return "RegexGroupModel{regex='$regex', notComparingGroups='$notComparingGroups', scope=$scope, enabled=$isEnabled}"
  }

  enum class Scope {
    CURRENT_FOLDER,
    INCLUDING_SUBFOLDERS,
    WHOLE_PROJECT
  }

  companion object {
    private val LOG = Logger.getInstance(RegexGroupModel::class.java)

    /**
     * Converts a string into a [RegexGroupModel].
     *
     * @param str stirng
     * @return
     */
    @JvmStatic
    fun deserialize(str: String): RegexGroupModel? {
      try {
        when {
          str.startsWith("v0") -> {
            val scopeEnd = str.indexOf("|", 3)
            val scope = str.substring(3, scopeEnd)
            val regex = str.substring(scopeEnd + 1)
            return RegexGroupModel(regex, Scope.valueOf(scope), "")
          }

          str.startsWith("v1") -> {
            val scopeEnd = str.indexOf("|", 3)
            val scope = str.substring(3, scopeEnd)

            val notComparingGroupsEnd = str.indexOf("|", scopeEnd + 1)
            val notComparingGroups = str.substring(scopeEnd + 1, notComparingGroupsEnd)

            val regex = str.substring(notComparingGroupsEnd + 1)
            return RegexGroupModel(regex, Scope.valueOf(scope), notComparingGroups)
          }

          else                 -> throw RuntimeException("not supported")
        }
      } catch (e: Throwable) {
        LOG.warn("$e; source='$str'")
        return null
      }
    }
  }
}
