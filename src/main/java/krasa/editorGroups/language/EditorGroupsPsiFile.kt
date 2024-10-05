package krasa.editorGroups.language

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class EditorGroupsPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, EditorGroupsLanguage) {
  override fun getFileType(): FileType = EditorGroupsFileType

  override fun toString(): String = "EditorGroupsPsiFile{myOriginalFile=$myOriginalFile}"
}
