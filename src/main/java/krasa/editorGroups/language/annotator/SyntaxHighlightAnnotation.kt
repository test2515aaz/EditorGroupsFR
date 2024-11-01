package krasa.editorGroups.language.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange

class SyntaxHighlightAnnotation(
  private val startSourceOffset: Int,
  private val endSourceOffset: Int,
  private val textAttributesKey: TextAttributesKey,
  private val text: String
) {
  fun annotate(holder: AnnotationHolder, sourceOffset: Int) {
    val fileRange = TextRange.create(
      /* startOffset = */ startSourceOffset + sourceOffset,
      /* endOffset = */ endSourceOffset + sourceOffset
    )

    val infoAnnotation = holder.newAnnotation(HighlightSeverity.INFORMATION, text).range(fileRange)
    infoAnnotation.textAttributes(textAttributesKey)
    infoAnnotation.create()
  }
}
