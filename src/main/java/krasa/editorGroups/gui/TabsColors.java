package krasa.editorGroups.gui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBDimension;
import krasa.editorGroups.EditorGroupsSettings;
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

    opacityDefault.addActionListener(e -> opacity.setText(String.valueOf(EditorGroupsSettings.DEFAULT_OPACITY)));
    darcula_opacityDefault.addActionListener(e -> opacity.setText(String.valueOf(EditorGroupsSettings.DEFAULT_DARK_OPACITY)));
    ideTabs.setVisible(false);
  }

  private void createUIComponents() {
    tabFgColor = new CheckBoxWithColorChooser("Default selected tab foreground color ", null);
    tabBgColor = new CheckBoxWithColorChooser("Default selected tab background color ", null);

    Dimension colorDimension = new JBDimension(30, 30);
    mask = new CheckBoxWithColorChooser(null, null, EditorGroupsSettings.getDEFAULT_MASK()).setColorDimension(colorDimension);
    CheckBoxWithColorChooser defaultTabColor = new CheckBoxWithColorChooser(null, null, EditorGroupsSettings.getDEFAULT_TAB_COLOR()).setColorDimension(colorDimension);

    darcula_mask = new CheckBoxWithColorChooser(null, null, EditorGroupsSettings.getDEFAULT_DARK_MASK()).setColorDimension(colorDimension);
    CheckBoxWithColorChooser darcula_defaultTabColor = new CheckBoxWithColorChooser(null, null, EditorGroupsSettings.getDEFAULT_DARK_TAB_COLOR()).setColorDimension(colorDimension);
  }

  public void setData(EditorGroupsSettings editorGroupsSettingsState, EditorGroupsSettings.EditorGroupsTabsState data) {
    tabBgColor.setColor(editorGroupsSettingsState.getTabBgColor());
    tabBgColor.setSelected(editorGroupsSettingsState.isTabBgColorEnabled());

    tabFgColor.setColor(editorGroupsSettingsState.getTabFgColor());
    tabFgColor.setSelected(editorGroupsSettingsState.isTabFgColorEnabled());


    enabledCheckBox.setSelected(data.isPatchPainter());

    mask.setColor(data.getMask());
    opacity.setText(String.valueOf(data.getOpacity()));

    darcula_mask.setColor(data.getDarkMask());
    darcula_opacity.setText(String.valueOf(data.getDarkOpacity()));
  }

  public void getData(EditorGroupsSettings editorGroupsSettingsState, EditorGroupsSettings.EditorGroupsTabsState data) {
    editorGroupsSettingsState.setTabBgColor(tabBgColor.getColor());
    editorGroupsSettingsState.setTabBgColorEnabled(tabBgColor.isSelected());

    editorGroupsSettingsState.setTabFgColor(tabFgColor.getColor());
    editorGroupsSettingsState.setTabFgColorEnabled(tabFgColor.isSelected());


    data.setPatchPainter(enabledCheckBox.isSelected());

    data.setMask(mask.getColorAsRGB());
    data.setOpacity(opacity.getText());

    data.setDarkMask(darcula_mask.getColorAsRGB());
    data.setDarkOpacity(darcula_opacity.getText());

    setData(editorGroupsSettingsState, data);
  }


  public boolean isModified(EditorGroupsSettings editorGroupsSettingsState, EditorGroupsSettings.EditorGroupsTabsState data) {
    if (tabBgColor.isSelected() != editorGroupsSettingsState.isTabBgColorEnabled()) return true;
    if (!Objects.equals(tabBgColor.getColor(), editorGroupsSettingsState.getTabBgColor())) return true;

    if (tabFgColor.isSelected() != editorGroupsSettingsState.isTabFgColorEnabled()) return true;
    if (!Objects.equals(tabFgColor.getColor(), editorGroupsSettingsState.getTabFgColor())) return true;


    if (enabledCheckBox.isSelected() != data.isPatchPainter()) return true;


    if (!Objects.equals(mask.getColorAsRGB(), data.getMask())) return true;
    if (!Objects.equals(opacity.getText(), String.valueOf(data.getOpacity()))) return true;

    if (!Objects.equals(darcula_mask.getColorAsRGB(), data.getDarkMask())) return true;
    return !Objects.equals(darcula_opacity.getText(), String.valueOf(data.getDarkOpacity()));
  }
}
