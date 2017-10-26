/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.modeler.editor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.cayenne.gen.ClassGenerationAction;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.action.GenerateCodeAction;
import org.apache.cayenne.modeler.dialog.ErrorDebugDialog;
import org.apache.cayenne.modeler.dialog.codegen.GeneratorTabController;
import org.apache.cayenne.modeler.dialog.codegen.StandardPanelComponent;
import org.apache.cayenne.swing.BindingBuilder;
import org.apache.cayenne.swing.control.ActionLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 4.1
 */
public class CgenView extends JPanel {

    private static Logger logger = LoggerFactory.getLogger(ErrorDebugDialog.class);

    private static final String MAIN_LAYOUT = "fill:80dlu, 3dlu, fill:200dlu, 6dlu, fill:50dlu, 3dlu";
    private static final int FULL_LINE_SPAN = 3;

    protected GeneratorTabController generatorSelector;

    protected Collection<StandardPanelComponent> dataMapLines;
    protected JTextField outputFolder;
    protected JButton selectOutputFolder;

    protected JComboBox generationMode;
    protected JComboBox subclassTemplate;
    protected JComboBox superclassTemplate;
    protected JCheckBox pairs;
    protected JCheckBox overwrite;
    protected JCheckBox usePackagePath;
    protected JTextField outputPattern;
    protected JCheckBox createPropertyNames;

    private DefaultFormBuilder builder;
    private ProjectController projectController;

    protected ActionLink manageTemplatesLink;

    public CgenView(ProjectController projectController) {
        this.projectController = projectController;
        this.dataMapLines = new ArrayList<>();
        this.outputFolder = new JTextField();
        //this.selectOutputFolder = new JButton("Select");

        initUIElements();
        initListeners();
        // assemble
        buildUI();
    }

    private void initUIElements() {
        this.generationMode = new JComboBox();
        this.superclassTemplate = new JComboBox();
        this.subclassTemplate = new JComboBox();
        this.pairs = new JCheckBox();
        this.overwrite = new JCheckBox();
        this.usePackagePath = new JCheckBox();
        this.outputPattern = new JTextField();
        this.createPropertyNames = new JCheckBox();
        this.manageTemplatesLink = new ActionLink("Customize Templates...");
        manageTemplatesLink.setFont(manageTemplatesLink.getFont().deriveFont(10f));
    }

    private void initListeners() {
        pairs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                superclassTemplate.setEnabled(pairs.isSelected());
                overwrite.setEnabled(!pairs.isSelected());
            }
        });
    }

    protected void initBindings() {
        BindingBuilder builder = new BindingBuilder(projectController.getApplication().getBindingFactory(), this);

        builder.bindToAction(this.selectOutputFolder, "generateAction()");

        //generatorSelectedAction();
    }

    public void generateAction() {
        Collection<ClassGenerationAction> generators = generatorSelector.getGenerator();

        if (generators != null) {
            try {
                for (ClassGenerationAction generator : generators) {
                    generator.execute();
                }
                projectController.getApplication().getFrameController().getProjectController().setDirty(true);
                JOptionPane.showMessageDialog(
                        this,
                        "Class generation finished");
            } catch (Exception e) {
                logger.error("Error generating classes", e);
                JOptionPane.showMessageDialog(
                        this,
                        "Error generating classes - " + e.getMessage());
            }
        }
    }

    private void buildUI() {
        GenerateCodeAction action = projectController.getApplication().getActionManager().getAction(GenerateCodeAction.class);
        this.selectOutputFolder = action.buildButton();
        FormLayout layout = new FormLayout(MAIN_LAYOUT);
        builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.append("Output Directory:", outputFolder, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Generation Mode:", generationMode, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Subclass Template:", subclassTemplate, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Superclass Template:", superclassTemplate, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Output Pattern:", outputPattern, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Make Pairs:", pairs, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Use Package Path:", usePackagePath, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Overwrite Subclasses:", overwrite, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Create Property Names:", createPropertyNames, FULL_LINE_SPAN);
        builder.nextLine();

        JPanel links = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        links.add(manageTemplatesLink);

        builder.append(links);
        builder.append("");
        builder.append(selectOutputFolder);

        setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
    }

    public void addDataMapLine(StandardPanelComponent dataMapLine) {
        dataMapLines.add(dataMapLine);
        builder.append(dataMapLine, 4);
        builder.nextLine();
    }

    public JComboBox getGenerationMode() {
        return generationMode;
    }

    public ActionLink getManageTemplatesLink() {
        return manageTemplatesLink;
    }

    public JComboBox getSubclassTemplate() {
        return subclassTemplate;
    }

    public JComboBox getSuperclassTemplate() {
        return superclassTemplate;
    }

    public JCheckBox getOverwrite() {
        return overwrite;
    }

    public JCheckBox getPairs() {
        return pairs;
    }

    public JCheckBox getUsePackagePath() {
        return usePackagePath;
    }

    public JTextField getOutputPattern() {
        return outputPattern;
    }

    public JCheckBox getCreatePropertyNames() {
        return createPropertyNames;
    }

    public JTextField getOutputFolder() {
        return outputFolder;
    }

    public JButton getSelectOutputFolder() {
        return selectOutputFolder;
    }

    public Collection<StandardPanelComponent> getDataMapLines() {
        return dataMapLines;
    }
}
