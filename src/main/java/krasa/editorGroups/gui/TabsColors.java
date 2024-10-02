package krasa.editorGroups.gui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBDimension;
import krasa.editorGroups.EditorGroupsSettingsState;
import krasa.editorGroups.support.CheckBoxWithColorChooser;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class TabsColors {
  private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(TabsColors.class);

  private JPanel root;

  private JCheckBox enabledCheckBox;

  private CheckBoxWithColorChooser mask;
  private JTextField opacity;

  private CheckBoxWithColorChooser darcula_mask;
  private JTextField darcula_opacity;
  private JButton darcula_opacityDefault;
  private JButton opacityDefault;
  private CheckBoxWithColorChooser tabBgColor;
  private CheckBoxWithColorChooser tabFgColor;
  private JPanel classic;
  private JPanel darcula;
  private JPanel ideTabs;

  public JPanel getRoot() {
    return root;
  }

  public TabsColors() {
    darcula.setVisible(!JBColor.isBright());
    classic.setVisible(JBColor.isBright());

    opacityDefault.addActionListener(e -> opacity.setText(String.valueOf(EditorGroupsSettingsState.Tabs.DEFAULT_OPACITY)));
    darcula_opacityDefault.addActionListener(e -> opacity.setText(String.valueOf(EditorGroupsSettingsState.Tabs.DEFAULT_DARK_OPACITY)));
    ideTabs.setVisible(false);
  }

  private void createUIComponents() {
    tabFgColor = new CheckBoxWithColorChooser("Default selected tab foreground color ", null);
    tabBgColor = new CheckBoxWithColorChooser("Default selected tab background color ", null);

    Dimension colorDimension = new JBDimension(30, 30);
    mask = new CheckBoxWithColorChooser(null, null, EditorGroupsSettingsState.Tabs.DEFAULT_MASK).setColorDimension(colorDimension);
    CheckBoxWithColorChooser defaultTabColor = new CheckBoxWithColorChooser(null, null, EditorGroupsSettingsState.Tabs.DEFAULT_TAB_COLOR).setColorDimension(colorDimension);

    darcula_mask = new CheckBoxWithColorChooser(null, null, EditorGroupsSettingsState.Tabs.DEFAULT_DARK_MASK).setColorDimension(colorDimension);
    CheckBoxWithColorChooser darcula_defaultTabColor = new CheckBoxWithColorChooser(null, null, EditorGroupsSettingsState.Tabs.DEFAULT_DARK_TAB_COLOR).setColorDimension(colorDimension);
  }

  public void setData(EditorGroupsSettingsState editorGroupsSettingsState, EditorGroupsSettingsState.Tabs data) {
    tabBgColor.setColor(editorGroupsSettingsState.getTabBgColorAsColor());
    tabBgColor.setSelected(editorGroupsSettingsState.isTabBgColorEnabled());

    tabFgColor.setColor(editorGroupsSettingsState.getTabFgColorAsColor());
    tabFgColor.setSelected(editorGroupsSettingsState.isTabFgColorEnabled());


    enabledCheckBox.setSelected(data.isPatchPainter());

    mask.setColor(data.mask);
    opacity.setText(String.valueOf(data.opacity));

    darcula_mask.setColor(data.darkMask);
    darcula_opacity.setText(String.valueOf(data.darkOpacity));
  }

  public void getData(EditorGroupsSettingsState editorGroupsSettingsState, EditorGroupsSettingsState.Tabs data) {
    editorGroupsSettingsState.setTabBgColorAsColor(tabBgColor.getColor());
    editorGroupsSettingsState.setTabBgColorEnabled(tabBgColor.isSelected());

    editorGroupsSettingsState.setTabFgColorAsColor(tabFgColor.getColor());
    editorGroupsSettingsState.setTabFgColorEnabled(tabFgColor.isSelected());


    data.setPatchPainter(enabledCheckBox.isSelected());

    data.mask = mask.getColorAsRGB();
    data.setOpacity(opacity.getText());

    data.darkMask = darcula_mask.getColorAsRGB();
    data.setDarkOpacity(darcula_opacity.getText());

    setData(editorGroupsSettingsState, data);
  }


  public boolean isModified(EditorGroupsSettingsState editorGroupsSettingsState, EditorGroupsSettingsState.Tabs data) {
    if (tabBgColor.isSelected() != editorGroupsSettingsState.isTabBgColorEnabled()) return true;
    if (!Objects.equals(tabBgColor.getColor(), editorGroupsSettingsState.getTabBgColorAsColor())) return true;

    if (tabFgColor.isSelected() != editorGroupsSettingsState.isTabFgColorEnabled()) return true;
    if (!Objects.equals(tabFgColor.getColor(), editorGroupsSettingsState.getTabFgColorAsColor())) return true;


    if (enabledCheckBox.isSelected() != data.isPatchPainter()) return true;


    if (!Objects.equals(mask.getColorAsRGB(), data.mask)) return true;
    if (!Objects.equals(opacity.getText(), String.valueOf(data.opacity))) return true;

    if (!Objects.equals(darcula_mask.getColorAsRGB(), data.darkMask)) return true;
    return !Objects.equals(darcula_opacity.getText(), String.valueOf(data.darkOpacity));
  }
}
