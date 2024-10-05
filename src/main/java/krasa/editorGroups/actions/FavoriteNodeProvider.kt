package krasa.editorGroups.actions

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.NonNls

abstract class FavoriteNodeProvider {

  /**
   * Returns the identifier used to persist favorites for this provider.
   *
   * @return the string identifier.
   */
  abstract val favoriteTypeId: @NonNls String

  abstract fun getFavoriteNodes(context: DataContext?, viewSettings: ViewSettings): Collection<AbstractTreeNode<*>>?

  fun createNode(project: Project?, element: Any?, viewSettings: ViewSettings): AbstractTreeNode<*>? = null

  /**
   * Checks if the specified project view node element (the value of
   * [AbstractTreeNode]) contains the specified virtual file as one of its
   * children.
   *
   * @param element the value element of a project view node.
   * @param vFile the file to check.
   * @return true if the file is contained, false if not or if `element` is
   *    not an element supported by this provider.
   */
  abstract fun elementContainsFile(element: Any?, vFile: VirtualFile?): Boolean

  /**
   * Returns the weight of the specified project view node element to use
   * when sorting the favorites list.
   *
   * @param element the element for which the weight is requested.
   * @param isSortByType true if the favorites list is sorted by type, false
   * @return the weight, or -1 if `element` is not an element supported by
   *    this provider.
   */
  abstract fun getElementWeight(element: Any?, isSortByType: Boolean): Int

  /**
   * Returns the location text (grey text in parentheses) to display in the
   * Favorites view for the specified element.
   *
   * @param element the element for which the location is requested.
   * @return the location text, or -1 if `element` is not an element
   *    supported by this provider.
   */
  abstract fun getElementLocation(element: Any?): @NlsSafe String?

  /**
   * Checks if the specified element is invalid and needs to be removed from
   * the tree.
   *
   * @param element the element to check.
   * @return true if the element is invalid, false if the element is valid or
   *    not supported by this provider.
   */
  abstract fun isInvalidElement(element: Any?): Boolean

  /**
   * Returns the persistable URL for the specified element.
   *
   * @return the URL, or null if the element is not supported by this
   *    provider.
   */
  abstract fun getElementUrl(element: Any?): @NonNls String?

  /**
   * Returns the name of the module containing the specified element.
   *
   * @return the name of the module, or null if the element is not supported
   *    by this provider or the module name is unknown.
   */
  abstract fun getElementModuleName(element: Any?): String?

  /**
   * Returns the path of node objects to be added to the favorites tree for
   * the specified persisted URL and module name.
   *
   * @param project the project to which the favorite is related.
   * @param url the loaded URL (initially returned from [.getElementUrl]).
   * @param moduleName the name of the module containing the element
   *    (initially returned from [.getElementModuleName])
   * @return the path of objects to be added to the tree, or null if it was
   *    not possible to locate an object with the specified URL.
   */
  abstract fun createPathFromUrl(project: Project?, url: String?, moduleName: String?): Array<Any?>?

  fun getPsiElement(element: Any?): PsiElement? = element as? PsiElement

  companion object {
    val EP_NAME: ExtensionPointName<FavoriteNodeProvider> = ExtensionPointName("com.intellij.favoriteNodeProvider")
  }
}
