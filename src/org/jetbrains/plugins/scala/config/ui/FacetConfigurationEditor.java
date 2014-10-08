package org.jetbrains.plugins.scala.config.ui;

import com.intellij.facet.ui.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.plugins.scala.config.*;
import org.jetbrains.plugins.scala.lang.languageLevel.ScalaLanguageLevel;
import scala.Enumeration;
import scala.Option;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Pavel.Fatin, 26.07.2010
 */
public class FacetConfigurationEditor extends FacetEditorTab {
    private JPanel panelContent;
    private JComboBox myCompilerLibrary;
    private RawCommandLineEditor myCompilerOptions;
    private JButton addButton;
    private JButton removeButton;
    private JButton editButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private MyTableView<CompilerPlugin> tablePlugins;
    private JPanel panelPlugins;
    private JCheckBox myEnableWarnings;
    private JCheckBox myDeprecationWarnings;
    private JCheckBox myOptimiseBytecode;
    private JCheckBox myUncheckedWarnings;
    private JComboBox myDebuggingInfoLevel;
    private JCheckBox myExplainTypeErrors;
    private JCheckBox myEnableContinuations;
    private RawCommandLineEditor myVmParameters;
    private JTextField myMaximumHeapSize;
    private JTextField basePackageField;
    private JRadioButton myFSCRadioButton;
    private JRadioButton myRunSeparateCompilerRadioButton;
    private JLabel myCompilerLibraryLabel;
    private JLabel myMaximumHeapSizeLabel;
    private JLabel myVmParametersLabel;
    private LinkLabel myFscSettings;
    private JComboBox languageLevelComboBox;
    private JPanel myFscSwitchPanel;
    private JPanel myJvmParametersPanel;
    private JComboBox myCompileOrder;
    private JPanel myCompileOrderPanel;

    private MyAction myAddPluginAction = new AddPluginAction();
    private MyAction myRemovePluginAction = new RemovePluginAction();
    private MyAction myEditPluginAction = new EditPluginAction();
    private MyAction myMoveUpPluginAction = new MoveUpPluginAction();
    private MyAction myMoveDownPluginAction = new MoveDownPluginAction();

    private ConfigurationData myData;
    private FacetEditorContext myEditorContext;
    private FacetValidatorsManager myValidatorsManager;
    private List<CompilerPlugin> myPlugins = new ArrayList<CompilerPlugin>();

    private static final FileChooserDescriptor CHOOSER_DESCRIPTOR =
            new FileChooserDescriptor(false, false, true, true, false, true);
    private final LibraryRenderer myLibraryRenderer;

    static {
        CHOOSER_DESCRIPTOR.setDescription("Select Scala compiler plugin JAR");
    }

    public FacetConfigurationEditor(ConfigurationData data, FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        myData = data;
        myEditorContext = editorContext;
        myValidatorsManager = validatorsManager;

        myDebuggingInfoLevel.setModel(new DefaultComboBoxModel(DebuggingInfoLevel.values()));
        myDebuggingInfoLevel.setRenderer(new DebuggingInfoRenderer());
        myLibraryRenderer = new LibraryRenderer(myCompilerLibrary);
        myCompilerLibrary.setRenderer(myLibraryRenderer);
        myCompileOrder.setModel(new DefaultComboBoxModel(CompileOrder.values()));
        myCompileOrder.setRenderer(new CompileOrderRenderer());

        myEnableWarnings.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateCheckboxesState();
            }
        });

        myFSCRadioButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateCompilerSection();
            }
        });

        CompilerPluginsTableModel model = new CompilerPluginsTableModel();
        model.setItems(myPlugins);
        tablePlugins.setModelAndUpdateColumns(model);

        addButton.setAction(myAddPluginAction);
        removeButton.setAction(myRemovePluginAction);
        editButton.setAction(myEditPluginAction);
        moveUpButton.setAction(myMoveUpPluginAction);
        moveDownButton.setAction(myMoveDownPluginAction);

        myAddPluginAction.registerOn(panelPlugins);
        myRemovePluginAction.registerOn(tablePlugins);
        myEditPluginAction.registerOn(tablePlugins);
        myMoveUpPluginAction.registerOn(tablePlugins);
        myMoveDownPluginAction.registerOn(tablePlugins);

        ListSelectionModel selectionModel = tablePlugins.getSelectionModel();
        selectionModel.addListSelectionListener(myAddPluginAction);
        selectionModel.addListSelectionListener(myRemovePluginAction);
        selectionModel.addListSelectionListener(myEditPluginAction);
        selectionModel.addListSelectionListener(myMoveUpPluginAction);
        selectionModel.addListSelectionListener(myMoveDownPluginAction);

        tablePlugins.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    myEditPluginAction.perform();
                }
            }
        });

        // TODO in-process build is now depreacted
        final boolean externalCompiler = true;

        myValidatorsManager.registerValidator(new FacetEditorValidator() {
            @Override
            public ValidationResult check() {
                ValidationResult libraryResult = externalCompiler || !myFSCRadioButton.isSelected()
                        ? checkCompilerLibrary((LibraryDescriptor) myCompilerLibrary.getSelectedItem())
                        : ValidationResult.OK;

                ValidationResult continuationsResult = myEnableContinuations.isSelected()
                        ? checkContinuationsPlugin(getPluginsModel().getItems())
                        : ValidationResult.OK;

                return conjunctionOf(libraryResult, continuationsResult);
            }
        }, myCompilerLibrary, myFSCRadioButton, myEnableContinuations, tablePlugins);


        Enumeration.Value[] values = ScalaLanguageLevel.valuesArray();
        for (Enumeration.Value value : values) {
            languageLevelComboBox.addItem(value.toString());
        }
        languageLevelComboBox.setSelectedItem(data.getLanguageLevel());

        myAddPluginAction.update();
        myRemovePluginAction.update();
        myEditPluginAction.update();
        myMoveUpPluginAction.update();
        myMoveDownPluginAction.update();

        myFscSettings.setListener(new LinkListener() {
            public void linkSelected(LinkLabel aSource, Object aLinkData) {
                ShowSettingsUtil.getInstance().showSettingsDialog(myEditorContext.getProject(), "Scala Compiler");
            }
        }, null);

        myFscSwitchPanel.setVisible(!externalCompiler);
        myJvmParametersPanel.setVisible(!externalCompiler);
        myCompileOrderPanel.setVisible(externalCompiler);
    }

    private void updateCompilerSection() {
        // TODO in-process build is now depreacted
        boolean externalCompiler = true;
        boolean b = externalCompiler || !myFSCRadioButton.isSelected();
        myCompilerLibraryLabel.setEnabled(b);
        myCompilerLibrary.setEnabled(b);
        myMaximumHeapSizeLabel.setEnabled(b);
        myMaximumHeapSize.setEnabled(b);
        myVmParametersLabel.setEnabled(b);
        myVmParameters.setEnabled(b);
    }

    private void updateCheckboxesState() {
        boolean enabled = myEnableWarnings.isSelected();
        myDeprecationWarnings.setEnabled(enabled);
        myUncheckedWarnings.setEnabled(enabled);
    }

    private static ValidationResult checkCompilerLibrary(LibraryDescriptor descriptor) {
        if (descriptor == null || descriptor.data().isEmpty())
            return new ValidationResult("No compiler library selected");

        String libraryName = "Compiler library";

        CompilerLibraryData compilerLibraryData = (CompilerLibraryData) descriptor.data().get();

        Option<String> compilerLibraryProblem = compilerLibraryData.problem();

        if (compilerLibraryProblem.isDefined())
            return new ValidationResult(libraryName + ": " + compilerLibraryProblem.get());

        return ValidationResult.OK;
    }

    private static ValidationResult checkContinuationsPlugin(List<CompilerPlugin> plugins) {
        for (CompilerPlugin plugin : plugins) {
            if ("continuations".equals(plugin.name()))
                return ValidationResult.OK;
        }
        return new ValidationResult("No continuations compiler plugin jar added");
    }

    private static ValidationResult conjunctionOf(ValidationResult a, ValidationResult b) {
        return a.isOk() ? b : a;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myCompilerLibrary;
    }

    @Override
    public void onTabEntering() {
        updateLibrariesList();
    }

    private CompilerPluginsTableModel getPluginsModel() {
        return (CompilerPluginsTableModel) tablePlugins.getModel();
    }

    @Nls
    public String getDisplayName() {
        return "Scala";
    }

    public JComponent createComponent() {
        return panelContent;
    }

    public boolean isModified() {
        ConfigurationData data = new ConfigurationData();
        update(data);
        return !myData.equals(data);
    }

    public void apply() throws ConfigurationException {
        update(myData);
    }

    private void update(ConfigurationData data) {
        data.setBasePackage(basePackageField.getText());
        data.setFsc(myFSCRadioButton.isSelected());
        data.setCompilerLibraryName(getCompilerLibraryName());
        data.setCompilerLibraryLevel(getCompilerLibraryLevel());

        data.setCompileOrder((CompileOrder) myCompileOrder.getSelectedItem());

        try {
            data.setMaximumHeapSize(Integer.parseInt(myMaximumHeapSize.getText().trim()));
        } catch (NumberFormatException e) {
            data.setMaximumHeapSize(myData.getMaximumHeapSize());
        }

        data.setVmOptions(myVmParameters.getText().trim());

        data.setWarnings(myEnableWarnings.isSelected());
        data.setDeprecationWarnings(myDeprecationWarnings.isSelected());
        data.setUncheckedWarnings(myUncheckedWarnings.isSelected());
        data.setOptimiseBytecode(myOptimiseBytecode.isSelected());
        data.setExplainTypeErrors(myExplainTypeErrors.isSelected());
        data.setContinuations(myEnableContinuations.isSelected());

        data.setDebuggingInfoLevel((DebuggingInfoLevel) myDebuggingInfoLevel.getSelectedItem());
        data.setCompilerOptions(myCompilerOptions.getText().trim());

        data.setPluginPaths(CompilerPlugin.toPaths(myPlugins));

        data.setLanguageLevel((String) languageLevelComboBox.getSelectedItem());

        updateCheckboxesState();
    }

    public void reset() {
        basePackageField.setText(myData.getBasePackage());
        myFSCRadioButton.setSelected(myData.getFsc());
        myRunSeparateCompilerRadioButton.setSelected(!myData.getFsc());
        updateLibrariesList();
        setCompilerLibraryById(new LibraryId(myData.getCompilerLibraryName(), myData.getCompilerLibraryLevel()));

        myCompileOrder.setSelectedItem(myData.getCompileOrder());

        myMaximumHeapSize.setText(Integer.toString(myData.getMaximumHeapSize()));
        myVmParameters.setText(myData.getVmOptions());

        myEnableWarnings.setSelected(myData.getWarnings());
        myDeprecationWarnings.setSelected(myData.getDeprecationWarnings());
        myUncheckedWarnings.setSelected(myData.getUncheckedWarnings());
        myOptimiseBytecode.setSelected(myData.getOptimiseBytecode());
        myExplainTypeErrors.setSelected(myData.getExplainTypeErrors());
        myEnableContinuations.setSelected(myData.getContinuations());

        myDebuggingInfoLevel.setSelectedItem(myData.getDebuggingInfoLevel());
        myCompilerOptions.setText(myData.getCompilerOptions());
        languageLevelComboBox.setSelectedItem(myData.getLanguageLevel());

        myPlugins = new ArrayList(CompilerPlugin.fromPaths(myData.getPluginPaths(), myEditorContext.getModule()));
        getPluginsModel().setItems(myPlugins);
    }

    private void updateLibrariesList() {
        LibraryId id = getCompilerLibraryId();

        LibraryDescriptor[] items = (LibraryDescriptor[]) LibraryDescriptor.compilersFor(myEditorContext.getProject());
        DefaultComboBoxModel model = new DefaultComboBoxModel(items);
        model.insertElementAt(null, 0);
        myCompilerLibrary.setModel(model);
        myLibraryRenderer.setPrefixLength(lastIndexOfProperItemIn(items) + 1);

        setCompilerLibraryById(id);
    }

    private static int lastIndexOfProperItemIn(LibraryDescriptor[] descriptors) {
        int result = -1;
        for (LibraryDescriptor descriptor : descriptors) {
            if (descriptor.data().get().problem().isDefined()) break;
            result++;
        }
        return result;
    }

    private String getCompilerLibraryName() {
        LibraryId id = getCompilerLibraryId();
        return id == null ? "" : id.name();
    }

    private LibraryLevel getCompilerLibraryLevel() {
        LibraryId id = getCompilerLibraryId();
        return id == null ? null : id.level();
    }

    private LibraryId getCompilerLibraryId() {
        LibraryDescriptor descriptor = (LibraryDescriptor) myCompilerLibrary.getSelectedItem();
        return descriptor == null ? LibraryId.empty() : descriptor.id();
    }

    public void setCompilerLibraryById(LibraryId id) {
        if (id.isEmpty()) {
//      myCompilerLibrary.addItem(null);
            myCompilerLibrary.setSelectedItem(null);
        } else {
            LibraryDescriptor descriptor = findLibraryDescriptorFor(id);
            if (descriptor == null) {
                LibraryDescriptor newId = LibraryDescriptor.createFor(id);
                myCompilerLibrary.addItem(newId);
                myCompilerLibrary.setSelectedItem(newId);
            } else {
                myCompilerLibrary.setSelectedItem(descriptor);
            }
        }
    }

    public LibraryDescriptor findLibraryDescriptorFor(LibraryId id) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) myCompilerLibrary.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            LibraryDescriptor entry = (LibraryDescriptor) model.getElementAt(i);
            if (entry != null && entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    public void disposeUIResources() {
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
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
        panelContent = new JPanel();
        panelContent.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPlugins = new JPanel();
        panelPlugins.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 3, -1));
        panelContent.add(panelPlugins, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPlugins.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        addButton = new JButton();
        addButton.setText("Add...");
        addButton.setMnemonic('A');
        addButton.setDisplayedMnemonicIndex(0);
        panel1.add(addButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setText("Remove");
        removeButton.setMnemonic('R');
        removeButton.setDisplayedMnemonicIndex(0);
        panel1.add(removeButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editButton = new JButton();
        editButton.setText("Edit...");
        editButton.setMnemonic('E');
        editButton.setDisplayedMnemonicIndex(0);
        panel1.add(editButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        moveUpButton = new JButton();
        moveUpButton.setText("Move Up");
        moveUpButton.setMnemonic('U');
        moveUpButton.setDisplayedMnemonicIndex(5);
        panel1.add(moveUpButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        moveDownButton = new JButton();
        moveDownButton.setText("Move Down");
        moveDownButton.setMnemonic('D');
        moveDownButton.setDisplayedMnemonicIndex(5);
        panel1.add(moveDownButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelPlugins.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tablePlugins = new MyTableView();
        scrollPane1.setViewportView(tablePlugins);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 10, 0), -1, -1));
        panelContent.add(panel2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        myJvmParametersPanel = new JPanel();
        myJvmParametersPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(myJvmParametersPanel, new GridConstraints(1, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        myVmParameters = new RawCommandLineEditor();
        myVmParameters.setDialogCaption("Java VM command line parameters");
        myJvmParametersPanel.add(myVmParameters, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(250, -1), null, null, 0, false));
        myVmParametersLabel = new JLabel();
        myVmParametersLabel.setText("VM parameters:");
        myVmParametersLabel.setDisplayedMnemonic('V');
        myVmParametersLabel.setDisplayedMnemonicIndex(0);
        myJvmParametersPanel.add(myVmParametersLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myMaximumHeapSizeLabel = new JLabel();
        myMaximumHeapSizeLabel.setText("Maximum heap size, MB:");
        myMaximumHeapSizeLabel.setDisplayedMnemonic('M');
        myMaximumHeapSizeLabel.setDisplayedMnemonicIndex(0);
        myJvmParametersPanel.add(myMaximumHeapSizeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myMaximumHeapSize = new JTextField();
        myMaximumHeapSize.setColumns(5);
        myJvmParametersPanel.add(myMaximumHeapSize, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myCompileOrderPanel = new JPanel();
        myCompileOrderPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(myCompileOrderPanel, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        final JLabel label1 = new JLabel();
        label1.setText("Compile order:");
        label1.setDisplayedMnemonic('O');
        label1.setDisplayedMnemonicIndex(8);
        myCompileOrderPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myCompileOrder = new JComboBox();
        myCompileOrderPanel.add(myCompileOrder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
        myCompilerLibraryLabel = new JLabel();
        myCompilerLibraryLabel.setText("Compiler library:");
        myCompilerLibraryLabel.setDisplayedMnemonic('L');
        myCompilerLibraryLabel.setDisplayedMnemonicIndex(9);
        panel4.add(myCompilerLibraryLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myCompilerLibrary = new JComboBox();
        panel4.add(myCompilerLibrary, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myFscSwitchPanel = new JPanel();
        myFscSwitchPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(myFscSwitchPanel, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        myRunSeparateCompilerRadioButton = new JRadioButton();
        myRunSeparateCompilerRadioButton.setText("Use ordinary compiler");
        myFscSwitchPanel.add(myRunSeparateCompilerRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        myFscSwitchPanel.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        myFSCRadioButton = new JRadioButton();
        myFSCRadioButton.setText("Use project FSC");
        panel5.add(myFSCRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myFscSettings = new LinkLabel();
        myFscSettings.setText("Settings");
        panel5.add(myFscSettings, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 10, 0), -1, -1));
        panelContent.add(panel6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
        final JLabel label2 = new JLabel();
        label2.setText("Additional compiler options:");
        label2.setDisplayedMnemonic('O');
        label2.setDisplayedMnemonicIndex(20);
        panel6.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myCompilerOptions = new RawCommandLineEditor();
        myCompilerOptions.setDialogCaption("Additional command-line parameters for Scala compiler");
        panel6.add(myCompilerOptions, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(250, -1), null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Debugging info level:");
        label3.setDisplayedMnemonic('L');
        label3.setDisplayedMnemonicIndex(15);
        panel6.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myDebuggingInfoLevel = new JComboBox();
        panel6.add(myDebuggingInfoLevel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myEnableWarnings = new JCheckBox();
        myEnableWarnings.setText("Enable warnings");
        myEnableWarnings.setMnemonic('W');
        myEnableWarnings.setDisplayedMnemonicIndex(7);
        myEnableWarnings.setToolTipText("Generate warnings");
        panel6.add(myEnableWarnings, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myDeprecationWarnings = new JCheckBox();
        myDeprecationWarnings.setText("Deprecation warnings");
        myDeprecationWarnings.setMnemonic('D');
        myDeprecationWarnings.setDisplayedMnemonicIndex(0);
        myDeprecationWarnings.setToolTipText("Indicate whether source should be compiled with deprecation information");
        panel6.add(myDeprecationWarnings, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        myUncheckedWarnings = new JCheckBox();
        myUncheckedWarnings.setText("Unchecked warnings");
        myUncheckedWarnings.setMnemonic('U');
        myUncheckedWarnings.setDisplayedMnemonicIndex(0);
        myUncheckedWarnings.setToolTipText("Enable detailed unchecked warnings ");
        panel6.add(myUncheckedWarnings, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        myOptimiseBytecode = new JCheckBox();
        myOptimiseBytecode.setText("Optimise bytecode (use with care*)");
        myOptimiseBytecode.setMnemonic('O');
        myOptimiseBytecode.setDisplayedMnemonicIndex(0);
        myOptimiseBytecode.setToolTipText("Generates faster bytecode by applying optimisations to the program. May trigger various compilation problems. Use with care.");
        panel6.add(myOptimiseBytecode, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myExplainTypeErrors = new JCheckBox();
        myExplainTypeErrors.setText("Explain type errors");
        myExplainTypeErrors.setMnemonic('E');
        myExplainTypeErrors.setDisplayedMnemonicIndex(0);
        myExplainTypeErrors.setToolTipText("Explain type errors in more detail");
        panel6.add(myExplainTypeErrors, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myEnableContinuations = new JCheckBox();
        myEnableContinuations.setText("Enable continuations");
        myEnableContinuations.setMnemonic('C');
        myEnableContinuations.setDisplayedMnemonicIndex(7);
        panel6.add(myEnableContinuations, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 10, 0), -1, -1));
        panelContent.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
        final JLabel label4 = new JLabel();
        label4.setText("Base package clause:");
        panel7.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basePackageField = new JTextField();
        basePackageField.setColumns(5);
        panel7.add(basePackageField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Language level:");
        panel7.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        languageLevelComboBox = new JComboBox();
        panel7.add(languageLevelComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final TitledSeparator titledSeparator1 = new TitledSeparator();
        titledSeparator1.setText("Compiler");
        panelContent.add(titledSeparator1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final TitledSeparator titledSeparator2 = new TitledSeparator();
        titledSeparator2.setText("Code");
        panelContent.add(titledSeparator2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final TitledSeparator titledSeparator3 = new TitledSeparator();
        titledSeparator3.setText("Compiler Options");
        panelContent.add(titledSeparator3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final TitledSeparator titledSeparator4 = new TitledSeparator();
        titledSeparator4.setText("Compiler Plugins");
        panelContent.add(titledSeparator4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label1.setLabelFor(myCompileOrder);
        myCompilerLibraryLabel.setLabelFor(myCompilerLibrary);
        label3.setLabelFor(myDebuggingInfoLevel);
        label4.setLabelFor(basePackageField);
        label5.setLabelFor(languageLevelComboBox);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(myFSCRadioButton);
        buttonGroup.add(myRunSeparateCompilerRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelContent;
    }

    private class AddPluginAction extends MyAction {
        private AddPluginAction() {
            super("ACTION_ADD", "&Add...", KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK);
        }

        public void actionPerformed(ActionEvent e) {
            VirtualFile[] files = FileChooser.chooseFiles(CHOOSER_DESCRIPTOR, myEditorContext.getProject(), null);
            tablePlugins.clearSelection();
            for (VirtualFile file : files) {
                String path = CompilerPlugin.pathTo(VfsUtil.virtualToIoFile(file), myEditorContext.getModule());
                CompilerPlugin item = new CompilerPlugin(path, myEditorContext.getModule());
                getPluginsModel().addRow(item);
                tablePlugins.addSelection(item);
            }
            tablePlugins.requestFocusInWindow();
        }
    }

    private class RemovePluginAction extends MyAction {
        private RemovePluginAction() {
            super("ACTION_REMOVE", "&Remove", KeyEvent.VK_DELETE);
        }

        public void actionPerformed(ActionEvent e) {
            tablePlugins.removeSelection();
            tablePlugins.requestFocusInWindow();
        }

        @Override
        public boolean isActive() {
            return tablePlugins.hasSelection();
        }
    }

    private class EditPluginAction extends MyAction {
        private EditPluginAction() {
            super("ACTION_EDIT", "&Edit...", KeyEvent.VK_ENTER);
        }

        public void actionPerformed(ActionEvent e) {
            int index = tablePlugins.getSelectedRow();
            CompilerPlugin plugin = (CompilerPlugin) getPluginsModel().getItem(index);
            EditPathDialog dialog = new EditPathDialog(myEditorContext.getProject(), CHOOSER_DESCRIPTOR);
            dialog.setPath(plugin.path());
            dialog.show();
            if (dialog.isOK()) {
                String path = CompilerPlugin.pathTo(new File(dialog.getPath()), myEditorContext.getModule());
                myPlugins.set(index, new CompilerPlugin(path, myEditorContext.getModule()));
                getPluginsModel().fireTableRowsUpdated(index, index);
            }
        }

        @Override
        public boolean isActive() {
            return tablePlugins.hasSingleSelection();
        }
    }

    private class MoveUpPluginAction extends MyAction {
        private MoveUpPluginAction() {
            super("ACTION_MOVE_UP", "Move &Up", KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK);
        }

        public void actionPerformed(ActionEvent e) {
            tablePlugins.moveSelectionUpUsing(myPlugins);
            tablePlugins.requestFocusInWindow();
        }

        @Override
        public boolean isActive() {
            return tablePlugins.isNotFirstRowSelected();
        }
    }

    private class MoveDownPluginAction extends MyAction {
        private MoveDownPluginAction() {
            super("ACTION_MOVE_DOWN", "Move &Down", KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK);
        }

        public void actionPerformed(ActionEvent e) {
            tablePlugins.moveSelectionDownUsing(myPlugins);
            tablePlugins.requestFocusInWindow();
        }

        @Override
        public boolean isActive() {
            return tablePlugins.isNotLastRowSelected();
        }
    }

}
