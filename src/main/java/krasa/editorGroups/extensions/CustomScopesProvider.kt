package krasa.editorGroups.extensions

import com.intellij.psi.search.scope.packageSet.CustomScopesProvider
import com.intellij.psi.search.scope.packageSet.NamedScope
import krasa.editorGroups.model.BookmarksGroup
import krasa.editorGroups.model.FolderGroup
import krasa.editorGroups.model.SameFeatureGroup
import krasa.editorGroups.model.SameNameGroup

internal class CustomScopesProvider : CustomScopesProvider {
  override fun getCustomScopes(): List<NamedScope> = listOf(
    SameNameGroup.SAME_NAME_GROUP_SCOPE,
    FolderGroup.FOLDER_GROUP_SCOPE,
    BookmarksGroup.BOOKMARKS_GROUP_SCOPE,
    SameFeatureGroup.SAME_FEATURE_GROUP_SCOPE
  )
}
