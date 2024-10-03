package krasa.editorGroups.actions

import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewSettings
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ModuleGroup
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil

class AddToFavoritesAction(val chosenList: String?) : AnAction() {
  override fun actionPerformed(e: AnActionEvent) = Unit

  fun getNodesToAdd(dataContext: DataContext, inProjectView: Boolean): Collection<AbstractTreeNode<*>> {
    val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return emptyList()
    val moduleContext = LangDataKeys.MODULE_CONTEXT.getData(dataContext)

    var nodesToAdd: Collection<AbstractTreeNode<*>>? = null
    for (provider in FavoriteNodeProvider.EP_NAME.getExtensions(project)) {
      val nodes = provider.getFavoriteNodes(dataContext, ProjectViewSettings.Immutable.DEFAULT)
      if (nodes != null && !nodes.isEmpty()) {
        nodesToAdd = nodes
        break
      }
    }

    if (nodesToAdd == null) {
      val elements = collectSelectedElements(dataContext)
      if (elements != null) {
        nodesToAdd = createNodes(project, moduleContext, elements, inProjectView, ProjectViewSettings.Immutable.DEFAULT)
      }
    }
    return nodesToAdd ?: emptyList()
  }

  fun retrieveData(obj: Any?, data: Any?): Any? = obj ?: data

  private fun collectSelectedElements(dataContext: DataContext): Any? {
    var elements = retrieveData(null, CommonDataKeys.PSI_ELEMENT.getData(dataContext))
    elements = retrieveData(elements, LangDataKeys.PSI_ELEMENT_ARRAY.getData(dataContext))
    elements = retrieveData(elements, CommonDataKeys.PSI_FILE.getData(dataContext))
    elements = retrieveData(elements, ModuleGroup.ARRAY_DATA_KEY.getData(dataContext))
    elements = retrieveData(elements, LangDataKeys.MODULE_CONTEXT_ARRAY.getData(dataContext))
    elements = retrieveData(elements, LibraryGroupElement.ARRAY_DATA_KEY.getData(dataContext))
    elements = retrieveData(elements, NamedLibraryElement.ARRAY_DATA_KEY.getData(dataContext))
    elements = retrieveData(elements, CommonDataKeys.VIRTUAL_FILE.getData(dataContext))
    elements = retrieveData(elements, CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext))
    return elements
  }

  fun createNodes(
    project: Project?,
    moduleContext: Module?,
    selectedObject: Any?,
    inProjectView: Boolean,
    favoritesConfig: ViewSettings
  ): Collection<AbstractTreeNode<*>> {
    var obj = selectedObject
    if (project == null) return emptyList()
    val result = ArrayList<AbstractTreeNode<*>>()

    for (provider in FavoriteNodeProvider.EP_NAME.getExtensions(project)) {
      val treeNode = provider.createNode(project, obj, favoritesConfig)
      if (treeNode != null) {
        result.add(treeNode)
        return result
      }
    }

    val psiManager = PsiManager.getInstance(project)
    val currentViewId = ProjectView.getInstance(project).currentViewId
    val pane = ProjectView.getInstance(project).getProjectViewPaneById(currentViewId)

    // on psi elements
    if (obj is Array<*> && obj.isArrayOf<PsiElement>()) {
      for (psiElement in obj as Array<PsiElement?>) {
        addPsiElementNode(psiElement, project, result, favoritesConfig)
      }
      return result
    }

    // on psi element
    if (obj is PsiElement) {
      addPsiElementNode(obj as? PsiElement?, project, result, favoritesConfig)
      return result
    }

    if (obj is Array<*> && obj.isArrayOf<VirtualFile>()) {
      for (vFile in obj as Array<VirtualFile?>) {
        var element: PsiElement? = psiManager.findFile(vFile!!)
        if (element == null) element = psiManager.findDirectory(vFile)
        addPsiElementNode(
          element,
          project,
          result,
          favoritesConfig
        )
      }
      return result
    }

    // on form in editor
    if (obj is VirtualFile) {
      val psiFile = psiManager.findFile(obj)
      addPsiElementNode(psiFile, project, result, favoritesConfig)
      return result
    }

    // on module groups
    if (obj is Array<*> && obj.isArrayOf<ModuleGroup>()) {
      for (moduleGroup in obj as Array<ModuleGroup?>) {
        result.add(ProjectViewModuleGroupNode(project, moduleGroup!!, favoritesConfig))
      }
      return result
    }

    // on module nodes
    if (obj is Module) obj = arrayOf(obj)
    if (obj is Array<*> && obj.isArrayOf<Module>()) {
      for (module1 in obj as Array<Module?>) {
        result.add(ProjectViewModuleNode(project, module1!!, favoritesConfig))
      }
      return result
    }

    // on library group node
    if (obj is Array<*> && obj.isArrayOf<LibraryGroupElement>()) {
      for (libraryGroup in obj as Array<LibraryGroupElement?>) {
        result.add(LibraryGroupNode(project, libraryGroup!!, favoritesConfig))
      }
      return result
    }

    // on named library node
    if (obj is Array<*> && obj.isArrayOf<NamedLibraryElement>()) {
      for (namedLibrary in obj as Array<NamedLibraryElement?>) {
        result.add(NamedLibraryElementNode(project, namedLibrary!!, favoritesConfig))
      }
      return result
    }
    return result
  }

  private fun addPsiElementNode(
    psiElement: PsiElement?,
    project: Project,
    result: ArrayList<in AbstractTreeNode<*>>,
    favoritesConfig: ViewSettings
  ) {
    var myPsiElement = psiElement
    var klass = getPsiElementNodeClass(myPsiElement)

    if (klass == null) {
      myPsiElement = PsiTreeUtil.getParentOfType(myPsiElement, PsiFile::class.java)
      if (myPsiElement != null) {
        klass = PsiFileNode::class.java
      }
    }

    val value: Any? = myPsiElement
    try {
      if (klass != null && value != null) {
        result.add(ProjectViewNode.createTreeNode(klass, project, value, favoritesConfig))
      }
    } catch (e: Exception) {
      LOG.error(e)
    }
  }

  private fun getPsiElementNodeClass(psiElement: PsiElement?): Class<out AbstractTreeNode<*>?>? {
    var klass: Class<out AbstractTreeNode<*>?>? = null
    when (psiElement) {
      is PsiFile      -> klass = PsiFileNode::class.java
      is PsiDirectory -> klass = PsiDirectoryNode::class.java
    }
    return klass
  }

  companion object {
    private val LOG = Logger.getInstance(AddToFavoritesAction::class.java)
  }
}
