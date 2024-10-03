package krasa.editorGroups.index

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.Pair
import com.intellij.patterns.StringPattern
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state
import krasa.editorGroups.IndexCache.Companion.getInstance
import krasa.editorGroups.PanelRefresher
import krasa.editorGroups.language.EditorGroupsLanguage
import krasa.editorGroups.model.EditorGroupIndexValue
import krasa.editorGroups.support.Notifications.indexingWarn
import org.apache.commons.lang3.StringUtils
import java.io.File

class EditorGroupIndexer : DataIndexer<String, EditorGroupIndexValue, FileContent> {
  private val MAIN_PATTERN = MyIndexPattern("@(idea|group)\\.\\w+.*", false)

  val indexPatterns: Array<Pair<MyIndexPattern, Consumer>> = arrayOf<Pair<MyIndexPattern, Consumer>>(
    Pair(MyIndexPattern("^@(idea|group)\\.root\\s(.*)", false), RootConsumer()),
    Pair(MyIndexPattern("^@(idea|group)\\.title\\s(.*)", false), TitleConsumer()),
    Pair(MyIndexPattern("^@(idea|group)\\.color\\s(.*)", false), ColorConsumer()),
    Pair(MyIndexPattern("^@(idea|group)\\.fgcolor\\s(.*)", false), FgColorConsumer()),
    Pair(MyIndexPattern("^@(idea|group)\\.related\\s(.*)", false), RelatedFilesConsumer()),
    Pair(MyIndexPattern("^@(idea|group)\\.id\\s(.*)", false), IdConsumer()),
    Pair(MyIndexPattern("(^@(idea|group)\\.disable.*)", false), DisableConsumer())
  )

  override fun map(inputData: FileContent): Map<String, EditorGroupIndexValue> {
    val file = inputData.file
    val isEGroup = EditorGroupsLanguage.isEditorGroupsLanguage(file)
    if (state().isIndexOnlyEditorGroupsFiles && !isEGroup) return emptyMap()

    val ownerPath = file.path
    try {
      val folder: File
      try {
        folder = File(inputData.file.parent.path)
      } catch (e: Exception) {
        return emptyMap()
      }

      var currentGroup: EditorGroupIndexValue? = null
      var lastGroup: EditorGroupIndexValue? = null
      var index = 0
      val map = HashMap<String, EditorGroupIndexValue>()

      val chars = inputData.contentAsText.toString() // matching strings is faster than HeapCharBuffer
      val input = StringPattern.newBombedCharSequence(chars)
      val optimizedIndexingPattern = MAIN_PATTERN.optimizedIndexingPattern
      val matcher = optimizedIndexingPattern!!.matcher(input)

      while (matcher.find()) {
        if (matcher.start() != matcher.end()) {
          val trim = matcher.group(0).trim { it <= ' ' }
          currentGroup = processPatterns(inputData, folder, currentGroup, trim)

          if (lastGroup != null && lastGroup !== currentGroup) {
            index = add(inputData, ownerPath, lastGroup, index, map)
          }

          lastGroup = currentGroup
        }
      }

      if (currentGroup != null) {
        add(inputData, ownerPath, currentGroup, index, map)
      }
      return map
    } catch (e: DisableException) {
      getInstance(inputData.project).removeGroup(ownerPath)
      return emptyMap()
    } catch (e: ProcessCanceledException) {
      throw e
    } catch (e: Exception) {
      LOG.error(e)
      return emptyMap()
    }
  }

  fun add(
    inputData: FileContent,
    ownerPath: String,
    lastGroup: EditorGroupIndexValue,
    index: Int,
    map: HashMap<String, EditorGroupIndexValue>
  ): Int {
    var myLastGroup = lastGroup
    var myIndex = index

    myLastGroup.ownerPath = ownerPath
    if (StringUtils.isEmpty(myLastGroup.id)) {
      myLastGroup.id = ownerPath + ";" + myIndex++
    }

    if (StringUtils.isEmpty(myLastGroup.root)) {
      myLastGroup.root = ownerPath
    }

    try {
      myLastGroup = PanelRefresher.getInstance(inputData.project).onIndexingDone(ownerPath, myLastGroup)
    } catch (e: ProcessCanceledException) {
      LOG.debug(e)
    } catch (e: Exception) {
      LOG.error(e)
    }

    if (map.containsKey(myLastGroup.id)) {
      indexingWarn(inputData.project, inputData.file, "Duplicate Group ID '${myLastGroup.id}'")
    } else {
      map[myLastGroup.id] = myLastGroup
    }
    return myIndex
  }

  fun processPatterns(inputData: FileContent, folder: File?, value: EditorGroupIndexValue?, trim: CharSequence): EditorGroupIndexValue? {
    var result = value

    for (indexPattern in indexPatterns) {
      val pattern = indexPattern.first.optimizedIndexingPattern
      val consumer = indexPattern.second

      if (pattern != null) {
        val subMatcher = pattern.matcher(trim)

        while (subMatcher.find()) {
          if (subMatcher.start() != subMatcher.end()) {
            result = consumer.consume(inputData, result, folder, subMatcher.group(INDEX_PATTERN_GROUP).trim { it <= ' ' })
          }
        }
      }
    }
    return result
  }

  abstract class Consumer {
    fun init(value: EditorGroupIndexValue?): EditorGroupIndexValue {
      when (value) {
        null -> return EditorGroupIndexValue()
        else -> return value
      }
    }

    abstract fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue?
  }

  internal class TitleConsumer : Consumer() {
    override fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue? {
      val result = init(groupIndexValue)
      result.title = value
      return result
    }
  }

  internal class RootConsumer : Consumer() {
    override fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue? {
      val result = init(groupIndexValue)
      result.root = value
      return result
    }
  }

  internal class ColorConsumer : Consumer() {
    override fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue? {
      val result = init(groupIndexValue)
      result.backgroundColor = value
      return result
    }
  }

  internal class FgColorConsumer : Consumer() {
    override fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue? {
      val result = init(groupIndexValue)
      result.foregroundColor = value
      return result
    }
  }

  internal class IdConsumer : Consumer() {
    override fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue? {
      var result = init(groupIndexValue)
      if (StringUtils.isNotEmpty(result.id)) result = EditorGroupIndexValue()
      if (StringUtils.isEmpty(result.title)) result.title = value

      result.id = value
      return result
    }
  }

  internal class RelatedFilesConsumer : Consumer() {
    override fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue? {
      var result = groupIndexValue
      if (StringUtils.isBlank(value)) return result

      result = init(result)
      result.addRelatedPath(value)
      return result
    }
  }

  private inner class DisableConsumer : Consumer() {
    override fun consume(
      inputData: FileContent?,
      groupIndexValue: EditorGroupIndexValue?,
      folder: File?,
      value: String
    ): EditorGroupIndexValue? = throw DisableException()
  }

  internal class DisableException : RuntimeException()

  companion object {
    private val LOG = Logger.getInstance(EditorGroupIndexer::class.java)
    const val INDEX_PATTERN_GROUP: Int = 2
  }
}
