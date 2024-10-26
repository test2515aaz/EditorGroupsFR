package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.util.indexing.FileBasedIndex
import krasa.editorGroups.support.Notifications

class ReindexThisFileAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val data = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext) ?: return
    FileBasedIndex.getInstance().requestReindex(data)
    Notifications.notifySimple("Reindexing started for ${data.name}")
  }

  companion object {
    const val ID = "krasa.editorGroups.ReindexThisFile"
  }
}
