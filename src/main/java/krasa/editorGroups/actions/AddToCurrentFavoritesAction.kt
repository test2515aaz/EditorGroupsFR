package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import krasa.editorGroups.EditorGroupPanel
import krasa.editorGroups.model.FavoritesGroup
import java.util.*

class AddToCurrentFavoritesAction : EditorGroupsAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun actionPerformed(e: AnActionEvent) {
    val favoritesGroup = getFavoritesGroup(e)
    if (favoritesGroup != null) {
      AddToFavoritesAction(favoritesGroup.title).actionPerformed(e)
    }
  }

  override fun update(e: AnActionEvent) {
    val presentation = e.presentation
    val favoritesGroup = getFavoritesGroup(e)

    presentation.isVisible = favoritesGroup != null

    if (favoritesGroup != null) {
      presentation.setText("Add to Favorites - ${favoritesGroup.title}")
      presentation.isEnabled = isEnabled(e, favoritesGroup)
    }
  }

  private fun getFavoritesGroup(e: AnActionEvent): FavoritesGroup? {
    var editorGroupPanel: EditorGroupPanel? = null

    val eventProject = getEventProject(e) ?: return null
    val fileEditorManagerEx = FileEditorManager.getInstance(eventProject) as FileEditorManagerEx

    val currentWindow = fileEditorManagerEx.currentWindow
    if (currentWindow != null) {
      val editor = currentWindow.getSelectedComposite(true)
      if (editor != null) {
        val selectedEditor = editor.selectedWithProvider!!.fileEditor
        editorGroupPanel = selectedEditor.getUserData(EditorGroupPanel.EDITOR_PANEL)
      }
    }

    var favoritesGroup: FavoritesGroup? = null
    if (editorGroupPanel != null) {
      val displayedGroup = editorGroupPanel.displayedGroup
      if (displayedGroup is FavoritesGroup) {
        favoritesGroup = displayedGroup
      }
    }
    return favoritesGroup
  }

  private fun isEnabled(e: AnActionEvent, favoritesGroup: FavoritesGroup): Boolean {
    val project = e.project
    var enabled = true
    val dataContext = e.dataContext
    val data1 = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext)
    if (data1 != null) {
      var everyFileIsContained = true
      for (virtualFile in data1) {
        everyFileIsContained = favoritesGroup.containsLink(project!!, virtualFile)
        if (!everyFileIsContained) {
          break
        }
      }
      if (everyFileIsContained) {
        enabled = false
      }
    }
    return enabled
  }
}
