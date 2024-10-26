package krasa.editorGroups.support

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import krasa.editorGroups.EditorGroupsSettings
import krasa.editorGroups.model.Link
import krasa.editorGroups.model.RegexGroup
import krasa.editorGroups.model.RegexGroupModel
import java.util.regex.Matcher

open class RegexFileResolver(private val project: Project) {
  protected var links: MutableSet<VirtualFile?> = HashSet()

  fun resolveRegexGroupLinks(regexGroup: RegexGroup, currentFile: VirtualFile?): List<Link> {
    thisLogger().debug("<resolveRegexGroupLinks")

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
        .forEach { processFolders(regexGroup, regexGroupModel, referenceMatcher, groupMatcher, projectFileIndex, it) }

    } catch (e: TooManyFilesException) {
      e.showNotification()
      thisLogger().warn("Found too many matching files, skipping. Size=${links.size} $regexGroup")
      thisLogger().debug(links.toString())
    }

    val duration = System.currentTimeMillis() - start
    if (duration > DURATION) thisLogger().warn("<resolveRegexGroup ${duration}ms $regexGroup; links=$links")

    thisLogger().debug("<resolveRegexGroup ${duration}ms links=$links")

    return Link.fromVirtualFiles(links, project)
  }

  private fun processFolders(
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

            if (shouldSkipDirectory(
                child = child,
                regexGroupModel = regexGroupModel,
                regexGroup = regexGroup,
                projectFileIndex = projectFileIndex
              )
            ) {
              return SKIP_CHILDREN
            }
          }

          else              -> {
            val matcher = groupMatcher.reset(child.name)
            if (matches(regexGroupModel, referenceMatcher, matcher)) {
              links.add(child)

              if (links.size > EditorGroupsSettings.instance.groupSizeLimit) throw TooManyFilesException()
            }
          }
        }
        return CONTINUE
      }
    })
  }

  private fun shouldSkipDirectory(
    child: VirtualFile,
    regexGroupModel: RegexGroupModel,
    regexGroup: RegexGroup,
    projectFileIndex: ProjectFileIndex
  ): Boolean = when (regexGroupModel.myScope) {
    RegexGroupModel.Scope.CURRENT_FOLDER -> child != regexGroup.folder
    else                                 -> projectFileIndex.isExcluded(child)
  }

  private fun matches(regexGroupModel: RegexGroupModel, referenceMatcher: Matcher?, matcher: Matcher): Boolean {
    if (!matcher.matches()) return false

    if (referenceMatcher == null) return true

    return (1..matcher.groupCount()).all { index ->
      when {
        regexGroupModel.isComparingGroup(index) -> referenceMatcher.group(index) == matcher.group(index)
        else                                    -> true
      }
    }
  }

  companion object {
    const val DURATION = 500
  }
}
