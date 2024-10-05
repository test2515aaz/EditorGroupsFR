package krasa.editorGroups

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.model.EditorGroup
import krasa.editorGroups.model.RegexGroup
import krasa.editorGroups.model.RegexGroupModel
import krasa.editorGroups.support.RegexFileResolver

class RegexGroupProvider {
  fun findFirstMatchingRegexGroup(file: VirtualFile): EditorGroup {
    if (LOG.isDebugEnabled) LOG.debug("<findFirstMatchingRegexGroup: $file")

    val start = System.currentTimeMillis()
    val fileName = file.name
    val matching = state().regexGroupModels.findFirstMatching(fileName)

    val result = when (matching) {
      null -> EditorGroup.EMPTY
      else -> RegexGroup(matching, file.parent, fileName)
    }

    if (LOG.isDebugEnabled) LOG.debug("<findFirstMatchingRegexGroup: result=$result in ${System.currentTimeMillis() - start}ms")

    return result
  }

  fun findMatchingRegexGroups(file: VirtualFile): List<RegexGroup> {
    if (LOG.isDebugEnabled) LOG.debug("findMatchingRegexGroups: $file")

    val start = System.currentTimeMillis()
    val fileName = file.name
    val matching: List<RegexGroupModel?> = state().regexGroupModels.findMatching(fileName)

    if (LOG.isDebugEnabled) LOG.debug("findMatchingRegexGroups: ${System.currentTimeMillis() - start}ms")

    return toRegexGroup(file, fileName, matching)
  }

  fun findProjectRegexGroups(): List<RegexGroup> {
    val globalRegexGroups = state().regexGroupModels.findProjectRegexGroups()
    return toRegexGroups_stub(globalRegexGroups)
  }

  fun toRegexGroup(file: VirtualFile, fileName: String?, matching: List<RegexGroupModel?>): List<RegexGroup> =
    matching.map { RegexGroup(it!!, file.parent, emptyList(), fileName) }

  private fun toRegexGroups_stub(globalRegexGroups: List<RegexGroupModel>): List<RegexGroup> =
    globalRegexGroups.map { RegexGroup(it, null, emptyList(), null) }

  fun getRegexGroup(group: RegexGroup, project: Project?, currentFile: VirtualFile?): RegexGroup {
    val links = RegexFileResolver(project!!).resolveRegexGroupLinks(group, currentFile)
    if (currentFile != null && links.isEmpty()) {
      LOG.error("should contain the current file at least: $group")
    }
    return RegexGroup(group.regexGroupModel, group.folder, links, group.fileName)
  }

  fun findRegexGroup(file: VirtualFile, substring: String?): EditorGroup {
    val regexGroupModels = state().regexGroupModels
    val regexGroupModel = regexGroupModels.find(substring!!) ?: return EditorGroup.EMPTY

    return RegexGroup(regexGroupModel, file.parent, emptyList(), file.name)
  }

  companion object {
    private val LOG = Logger.getInstance(RegexGroupProvider::class.java)

    @JvmStatic
    fun getInstance(project: Project): RegexGroupProvider = project.getService(RegexGroupProvider::class.java)
  }
}
