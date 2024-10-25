package krasa.editorGroups

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "EditorGroups", storages = [Storage(value = "EditorGroups.xml")])
class EditorGroupsSettings : PersistentStateComponent<EditorGroupsSettingsState> {
  private var editorGroupsSettingsState = EditorGroupsSettingsState()

  override fun getState(): EditorGroupsSettingsState = editorGroupsSettingsState

  override fun loadState(editorGroupsSettingsState: EditorGroupsSettingsState) {
    this.editorGroupsSettingsState = editorGroupsSettingsState
  }

  companion object {
    @JvmStatic
    val instance: EditorGroupsSettings
      get() = ApplicationManager.getApplication().getService(EditorGroupsSettings::class.java)
  }
}
