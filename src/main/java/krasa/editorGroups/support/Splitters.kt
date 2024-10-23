package krasa.editorGroups.support

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.BitUtil
import java.awt.event.InputEvent
import javax.swing.SwingConstants

enum class Splitters {
  NONE,
  VERTICAL,
  HORIZONTAL;

  val isSplit: Boolean
    get() = this != NONE

  val orientation: Int
    get() = when (HORIZONTAL) {
      this -> SwingConstants.HORIZONTAL
      else -> SwingConstants.VERTICAL
    }

  companion object {
    @JvmStatic
    fun from(alt: Boolean, shift: Boolean): Splitters = when {
      alt && shift -> HORIZONTAL
      alt          -> VERTICAL
      else         -> NONE
    }

    fun from(alt: Boolean): Splitters = when {
      alt  -> VERTICAL
      else -> NONE
    }

    fun from(e: InputEvent): Splitters = from(BitUtil.isSet(e.modifiersEx, InputEvent.ALT_DOWN_MASK))

    @JvmStatic
    fun from(e: AnActionEvent): Splitters {
      val inputEvent = e.inputEvent ?: return NONE
      val alt = BitUtil.isSet(inputEvent.modifiersEx, InputEvent.ALT_DOWN_MASK)
      val shift = BitUtil.isSet(inputEvent.modifiersEx, InputEvent.SHIFT_DOWN_MASK)
      return when {
        alt && shift -> HORIZONTAL
        else         -> from(alt)
      }
    }
  }
}
