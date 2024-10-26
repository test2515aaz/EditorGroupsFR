package krasa.editorGroups.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import krasa.editorGroups.EditorGroupManager
import krasa.editorGroups.settings.EditorGroupsSettings
import java.awt.Color

@Service(Service.Level.PROJECT)
class TabGroupColorizer {
  fun refreshTabs(event: FileEditorManagerEvent) {
    if (!EditorGroupsSettings.instance.isColorTabs) return

    val project = event.manager.project
    FileEditorManagerEx.getInstanceEx(project).windows
      .forEach { window ->
        window.tabbedPane.tabs.tabs.forEach { tabInfo ->
          tabInfo.setTabColor(getColor(project, tabInfo.text))
        }
      }
  }

  fun getColor(project: Project, tabInfo: @NlsContexts.TabTitle String): Color? {
    val lastGroup = EditorGroupManager.getInstance(project).lastGroup
    if (lastGroup.isStub) return null

    if (!lastGroup.containsLink(project, tabInfo)) return null

    return lastGroup.bgColor
  }

  companion object {
    fun getInstance(project: Project): TabGroupColorizer = project.getService<TabGroupColorizer>(TabGroupColorizer::class.java)
  }
}
