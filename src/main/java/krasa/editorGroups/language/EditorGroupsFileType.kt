package krasa.editorGroups.language

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.icons.EditorGroupsIcons
import javax.swing.Icon

class EditorGroupsFileType private constructor() : LanguageFileType(EditorGroupsLanguage.INSTANCE) {
  override fun getName(): String = "EditorGroups file"

  override fun getDescription(): String = "EditorGroups files"

  override fun getDefaultExtension(): String = EXTENSION

  override fun getIcon(): Icon = EditorGroupsIcons.groupBy

  override fun getCharset(virtualFile: VirtualFile, bytes: ByteArray): String? = "UTF-8"

  companion object {
    @JvmField
    val EDITOR_GROUPS_FILE_TYPE: EditorGroupsFileType = EditorGroupsFileType()

    const val EXTENSION: String = "egroups"
  }
}
