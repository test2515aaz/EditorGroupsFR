package krasa.editorGroups

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import krasa.editorGroups.model.EditorGroup
import krasa.editorGroups.model.RegexGroup
import krasa.editorGroups.model.RegexGroupModel
import krasa.editorGroups.settings.EditorGroupsSettings
import krasa.editorGroups.support.RegexFileResolver

@Service(Service.Level.PROJECT)
class RegexGroupProvider {
  fun findFirstMatchingRegexGroup(file: VirtualFile): EditorGroup {
    thisLogger().debug("<findFirstMatchingRegexGroup: $file")

    val start = System.currentTimeMillis()
    val fileName = file.name
    val matching = EditorGroupsSettings.instance.regexGroupModels.findFirstMatching(fileName)

    val result = when (matching) {
      null -> EditorGroup.EMPTY
      else -> RegexGroup(matching, file.parent, fileName)
    }

    thisLogger().debug("<findFirstMatchingRegexGroup: result=$result in ${System.currentTimeMillis() - start}ms")

    return result
  }

  fun findMatchingRegexGroups(file: VirtualFile): List<RegexGroup> {
    thisLogger().debug("findMatchingRegexGroups: $file")

    val start = System.currentTimeMillis()
    val fileName = file.name
    val matching: List<RegexGroupModel> = EditorGroupsSettings.instance.regexGroupModels.findMatching(fileName)

    thisLogger().debug("findMatchingRegexGroups: ${System.currentTimeMillis() - start}ms")

    return toRegexGroup(file, fileName, matching)
  }

  fun findProjectRegexGroups(): List<RegexGroup> {
    val globalRegexGroups = EditorGroupsSettings.instance.regexGroupModels.findProjectRegexGroups()
    return toRegexGroups(globalRegexGroups)
  }

  fun toRegexGroup(file: VirtualFile, fileName: String?, matching: List<RegexGroupModel>): List<RegexGroup> =
    matching.map { RegexGroup(it, file.parent, emptyList(), fileName) }

  private fun toRegexGroups(globalRegexGroups: List<RegexGroupModel>): List<RegexGroup> =
    globalRegexGroups.map { RegexGroup(it, null, emptyList(), null) }

  fun getRegexGroup(group: RegexGroup, project: Project?, currentFile: VirtualFile?): RegexGroup {
    val links = RegexFileResolver(project!!).resolveRegexGroupLinks(group, currentFile)

    if (currentFile != null && links.isEmpty()) {
      thisLogger().error("should contain the current file at least: $group")
    }

    return RegexGroup(group.regexGroupModel, group.folder, links, group.fileName)
  }

  fun findRegexGroup(file: VirtualFile, substring: String?): EditorGroup {
    val regexGroupModels = EditorGroupsSettings.instance.regexGroupModels
    val regexGroupModel = regexGroupModels.find(substring!!) ?: return EditorGroup.EMPTY

    return RegexGroup(regexGroupModel, file.parent, emptyList(), file.name)
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): RegexGroupProvider = project.getService(RegexGroupProvider::class.java)
  }
}
