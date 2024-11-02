package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil

/** Represents a link to a file path. */
class PathLink(path: String, project: Project?) : Link(project!!) {
  override val path: String = FileUtil.toSystemIndependentName(path)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    if (!super.equals(other)) return false

    val pathLink = other as PathLink

    return path == pathLink.path
  }

  override fun hashCode(): Int {
    var result = path.hashCode()
    result = 31 * result + (line?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String = "Link{line=$line, path='$path', icon=$icon}"
}
