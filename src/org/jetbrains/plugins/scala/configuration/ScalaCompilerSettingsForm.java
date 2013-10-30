package org.jetbrains.plugins.scala.configuration;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.projectRoots.ui.PathEditor;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Fatin
 */
public class ScalaCompilerSettingsForm {
  private JPanel myContentPanel;
  private JPanel myPluginsPanel;
  private RawCommandLineEditor myAdditionalCompilerOptions;
  private JComboBox<DebuggingInfoLevel> myDebuggingInfoLevel;
  private JCheckBox myWarnings;
  private JCheckBox myDeprecationWarnings;
  private JCheckBox myUncheckedWarnings;
  private JCheckBox myOptimiseBytecode;
  private JCheckBox myExplainTypeErrors;
  private JCheckBox myContinuations;
  private JComboBox<CompileOrder> myCompileOrder;
  private PathEditor myPluginsEditor = new PathEditor(new FileChooserDescriptor(true, false, true, false, false, true));

  public ScalaCompilerSettingsForm() {
    myCompileOrder.setModel(new DefaultComboBoxModel<CompileOrder>(CompileOrder.values()));
    myDebuggingInfoLevel.setModel(new DefaultComboBoxModel<DebuggingInfoLevel>(DebuggingInfoLevel.values()));

    myPluginsPanel.setBorder(IdeBorderFactory.createBorder());
    myPluginsPanel.add(myPluginsEditor.createComponent(), BorderLayout.CENTER);
  }

  public ScalaCompilerSettingsState getState() {
    ScalaCompilerSettingsState state = new ScalaCompilerSettingsState();
    state.compileOrder = (CompileOrder) myCompileOrder.getSelectedItem();
    state.warnings = myWarnings.isSelected();
    state.deprecationWarnings = myDeprecationWarnings.isSelected();
    state.uncheckedWarnings = myUncheckedWarnings.isSelected();
    state.optimiseBytecode = myOptimiseBytecode.isSelected();
    state.explainTypeErrors = myExplainTypeErrors.isSelected();
    state.continuations = myContinuations.isSelected();
    state.debuggingInfoLevel = (DebuggingInfoLevel) myDebuggingInfoLevel.getSelectedItem();
    state.additionalCompilerOptions = myAdditionalCompilerOptions.getText();
    state.plugins = virtualFilesToPaths(myPluginsEditor.getRoots());
    return state;
  }

  private static String[] virtualFilesToPaths(VirtualFile[] files) {
    String[] result = new String[files.length];
    int i = 0;
    for (VirtualFile file : files) {
      result[i] = file.getUrl();
      i++;
    }
    return result;
  }

  public void setState(ScalaCompilerSettingsState state) {
    myCompileOrder.setSelectedItem(state.compileOrder);
    myWarnings.setSelected(state.warnings);
    myDeprecationWarnings.setSelected(state.deprecationWarnings);
    myUncheckedWarnings.setSelected(state.uncheckedWarnings);
    myOptimiseBytecode.setSelected(state.optimiseBytecode);
    myExplainTypeErrors.setSelected(state.explainTypeErrors);
    myContinuations.setSelected(state.continuations);
    myDebuggingInfoLevel.setSelectedItem(state.debuggingInfoLevel);
    myAdditionalCompilerOptions.setText(state.additionalCompilerOptions);
    myPluginsEditor.resetPath(pathsToVirtualFiles(state.plugins));
  }

  private static List<VirtualFile> pathsToVirtualFiles(String[] urls) {
    List<VirtualFile> result = new ArrayList<VirtualFile>(urls.length);
    for (String url : urls) {
      VirtualFile file = VfsUtil.findFileByIoFile(new File(VfsUtil.urlToPath(url)), true);
      result.add(file);
    }
    return result;
  }

  public JPanel getComponent() {
    return myContentPanel;
  }
}