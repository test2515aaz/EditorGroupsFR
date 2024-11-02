package krasa.editorGroups.index

import com.intellij.openapi.util.text.StringUtil
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

@Suppress("DataClassShouldBeImmutable", "DataClassContainsFunctions")
data class MyIndexPattern(
  private var regexPattern: String
) {
  var optimizedIndexingPattern: Pattern? = null
    private set

  private var pattern: Pattern? = null

  init {
    compilePattern()
  }

  private fun compilePattern() {
    try {
      val flags = 0
      this.pattern = Pattern.compile(regexPattern, flags)

      var optimizedPattern = regexPattern
      optimizedPattern = StringUtil.trimStart(optimizedPattern, ".*")

      this.optimizedIndexingPattern = Pattern.compile(optimizedPattern, flags)
    } catch (_: PatternSyntaxException) {
      this.pattern = null
      this.optimizedIndexingPattern = null
    }
  }
}
