package krasa.editorGroups.support

import com.intellij.ide.util.treeView.FileNameComparator
import com.intellij.openapi.vfs.VirtualFile

class VirtualFileComparator : Comparator<VirtualFile> {
  override fun compare(o1: VirtualFile, o2: VirtualFile): Int = FileNameComparator.getInstance().compare(o1.path, o2.path)

  companion object {
    val instance: VirtualFileComparator = VirtualFileComparator()
  }
}
