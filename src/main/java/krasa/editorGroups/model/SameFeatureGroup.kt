package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.ui.FileColorManager
import krasa.editorGroups.icons.EditorGroupsIcons
import krasa.editorGroups.messages.EditorGroupsBundle.message
import org.jetbrains.annotations.NonNls
import java.awt.Color
import javax.swing.Icon

/** Find files that have the same feature (e.g. test.service|controller). */
class SameFeatureGroup(
  private val fileNameWithoutExtension: String,
  links: List<Link>,
  override val project: Project? = null
) : AutoGroup(links) {
  override val id: String = SAME_FEATURE
  override val title: String = SAME_FEATURE

  override val bgColor: Color?
    get() {
      val fileColorManager = FileColorManager.getInstance(project ?: return null)
      return fileColorManager.getScopeColor(SAME_FEATURE_GROUP_SCOPE_ID)
    }

  override fun icon(): Icon = EditorGroupsIcons.feature

  override fun getPresentableTitle(project: Project, presentableNameForUI: String, showSize: Boolean): String =
    message("by.same.logical.name")

  override fun getTabTitle(project: Project, presentableNameForUI: String): String = presentableNameForUI

  override fun toString(): String =
    "SameFeatureGroup{fileNameWithoutExtension='$fileNameWithoutExtension', links=$links, valid=$isValid, stub='$isStub'}"

  class SameFeatureGroupScope : NamedScope(
    SAME_FEATURE_GROUP_SCOPE_ID,
    EditorGroupsIcons.groupBy,
    null
  ) {
    @NonNls
    override fun getDefaultColorName(): String = "Yellow"

    override fun getPresentableName(): String = SAME_FEATURE_GROUP_SCOPE_NAME
  }

  companion object {
    val INSTANCE: SameFeatureGroup = SameFeatureGroup(
      fileNameWithoutExtension = "SAME_FEATURE_INSTANCE",
      links = emptyList(),
    )
    const val SAME_FEATURE_GROUP_SCOPE_ID: String = "krasa.editorGroups.model.SameFeatureGroup"
    val SAME_FEATURE_GROUP_SCOPE_NAME: String = message("group.same.feature.scope")
    val SAME_FEATURE_GROUP_SCOPE: NamedScope = SameFeatureGroupScope()
  }
}
