package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import krasa.editorGroups.icons.EditorGroupsIcons
import javax.swing.Icon

/** Find files that have the same name. */
class SameNameGroup(private val fileNameWithoutExtension: String, links: List<Link>) : AutoGroup(links) {
  override val id: String = SAME_FILE_NAME

  override val title: String = SAME_FILE_NAME

  override fun icon(): Icon = EditorGroupsIcons.copy

  override fun getPresentableTitle(project: Project, presentableNameForUI: String, showSize: Boolean): String = "By same file name"

  override fun toString(): String =
    "SameNameGroup{fileNameWithoutExtension='$fileNameWithoutExtension', links=$links, valid=$isValid, stub='$isStub'}"

  companion object {
    val INSTANCE: SameNameGroup = SameNameGroup("SAME_NAME_INSTANCE", emptyList())
  }
}
