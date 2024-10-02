package krasa.editorGroups.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import krasa.editorGroups.EditorGroupsSettingsState;
import krasa.editorGroups.PanelRefresher;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TogglePanelVisibilityAction extends DumbAwareAction {
  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    EditorGroupsSettingsState state = EditorGroupsSettingsState.state();
    state.setShowPanel(state.isShowPanel());
    PanelRefresher.getInstance(Objects.requireNonNull(getEventProject(e))).refresh();
  }


  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    EditorGroupsSettingsState state = EditorGroupsSettingsState.state();
    if (state.isShowPanel()) {
      e.getPresentation().setText("Hide Panel");
    } else {
      e.getPresentation().setText("Show Panel");
    }
  }
}
