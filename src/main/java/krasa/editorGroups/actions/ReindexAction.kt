package krasa.editorGroups.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.indexing.FileBasedIndex
import krasa.editorGroups.IndexCache
import krasa.editorGroups.index.EditorGroupIndex

class ReindexAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    thisLogger().debug("INDEXING START " + System.currentTimeMillis())
    IndexCache.getInstance(e.project!!).clear()
    FileBasedIndex.getInstance().requestRebuild(EditorGroupIndex.NAME)
  }

  companion object {
    const val ID = "krasa.editorGroups.Reindex"
  }
}
