package krasa.editorGroups.model

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import krasa.editorGroups.IndexCache
import krasa.editorGroups.support.getColorInstance
import java.awt.Color
import java.util.*
import javax.swing.Icon
import kotlin.concurrent.Volatile

/** Represents an editorGroup but for indexing. */
class EditorGroupIndexValue : EditorGroup {
  override var id = ""
    set(value) {
      field = StringUtil.notNullize(value)
    }

  override var ownerPath = ""
    set(value) {
      field = FileUtil.toSystemIndependentName(value)
    }

  override var title = ""
    set(value) {
      field = StringUtil.notNullize(value)
    }

  var root: String? = ""

  var backgroundColor: String = ""
    set(value) {
      field = StringUtil.notNullize(value).lowercase(Locale.getDefault())
    }

  var foregroundColor: String = ""
    set(value) {
      field = StringUtil.notNullize(value).lowercase(Locale.getDefault())
    }

  override val bgColor: Color?
    get() {
      if (bgColorInstance != null) return bgColorInstance

      if (backgroundColor.isNotEmpty()) {
        try {
          bgColorInstance = when {
            backgroundColor.startsWith("0x") || backgroundColor.startsWith("#") -> Color.decode(backgroundColor)
            else                                                                -> getColorInstance(backgroundColor)
          }
        } catch (ignored: Exception) {
        }
      }

      return bgColorInstance
    }

  override val fgColor: Color?
    get() {
      if (fgColorInstance != null) return fgColorInstance

      if (foregroundColor.isNotEmpty()) {
        try {
          fgColorInstance = when {
            foregroundColor.startsWith("0x") || foregroundColor.startsWith("#") -> Color.decode(foregroundColor)
            else                                                                -> getColorInstance(foregroundColor)
          }
        } catch (ignored: Exception) {
        }
      }

      return fgColorInstance
    }

  val relatedPaths: MutableList<String> = ArrayList()

  override val isValid: Boolean
    get() = valid

  /*runtime data*/
  @Volatile
  @Transient
  private var links: List<Link>? = null

  @Volatile
  @Transient
  private var valid = true

  @Volatile
  @Transient
  private var bgColorInstance: Color? = null

  @Volatile
  @Transient
  private var fgColorInstance: Color? = null

  constructor()

  constructor(id: String, title: String, valid: Boolean) {
    this.id = id
    this.title = title
    this.valid = valid
  }

  override fun icon(): Icon = AllIcons.Actions.GroupByModule

  override fun invalidate() {
    this.valid = false
  }

  override fun size(project: Project): Int = getLinks(project).size

  override fun getLinks(project: Project): List<Link> {
    if (links == null) IndexCache.getInstance(project).initGroup(this)

    return links!!
  }

  override fun isOwner(ownerPath: String): Boolean = this.ownerPath == ownerPath

  fun addRelatedPath(value: String) {
    relatedPaths.add(value)
  }

  override fun needSmartMode(): Boolean = true

  fun setLinks(links: List<Link>?): EditorGroupIndexValue {
    this.links = links
    return this
  }

  /** FOR INDEX STORE. */
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false

    val that = other as EditorGroupIndexValue

    if (id != that.id) return false
    if (ownerPath != that.ownerPath) return false
    if (root != that.root) return false
    if (title != that.title) return false
    if (backgroundColor != that.backgroundColor) return false
    if (foregroundColor != that.foregroundColor) return false
    return relatedPaths == that.relatedPaths
  }

  /** FOR INDEX STORE. */
  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + ownerPath.hashCode()
    result = 31 * result + if (root != null) root.hashCode() else 0
    result = 31 * result + title.hashCode()
    result = 31 * result + backgroundColor.hashCode()
    result = 31 * result + foregroundColor.hashCode()
    result = 31 * result + relatedPaths.hashCode()
    return result
  }

  override fun toString(): String =
    "EditorGroupIndexValue{id='$id', ownerFile='$ownerPath', root='$root', title='$title', backgroundColor='$backgroundColor', foregroundColor='$foregroundColor', relatedPaths=$relatedPaths, valid=$valid}"
}
