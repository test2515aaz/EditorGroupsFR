package krasa.editorGroups.language.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.xml.util.ColorIconCache
import javax.swing.Icon

class SyntaxHighlightAnnotation(
  private val startSourceOffset: Int,
  private val endSourceOffset: Int,
  private val textAttributesKey: TextAttributesKey,
  private val text: String = "",
  private val isColor: Boolean = false
) {
  fun annotate(holder: AnnotationHolder, sourceOffset: Int) {
    val fileRange = TextRange.create(
      /* startOffset = */ startSourceOffset + sourceOffset,
      /* endOffset = */ endSourceOffset + sourceOffset
    )

    val infoAnnotation = when {
      text.isEmpty() -> holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(fileRange)
      else           -> holder.newAnnotation(HighlightSeverity.INFORMATION, text).range(fileRange)
    }

    infoAnnotation.textAttributes(textAttributesKey)

    if (isColor) {
      val color = textAttributesKey.defaultAttributes.backgroundColor
      val colorIcon = ColorIconCache.getIconCache().getIcon(color, 13)
      infoAnnotation.gutterIconRenderer(
        object : GutterIconRenderer() {
          override fun getIcon(): Icon = colorIcon

          override fun getTooltipText(): String? = text

          override fun equals(obj: Any?): Boolean = true

          override fun hashCode(): Int = 0
        }
      )
    }

    infoAnnotation.create()
  }
}
