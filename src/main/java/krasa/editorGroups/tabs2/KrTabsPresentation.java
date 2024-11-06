package krasa.editorGroups.tabs2;

import com.intellij.openapi.util.NlsContexts;
import krasa.editorGroups.tabs2.label.TabUiDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public interface KrTabsPresentation {
  boolean isHideTabs();

  void setHideTabs(boolean hideTabs);

  KrTabsPresentation setPaintFocus(boolean paintFocus);

  KrTabsPresentation setSideComponentVertical(boolean vertical);

  KrTabsPresentation setSideComponentOnTabs(boolean onTabs);

  KrTabsPresentation setSideComponentBefore(boolean before);

  KrTabsPresentation setSingleRow(boolean singleRow);

  boolean isSingleRow();

  KrTabsPresentation setUiDecorator(@Nullable TabUiDecorator decorator);

  KrTabsPresentation setRequestFocusOnLastFocusedComponent(boolean request);

  void setPaintBlocked(boolean blocked, final boolean takeSnapshot);

  KrTabsPresentation setInnerInsets(Insets innerInsets);

  KrTabsPresentation setFocusCycle(final boolean root);

  @NotNull
  KrTabsPresentation setToDrawBorderIfTabsHidden(boolean draw);

  @NotNull
  EditorGroupsTabsBase getJBTabs();

  @NotNull
  KrTabsPresentation setActiveTabFillIn(@Nullable Color color);

  @NotNull
  KrTabsPresentation setTabLabelActionsAutoHide(boolean autoHide);

  @NotNull
  KrTabsPresentation setTabsPosition(EditorGroupsTabsPosition position);

  EditorGroupsTabsPosition getTabsPosition();

  void setFirstTabOffset(int offset);

  KrTabsPresentation setEmptyText(@Nullable @NlsContexts.StatusText String text);
}
