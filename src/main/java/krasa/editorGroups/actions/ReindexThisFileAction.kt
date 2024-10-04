package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.util.indexing.FileBasedIndex

class ReindexThisFileAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val data = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
    if (data != null) {
      FileBasedIndex.getInstance().requestReindex(data)
    }
  }

  companion object {
    const val ID = "krasa.editorGroups.ReindexThisFile"
  }
}
