package krasa.editorGroups.support

import com.intellij.ide.util.treeView.FileNameComparator
import com.intellij.openapi.vfs.VirtualFile

open class VirtualFileComparator protected constructor() : Comparator<VirtualFile> {
  override fun compare(
    o1: VirtualFile,
    o2: VirtualFile
  ): Int = FileNameComparator.getInstance().compare(o1.path, o2.path)

  companion object {
    @JvmField
    val INSTANCE: VirtualFileComparator = VirtualFileComparator()
  }
}
