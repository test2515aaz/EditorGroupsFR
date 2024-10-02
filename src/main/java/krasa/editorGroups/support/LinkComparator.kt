package krasa.editorGroups.support

import com.intellij.ide.util.treeView.FileNameComparator
import krasa.editorGroups.model.Link

open class LinkComparator protected constructor() : Comparator<Link> {
  override fun compare(link1: Link, link2: Link): Int {
    val s1 = link1.path
    val s2 = link2.path

    return FileNameComparator.getInstance().compare(s1, s2)
  }

  companion object {
    @JvmField
    val INSTANCE: LinkComparator = LinkComparator()
  }
}
