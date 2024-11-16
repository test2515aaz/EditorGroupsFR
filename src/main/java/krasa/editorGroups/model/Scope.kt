package krasa.editorGroups.model

import krasa.editorGroups.messages.EditorGroupsBundle.message

enum class Scope(val label: String) {
  CURRENT_FOLDER(message("scope.currentFolder")),
  INCLUDING_SUBFOLDERS(message("scope.includeSubfolders")),
  WHOLE_PROJECT(message("scope.wholeProject"))
}
