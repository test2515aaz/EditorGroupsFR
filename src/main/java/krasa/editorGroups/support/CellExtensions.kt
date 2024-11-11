package krasa.editorGroups.support

import com.intellij.ui.FontComboBox
import com.intellij.ui.dsl.builder.Cell
import kotlin.reflect.KMutableProperty0

fun Cell<FontComboBox>.bind(property: KMutableProperty0<String?>, fallbackValue: String): Any {
  this.component.fontName = property.get() ?: fallbackValue
  this.component.addActionListener {
    property.set(
      this.component.fontName ?: return@addActionListener
    )
  }
  return this
}
