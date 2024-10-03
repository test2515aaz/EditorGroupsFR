package krasa.editorGroups.support

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import krasa.editorGroups.EditorGroupsSettingsState
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.model.Link
import krasa.editorGroups.model.Link.Companion.fromVirtualFiles
import krasa.editorGroups.model.RegexGroup
import krasa.editorGroups.model.RegexGroupModel
import java.util.regex.Matcher

class RegexFileResolver(private val project: Project) {
  protected var links: MutableSet<VirtualFile?> = HashSet()
  protected var config: EditorGroupsSettingsState = state()

  fun resolveRegexGroupLinks(regexGroup: RegexGroup, currentFile: VirtualFile?): List<Link> {
    if (LOG.isDebugEnabled) LOG.debug(">resolveRegexGroupLinks")

    val start = System.currentTimeMillis()
    val regexGroupModel = regexGroup.regexGroupModel
    val referenceMatcher = regexGroup.referenceMatcher

    if (currentFile != null) {
      // always include it in case there are too many matches
      links.add(currentFile)
    }

    val groupMatcher = regexGroupModel.regexPattern!!.matcher("")
    val projectFileIndex = ProjectFileIndex.getInstance(project)
    val folders = regexGroup.getScopes(project)

    try {
      folders
        .asSequence()
        .filterNotNull()
        .forEach { processFolders2(regexGroup, regexGroupModel, referenceMatcher, groupMatcher, projectFileIndex, it) }

    } catch (e: TooManyFilesException) {
      e.showNotification()
      LOG.warn("Found too many matching files, skipping. Size=${links.size} $regexGroup")

      if (LOG.isDebugEnabled) LOG.debug(links.toString())
    }

    val duration = System.currentTimeMillis() - start
    if (duration > 500) {
      LOG.warn("<resolveRegexGroup ${duration}ms $regexGroup; links=$links")
    } else if (LOG.isDebugEnabled) {
      LOG.debug("<resolveRegexGroup ${duration}ms links=$links")
    }

    return fromVirtualFiles(links, project)
  }

  private fun processFolders2(
    regexGroup: RegexGroup,
    regexGroupModel: RegexGroupModel,
    referenceMatcher: Matcher?,
    groupMatcher: Matcher,
    projectFileIndex: ProjectFileIndex,
    folder: VirtualFile
  ) {
    VfsUtilCore.visitChildrenRecursively(folder, object : VirtualFileVisitor<Any?>() {
      override fun visitFileEx(child: VirtualFile): Result {
        when {
          child.isDirectory -> {
            ProgressManager.checkCanceled()

            if (regexGroupModel.scope == RegexGroupModel.Scope.CURRENT_FOLDER) {
              if (child != regexGroup.folder) return SKIP_CHILDREN
            } else {
              if (projectFileIndex.isExcluded(child)) return SKIP_CHILDREN
            }
          }

          else              -> {
            val matcher = groupMatcher.reset(child.name)
            if (matches(regexGroupModel, referenceMatcher, matcher)) {
              links.add(child)

              if (links.size > config.groupSizeLimitInt) throw TooManyFilesException()
            }
          }
        }
        return CONTINUE
      }
    })
  }

  private fun matches(regexGroupModel: RegexGroupModel, referenceMatcher: Matcher?, matcher: Matcher): Boolean {
    if (!matcher.matches()) return false

    if (referenceMatcher == null) return true

    for (j in 1..matcher.groupCount()) {
      if (regexGroupModel.isComparingGroup(j)) {
        val refGroup = referenceMatcher.group(j)
        val group = matcher.group(j)
        if (refGroup != group) return false
      }
    }
    return true
  }

  companion object {
    private val LOG = Logger.getInstance(RegexFileResolver::class.java)
  }
}
