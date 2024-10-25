package krasa.editorGroups.model

import com.intellij.openapi.vfs.VirtualFile

/**
 * Data class representing a request to switch between files in an editor group.
 *
 * @property group The editor group where the file switch is to take place.
 * @property fileToOpen The virtual file that needs to be opened.
 * @property myScrollOffset The tabs' scroll offset
 * @property width The tabs' width
 * @property line The line number to scroll to, if specified.
 */
data class SwitchRequest(
  val group: EditorGroup,
  val fileToOpen: VirtualFile,
  val myScrollOffset: Int = 0,
  val width: Int = 0,
  val line: Int? = null,
) {
  constructor(group: EditorGroup, fileToOpen: VirtualFile) : this(group, fileToOpen, 0, 0, null)

  override fun toString(): String =
    "SwitchRequest{group=$group, fileToOpen=$fileToOpen, myScrollOffset=$myScrollOffset, width=$width, line=$line}"
}
