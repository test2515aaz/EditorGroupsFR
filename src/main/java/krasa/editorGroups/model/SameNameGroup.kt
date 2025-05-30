package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.ui.FileColorManager
import krasa.editorGroups.icons.EditorGroupsIcons
import krasa.editorGroups.messages.EditorGroupsBundle.message
import org.jetbrains.annotations.NonNls
import java.awt.Color
import javax.swing.Icon

/** Find files that have the same name. */
class SameNameGroup(
  private val fileNameWithoutExtension: String,
  links: List<Link>,
  override val project: Project? = null
) : AutoGroup(links) {
  override val id: String = SAME_FILE_NAME
  override val title: String = SAME_FILE_NAME

  override val bgColor: Color?
    get() {
      val fileColorManager = FileColorManager.getInstance(project ?: return null)
      return fileColorManager.getScopeColor(SAME_NAME_GROUP_SCOPE_ID)
    }

  override fun icon(): Icon = EditorGroupsIcons.copy

  override fun getPresentableTitle(project: Project, presentableNameForUI: String, showSize: Boolean): String = message("by.same.file.name")

  override fun getTabTitle(project: Project, presentableNameForUI: String): String = presentableNameForUI

  override fun toString(): String =
    "SameNameGroup{fileNameWithoutExtension='$fileNameWithoutExtension', links=$links, valid=$isValid, stub='$isStub'}"

  class SameNameGroupScope : NamedScope(
    SAME_NAME_GROUP_SCOPE_ID,
    EditorGroupsIcons.groupBy,
    null
  ) {
    @NonNls
    override fun getDefaultColorName(): String = "Blue"

    override fun getPresentableName(): String = SAME_NAME_GROUP_SCOPE_NAME
  }

  companion object {
    val INSTANCE: SameNameGroup = SameNameGroup(
      fileNameWithoutExtension = "SAME_NAME_INSTANCE",
      links = emptyList(),
    )
    const val SAME_NAME_GROUP_SCOPE_ID: String = "krasa.editorGroups.model.SameNameGroup"
    val SAME_NAME_GROUP_SCOPE_NAME: String = message("group.same.name.scope")
    val SAME_NAME_GROUP_SCOPE: NamedScope = SameNameGroupScope()
  }
}
