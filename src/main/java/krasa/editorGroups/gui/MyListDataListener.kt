package krasa.editorGroups.gui

import krasa.editorGroups.model.RegexGroupModel
import krasa.editorGroups.model.RegexGroupModels
import javax.swing.DefaultListModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class MyListDataListener(private val model: DefaultListModel<RegexGroupModel>, private val models: RegexGroupModels) : ListDataListener {
  override fun intervalAdded(e: ListDataEvent) = listChanged()

  override fun intervalRemoved(e: ListDataEvent) = listChanged()

  override fun contentsChanged(e: ListDataEvent) = listChanged()

  private fun listChanged() {
    models.regexGroupModels.clear()
    models.regexGroupModels.addAll(model.elements().toList())
  }
}
