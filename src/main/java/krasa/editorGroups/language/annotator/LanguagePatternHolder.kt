package krasa.editorGroups.language.annotator

import krasa.editorGroups.messages.EditorGroupsBundle.message
import krasa.editorGroups.support.colorSet
import java.util.regex.Pattern

object LanguagePatternHolder {
  const val GROUP_ID: String = "@group.id"
  const val GROUP_ROOT: String = "@group.root"
  const val GROUP_TITLE: String = "@group.title"
  const val GROUP_COLOR: String = "@group.color"
  const val GROUP_RELATED: String = "@group.related"
  const val GROUP_DISABLE: String = "@group.disable"
  const val GROUP_FG_COLOR: String = "@group.fgcolor"

  const val MODULE_MACRO: String = "MODULE/"
  const val PROJECT_MACRO: String = "PROJECT/"
  const val GLOBAL_MACRO: String = "*/"

  /** The list of autocomplete keywords. */
  val keywords: Set<String> = setOf(
    GROUP_ROOT,
    GROUP_RELATED,
    GROUP_COLOR,
    GROUP_FG_COLOR,
    GROUP_DISABLE,
    GROUP_TITLE
  ).map { it.substring(1) }.toSet()

  /** The list of autocomplete keywords with their descriptions. */
  val keywordsWithDescription: Map<String, String> = mapOf(
    GROUP_ID to message("keyword.group.id"),
    GROUP_ROOT to message("keyword.group.root"),
    GROUP_RELATED to message("keyword.group.related"),
    GROUP_COLOR to message("keyword.group.color"),
    GROUP_FG_COLOR to message("keyword.group.fgcolor"),
    GROUP_DISABLE to message("keyword.group.disable"),
    GROUP_TITLE to message("keyword.group.title")
  )

  /** The list of autocomplete colors. */
  val colors: Set<String> = colorSet

  val metadata: Set<String> = setOf(
    GROUP_ID
  ).map { it.substring(1) }.toSet()

  /** The list of autocomplete macros. */
  val macros: Set<String> = setOf(
    PROJECT_MACRO,
    MODULE_MACRO,
    GLOBAL_MACRO
  )

  /** The list of autocomplete macros with their descriptions. */
  val macrosWithDescription: MutableMap<String, String> = mutableMapOf(
    PROJECT_MACRO to message("project.macro"),
    MODULE_MACRO to message("current.module.macro"),
    GLOBAL_MACRO to message("anywhere.in.the.project")
  )

  /** Constants. */
  val constants: Set<String> = setOf(
    "true",
    "false"
  )

  /** The list of all autocomplete keywords and macros with their desc. */
  val allWithDescription: MutableMap<String, String> = mutableMapOf<String, String>().apply {
    putAll(keywordsWithDescription)
    putAll(macrosWithDescription)
  }

  /** Get the description for the provided keyword or macro. */
  fun getDescription(keywordOrMacro: String): String {
    val key = if (keywordOrMacro.startsWith("@")) keywordOrMacro else "@${keywordOrMacro}"
    return allWithDescription[key] ?: ""
  }

  @JvmField
  val keywordsPattern: Pattern = createPipePattern(tokens = keywords, patternPrefix = "[@]", caseSensitive = true)

  @JvmField
  val colorPattern: Pattern = createPipePattern(tokens = colors, patternPrefix = "", caseSensitive = false)

  @JvmField
  val metadataPattern: Pattern = createPipePattern(tokens = metadata, patternPrefix = "[@]", caseSensitive = true)

  @JvmField
  val macrosPattern: Pattern = createPipePattern(tokens = macros, patternPrefix = "", caseSensitive = true)

  @JvmField
  val constantsPattern: Pattern = createPipePattern(tokens = constants, patternPrefix = "", caseSensitive = true)

  @JvmField
  val commentPattern: Pattern = Pattern.compile("(?m)^\\s*#.*$")

  @JvmField
  val pathPattern: Pattern = Pattern.compile("(?m)^\\s*/.*$")

  /** Create a regex pattern to encapsulate the provided tokens. */
  private fun createPipePattern(tokens: Set<String>, patternPrefix: String, caseSensitive: Boolean): Pattern =
    tokens.joinToString("|") { "\\b$it\\b" } // NON-NLS
      .let { piped: String ->
        when {
          caseSensitive -> Pattern.compile("(${patternPrefix}${piped})")
          else          -> Pattern.compile("(?i:${patternPrefix}${piped})")
        }
      }
}
