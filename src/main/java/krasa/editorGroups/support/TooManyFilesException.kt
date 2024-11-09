package krasa.editorGroups.support

import krasa.editorGroups.support.Notifications.showWarning

class TooManyFilesException : RuntimeException() {
  fun showNotification(): Unit = showWarning(FOUND_TOO_MANY_MATCHING_FILES_SKIPPING)

  companion object {
    const val FOUND_TOO_MANY_MATCHING_FILES_SKIPPING: String = "Found too many matching files, skipping."
  }
}
