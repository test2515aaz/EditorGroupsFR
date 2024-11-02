package krasa.editorGroups.language.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiFile
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import krasa.editorGroups.colorscheme.*
import krasa.editorGroups.language.EditorGroupsPsiFile
import krasa.editorGroups.support.getColorInstance
import java.awt.Font
import java.util.UUID.randomUUID
import java.util.regex.Pattern

/** An external annotator for the EditorGroups language. */
internal class EditorGroupsExternalAnnotator : ExternalAnnotator<EditorGroupsPsiFile, FileAnnotationResult>() {
  override fun collectInformation(file: PsiFile): EditorGroupsPsiFile? = when (file) {
    is EditorGroupsPsiFile -> file
    else                   -> null
  }

  override fun doAnnotate(file: EditorGroupsPsiFile?): FileAnnotationResult? {
    val fileAnnotationResult = FileAnnotationResult()
    val sourceAnnotationResult = SourceAnnotationResult()

    val source = file?.firstChild?.text ?: return null

    // Matches all keywords and assign them the keyword text attribute
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.keywordsPattern,
        textAttributesKey = EG_KEYWORD
      )
    )

    // Matches all colors and assign them the static field attribute
    sourceAnnotationResult.addAll(
      annotateColor(
        source = source,
      )
    )

    // Matches all macros and assign them the static method attribute
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.macrosPattern,
        textAttributesKey = EG_MACRO
      )
    )

    // Matches all metadata and assign them the metadata attribute
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.metadataPattern,
        textAttributesKey = EG_METADATA
      )
    )

    // Matches comments
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.commentPattern,
        textAttributesKey = EG_COMMENT
      )
    )

    // Matches constants
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.constantsPattern,
        textAttributesKey = EG_CONSTANT
      )
    )

    // Matches paths
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.pathPattern,
        textAttributesKey = EG_PATH
      )
    )

    fileAnnotationResult.add(sourceAnnotationResult)

    return fileAnnotationResult
  }

  private fun annotateSyntaxHighlight(
    source: String,
    pattern: Pattern,
    textAttributesKey: TextAttributesKey
  ): MutableList<SyntaxHighlightAnnotation> {
    val result = mutableListOf<SyntaxHighlightAnnotation>()

    val matcher = pattern.matcher(source)
    while (matcher.find()) {
      result.add(
        SyntaxHighlightAnnotation(
          startSourceOffset = matcher.start(),
          endSourceOffset = matcher.end(),
          text = LanguagePatternHolder.getDescription(matcher.group()),
          textAttributesKey = textAttributesKey
        )
      )
    }

    return result
  }

  private fun annotateColor(source: String): MutableList<SyntaxHighlightAnnotation> {
    val result = mutableListOf<SyntaxHighlightAnnotation>()
    val matcher = LanguagePatternHolder.colorPattern.matcher(source)
    val fallbackKey = EG_COLOR

    while (matcher.find()) {
      val color = getColorInstance(matcher.group())
      val textAttribute = when (color) {
        null -> fallbackKey
        else -> {
          val textAttributes = TextAttributes()
          textAttributes.backgroundColor = color
          textAttributes.foregroundColor = if (ColorUtil.isDark(color)) JBColor.white else JBColor.black
          textAttributes.fontType = Font.ITALIC

          TextAttributesKey.createTempTextAttributesKey("EDITOR_GROUP_COLOR_${randomUUID()}", textAttributes)
        }
      }

      result.add(
        SyntaxHighlightAnnotation(
          startSourceOffset = matcher.start(),
          endSourceOffset = matcher.end(),
          textAttributesKey = textAttribute,
          isColor = true
        )
      )
    }

    return result
  }

  override fun apply(file: PsiFile, fileAnnotationResult: FileAnnotationResult?, holder: AnnotationHolder) {
    fileAnnotationResult?.annotate(holder)
  }
}
