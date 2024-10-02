package krasa.editorGroups.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import javax.swing.Icon

/** Represents a link to a file path. */
class PathLink(path: String, project: Project?) : Link(project!!) {
  override val path: String = FileUtil.toSystemIndependentName(path)

  constructor(path: String, icon: Icon?, line: Int?, project: Project?) : this(path, project) {
    this.icon = icon
    this.line = line
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    if (!super.equals(other)) return false

    val pathLink = other as PathLink

    return path == pathLink.path
  }

  override fun hashCode(): Int {
    var result = path.hashCode()
    result = 31 * result + (if (line != null) line.hashCode() else 0)
    return result
  }

  override fun toString(): String = "Link{line=$line, path='$path', icon=$icon}"
}
