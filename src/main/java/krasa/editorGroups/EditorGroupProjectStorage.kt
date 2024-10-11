package krasa.editorGroups

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import krasa.editorGroups.EditorGroupsSettingsState.Companion.state

@Service(Service.Level.PROJECT)
@State(name = "EditorGroups", storages = [Storage(value = "EditorGroups.xml")])
class EditorGroupProjectStorage(private val project: Project) : PersistentStateComponent<EditorGroupProjectStorage.State> {

  class State {
    @XCollection(propertyElementName = "lastGroup", elementTypes = [StringPair::class])
    var lastGroup: MutableList<StringPair> = emptyList<StringPair>().toMutableList()

    override fun equals(o: Any?): Boolean {
      if (this === o) return true
      if (o == null || javaClass != o.javaClass) return false

      val state = o as State
      return lastGroup == state.lastGroup
    }

    override fun hashCode(): Int = lastGroup.hashCode()
  }

  override fun getState(): State {
    when {
      state().isRememberLastGroup -> {
        val start = System.currentTimeMillis()
        val state = IndexCache.getInstance(project).state

        thisLogger().debug("EditorGroupProjectSettings.getState size:${state.lastGroup.size} ${System.currentTimeMillis() - start}ms")
        return state
      }

      else                        -> return State()
    }
  }

  override fun loadState(state: State) {
    if (state().isRememberLastGroup) {
      val start = System.currentTimeMillis()
      IndexCache.getInstance(project).loadState(state)

      thisLogger().debug("EditorGroupProjectSettings.loadState size:${state.lastGroup.size} ${System.currentTimeMillis() - start}ms")
    }
  }

  @Tag("pair")
  class StringPair {
    @Attribute("key")
    var key: String? = null

    @Attribute("value")
    var value: String? = null

    constructor()

    constructor(key: String?, value: String?) {
      this.key = key
      this.value = value
    }
  }
}
