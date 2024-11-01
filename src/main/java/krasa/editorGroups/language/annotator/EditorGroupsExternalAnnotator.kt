package krasa.editorGroups.language.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiFile
import krasa.editorGroups.language.EditorGroupsPsiFile
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
        textAttributesKey = DefaultLanguageHighlighterColors.KEYWORD
      )
    )

    // Matches all colors and assign them the static field attribute
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.colorPattern,
        textAttributesKey = DefaultLanguageHighlighterColors.STATIC_FIELD
      )
    )

    // Matches all macros and assign them the static method attribute
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.macrosPattern,
        textAttributesKey = DefaultLanguageHighlighterColors.STATIC_METHOD
      )
    )

    // Matches all metadata and assign them the metadata attribute
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.metadataPattern,
        textAttributesKey = DefaultLanguageHighlighterColors.METADATA
      )
    )

    // Matches comments
    sourceAnnotationResult.addAll(
      annotateSyntaxHighlight(
        source = source,
        pattern = LanguagePatternHolder.commentPattern,
        textAttributesKey = DefaultLanguageHighlighterColors.LINE_COMMENT
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

  override fun apply(file: PsiFile, fileAnnotationResult: FileAnnotationResult?, holder: AnnotationHolder) {
    fileAnnotationResult?.annotate(holder)
  }
}
