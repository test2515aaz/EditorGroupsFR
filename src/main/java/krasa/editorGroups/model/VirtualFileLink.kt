package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

/** A link with the associated virtual file. */
class VirtualFileLink : Link {
  override val virtualFile: VirtualFile

  constructor(virtualFile: VirtualFile, project: Project) : super(project) {
    this.virtualFile = virtualFile
  }

  constructor(file: VirtualFile, icon: Icon?, line: Int, project: Project) : super(icon, line, project) {
    this.virtualFile = file
  }

  fun isTheSameFile(file: VirtualFile): Boolean = fileEquals(file)

  override val path: String
    get() = virtualFile.path

  override fun exists(): Boolean = virtualFile.exists()

  override val name: String
    get() = virtualFile.presentableName

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    if (!super.equals(other)) return false

    val that = other as VirtualFileLink

    return virtualFile == that.virtualFile
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + virtualFile.hashCode()
    return result
  }

  override fun fileEquals(currentFile: VirtualFile): Boolean = virtualFile == currentFile

  override fun toString(): String = "VirtualFileLink{virtualFile=$virtualFile, icon=$icon, line=$line}"

}
