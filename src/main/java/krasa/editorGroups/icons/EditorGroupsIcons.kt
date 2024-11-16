package krasa.editorGroups.icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object EditorGroupsIcons {
  val regex: Icon = AllIcons.Actions.Regex
  val folder: Icon = AllIcons.Nodes.Folder
  val settings: Icon = AllIcons.General.Settings
  val refresh: Icon = AllIcons.Actions.Refresh
  val bookmarks: Icon = AllIcons.Nodes.Bookmark
  val copy: Icon = AllIcons.Actions.Copy
  val feature: Icon = AllIcons.Actions.Search
  val hide: Icon = AllIcons.Actions.ToggleVisibility
  val groupBy: Icon = AllIcons.Actions.GroupBy
  val listFiles: Icon = AllIcons.Actions.ListFiles
  val logo: Icon = IconLoader.getIcon("/icons/pluginIcon.svg", javaClass)
}
