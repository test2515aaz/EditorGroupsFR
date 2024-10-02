package krasa.editorGroups.model

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import javax.swing.Icon

class StubGroup : EditorGroup() {
  override var isStub = true

  override val id: String = ID

  override val title: String = ""

  override val isValid: Boolean = true

  override fun icon(): Icon = AllIcons.Actions.GroupByModule

  override fun invalidate() {}

  override fun size(project: Project): Int = 0

  override fun getLinks(project: Project): List<Link> = emptyList()

  override fun isOwner(ownerPath: String): Boolean = false

  companion object {
    const val ID: String = "STUB_GROUP"
  }
}
