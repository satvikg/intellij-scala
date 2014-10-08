package org.jetbrains.plugins.scala.config.scalaProjectTemplate.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.plugins.scala.config.Libraries;
import org.jetbrains.plugins.scala.config.LibraryId;
import org.jetbrains.plugins.scala.config.LibraryLevel;
import org.jetbrains.plugins.scala.config.scalaProjectTemplate.scalaDownloader.ScalaDownloader;
import org.jetbrains.plugins.scala.config.ui.ScalaSupportUiBase;
import scala.Tuple2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: Dmitry Naydanov
 * Date: 11/10/12
 */
public class ScalaModuleSettingsUi {
    private JRadioButton existentLibraryRadioButton;
    private JRadioButton setScalaHomeRadioButton;
    private JRadioButton configLaterRadioButton;
    private JComboBox existentLibraryComboBox;
    private JTextField compilerNameField;
    private JTextField libraryNameField;
    private TextFieldWithBrowseButton scalaHome;
    private JLabel scalaHomeComment;
    private JPanel mainPanel;
    private JComboBox existentCompilerComboBox;
    private JButton downloadScalaButton;
    private JCheckBox makeGlobalLibrariesCheckBox;
    private ScalaSupportUiBase uiUtil;
    private Project myProject;

    private ButtonGroup configTypeGroup;

    public ScalaModuleSettingsUi(Project project) {
        myProject = project;
        uiUtil = new ScalaSupportUiBase(project, scalaHome, scalaHomeComment, compilerNameField, libraryNameField,
                makeGlobalLibrariesCheckBox, new JRadioButton[]{existentLibraryRadioButton, setScalaHomeRadioButton, configLaterRadioButton}) {
            @Override
            protected void updateSectionsState() {
                setEnabled(setScalaHomeRadioButton.isSelected(), libraryNameField, makeGlobalLibrariesCheckBox, compilerNameField,
                        scalaHome, scalaHomeComment, downloadScalaButton);
                setEnabled(existentLibraryRadioButton.isSelected(), existentLibraryComboBox, existentCompilerComboBox);
            }
        };

        configTypeGroup = new ButtonGroup();
        configTypeGroup.add(existentLibraryRadioButton);
        configTypeGroup.add(setScalaHomeRadioButton);
        configTypeGroup.add(configLaterRadioButton);

        Tuple2<Library[], Library[]> filteredLibraries = Libraries.filterScalaLikeLibraries(getExistentLibraries());

        existentCompilerComboBox.setModel(new DefaultComboBoxModel(filteredLibraries._1()));
        existentLibraryComboBox.setModel(new DefaultComboBoxModel(filteredLibraries._2()));
        existentCompilerComboBox.setRenderer(new ExistentLibraryRenderer("compiler"));
        existentLibraryComboBox.setRenderer(new ExistentLibraryRenderer("library"));

        if (filteredLibraries._1().length > 0 && filteredLibraries._2().length > 0)
            existentLibraryRadioButton.setSelected(true);
        else setScalaHomeRadioButton.setSelected(true);

        downloadScalaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = ScalaDownloader.download(myProject, getMainPanel());
                if (path != null) {
                    scalaHome.setText(path);
                }
            }
        });

        uiUtil.initControls();
        setHomeDefault();
    }

    private void setHomeDefault() {
        if (scalaHome.getText() != null && scalaHome.getText().trim().length() > 0) return;
        uiUtil.guessHome();
    }

    private Library[] getExistentLibraries() {
        return LibraryTablesRegistrar.getInstance().getLibraryTable().getLibraries();
    }

    private LibraryId extractLibraryId(JComboBox existentComboBox, JTextField nameField) {
        if (existentLibraryRadioButton.isSelected()) {
            return new LibraryId(((Library) existentComboBox.getSelectedItem()).getName(), LibraryLevel.Global);
        } else if (setScalaHomeRadioButton.isSelected()) {
            return new LibraryId(nameField.getText().trim(), makeGlobalLibrariesCheckBox.isSelected() ? LibraryLevel.Global : LibraryLevel.Project);
        } else {
            return null;
        }
    }

    public JComponent getMainPanel() {
        return mainPanel;
    }

    public String getScalaHome() {
        return setScalaHomeRadioButton.isSelected() ? scalaHome.getText() : null;
    }

    public LibraryId getCompilerLibraryId() {
        return extractLibraryId(existentCompilerComboBox, compilerNameField);
    }

    public LibraryId getStandardLibraryId() {
        return extractLibraryId(existentLibraryComboBox, libraryNameField);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(10, 5, new Insets(0, 0, 0, 0), -1, -1));
        existentLibraryRadioButton = new JRadioButton();
        existentLibraryRadioButton.setText("Existent Library");
        mainPanel.add(existentLibraryRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        setScalaHomeRadioButton = new JRadioButton();
        setScalaHomeRadioButton.setText("Set Scala Home");
        mainPanel.add(setScalaHomeRadioButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        configLaterRadioButton = new JRadioButton();
        configLaterRadioButton.setText("Config later");
        mainPanel.add(configLaterRadioButton, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        existentLibraryComboBox = new JComboBox();
        mainPanel.add(existentLibraryComboBox, new GridConstraints(3, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        scalaHome = new TextFieldWithBrowseButton();
        mainPanel.add(scalaHome, new GridConstraints(5, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Compiler library:");
        mainPanel.add(label1, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        compilerNameField = new JTextField();
        mainPanel.add(compilerNameField, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        scalaHomeComment = new JLabel();
        scalaHomeComment.setText("");
        mainPanel.add(scalaHomeComment, new GridConstraints(6, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        existentCompilerComboBox = new JComboBox();
        mainPanel.add(existentCompilerComboBox, new GridConstraints(2, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Compiler library:");
        mainPanel.add(label2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Standard library:");
        mainPanel.add(label3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Scala settings:");
        mainPanel.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        mainPanel.add(separator1, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        downloadScalaButton = new JButton();
        downloadScalaButton.setText("Download Scala...");
        mainPanel.add(downloadScalaButton, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Standard library:");
        mainPanel.add(label5, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        libraryNameField = new JTextField();
        mainPanel.add(libraryNameField, new GridConstraints(7, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        makeGlobalLibrariesCheckBox = new JCheckBox();
        makeGlobalLibrariesCheckBox.setText("Make global libraries");
        mainPanel.add(makeGlobalLibrariesCheckBox, new GridConstraints(8, 1, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private class ExistentLibraryRenderer extends ColoredListCellRenderer {
        private final String libraryNameBase;

        private ExistentLibraryRenderer(String libraryNameBase) {
            this.libraryNameBase = libraryNameBase;
        }

        @Override
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            if (value == null) return;
            Library library = (Library) value;

            append(library.getName() + "  ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            append(
                    Libraries.extractLibraryVersion(library, "scala-" + libraryNameBase + ".jar", libraryNameBase + ".properties"),
                    SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
    }
}
