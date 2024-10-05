package krasa.editorGroups

import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.model.EditorGroup

data class SwitchRequest(
  val group: EditorGroup,
  val fileToOpen: VirtualFile,
  val myScrollOffset: Int = 0,
  val width: Int = 0,
  val line: Int? = null,
) {
  constructor(group: EditorGroup, fileToOpen: VirtualFile) : this(group, fileToOpen, 0, 0, null) {
  }

  override fun toString(): String =
    "SwitchRequest{, group=$group, fileToOpen=$fileToOpen, myScrollOffset=$myScrollOffset, width=$width, line=$line}"
}
