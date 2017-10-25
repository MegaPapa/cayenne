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
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.action.LoadDbSchemaAction;
import org.apache.cayenne.modeler.action.ReverseEngineeringAction;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.modeler.dialog.db.load.TransferableNode;
import org.apache.cayenne.modeler.event.DataMapDisplayEvent;
import org.apache.cayenne.modeler.event.DataMapDisplayListener;

import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;

/**
 * @since 4.1
 */
public class DbImportView extends JPanel {

    private static final String MAIN_LAYOUT = "fill:160dlu, 5dlu, fill:50dlu, 5dlu, fill:160dlu";
    private static final String HEADER_LAYOUT = "fill:120dlu, 15dlu, fill:25dlu";
    private static final String BUTTON_PANEL_LAYOUT = "fill:50dlu";
    private static final int ALL_LINE_SPAN = 5;

    private TreeToolbarPanel treeToolbar;
    private ReverseEngineeringTreePanel treePanel;
    private ReverseEngineeringConfigPanel configPanel;
    private DraggableTreePanel draggableTreePanel;

    private ProjectController projectController;

    private DbTreeColorMap colorMap;

    DbImportView(ProjectController projectController) {
        this.projectController = projectController;
        initFormElements();
        initListeners();
        buildForm();
        colorMap.buildColorMap();
    }

    private void initListeners() {
        projectController.addDataMapDisplayListener(new DataMapDisplayListener() {

            public void currentDataMapChanged(DataMapDisplayEvent e) {
                DataMap map = e.getDataMap();
                treePanel.getReverseEngineeringTree().stopEditing();
                if (map != null) {
                    treeToolbar.unlockButtons();
                    ReverseEngineering reverseEngineering = DbImportView.this.projectController.getApplication().
                            getMetaData().get(map, ReverseEngineering.class);
                    if (reverseEngineering == null) {
                        reverseEngineering = new ReverseEngineering();
                        DbImportView.this.projectController.getApplication().getMetaData().add(map, reverseEngineering);
                    }
                    configPanel.fillCheckboxes(reverseEngineering);
                    configPanel.initializeTextFields(reverseEngineering);
                    treePanel.updateTree();
                    DbImportTreeNode root = ((DbImportTreeNode)draggableTreePanel.getSourceTree().getModel().getRoot());
                    root.removeAllChildren();
                    DbImportModel model = (DbImportModel) draggableTreePanel.getSourceTree().getModel();
                    model.reload();
                    draggableTreePanel.getSourceTree().setEnabled(false);
                    draggableTreePanel.getMoveButton().setEnabled(false);
                    draggableTreePanel.getMoveInvertButton().setEnabled(false);
                }
            }
        });
    }

    private void buildForm() {
        FormLayout buttonPanelLayout = new FormLayout(BUTTON_PANEL_LAYOUT);
        DefaultFormBuilder buttonBuilder = new DefaultFormBuilder(buttonPanelLayout);
        buttonBuilder.append(draggableTreePanel.getMoveButton());
        buttonBuilder.append(draggableTreePanel.getMoveInvertButton());

        FormLayout layout = new FormLayout(MAIN_LAYOUT);
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Database Import Configuration");
        builder.append(treeToolbar, ALL_LINE_SPAN);

        FormLayout headerLayout = new FormLayout(HEADER_LAYOUT);

        DefaultFormBuilder reverseEngineeringHeaderBuilder = new DefaultFormBuilder(headerLayout);
        reverseEngineeringHeaderBuilder.append("Reverse Engineering Configuration");
        ReverseEngineeringAction reverseEngineeringAction = projectController.getApplication().getActionManager().
                getAction(ReverseEngineeringAction.class);
        reverseEngineeringAction.setView(this);
        reverseEngineeringHeaderBuilder.append(reverseEngineeringAction.buildButton(0));
        builder.append(reverseEngineeringHeaderBuilder.getPanel());

        DefaultFormBuilder databaseHeaderBuilder = new DefaultFormBuilder(headerLayout);
        databaseHeaderBuilder.append("Database");
        LoadDbSchemaAction loadDbSchemaAction = projectController.getApplication().getActionManager().
                getAction(LoadDbSchemaAction.class);
        loadDbSchemaAction.setDraggableTreePanel(draggableTreePanel);
        databaseHeaderBuilder.append(loadDbSchemaAction.buildButton(0));

        builder.append("");
        builder.append(databaseHeaderBuilder.getPanel());
        builder.append(treePanel);
        builder.append(buttonBuilder.getPanel());
        builder.append(draggableTreePanel);
        builder.append(configPanel, ALL_LINE_SPAN);
        this.setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
    }

    private void initFormElements() {
        DbImportTreeNode root = new DbImportTreeNode(new ReverseEngineering());
        DbImportTreeNode draggableTreeRoot = new DbImportTreeNode(new ReverseEngineering());
        DbImportTree reverseEngineeringTree = new DbImportTree(root);
        DbImportTree draggableTree = new DbImportTree(new TransferableNode(draggableTreeRoot));
        DbImportModel model = new DbImportModel(root);
        DbImportModel draggableTreeModel = new DbImportModel(draggableTreeRoot);

        draggableTree.setRootVisible(false);
        draggableTree.setShowsRootHandles(true);
        draggableTree.setModel(draggableTreeModel);
        reverseEngineeringTree.setRootVisible(false);
        reverseEngineeringTree.setModel(model);
        reverseEngineeringTree.setShowsRootHandles(true);
        reverseEngineeringTree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        draggableTreePanel = new DraggableTreePanel(projectController, draggableTree, reverseEngineeringTree);
        treeToolbar = new TreeToolbarPanel(projectController, reverseEngineeringTree);
        treePanel = new ReverseEngineeringTreePanel(projectController, reverseEngineeringTree);
        treePanel.setTreeToolbar(treeToolbar);

        colorMap = new DbTreeColorMap(treePanel.getReverseEngineeringTree(), draggableTreePanel.getSourceTree());
        model.setColorMap(colorMap);
        draggableTreeModel.setColorMap(colorMap);
        ((ColorTreeRenderer) draggableTreePanel.getSourceTree().getCellRenderer()).setColorMap(colorMap);

        configPanel = new ReverseEngineeringConfigPanel(projectController);
    }

    public boolean isSkipRelationshipsLoading() {
        return configPanel.getSkipRelationshipsLoading().isSelected();
    }

    public boolean isSkipPrimaryKeyLoading() {
        return configPanel.getSkipPrimaryKeyLoading().isSelected();
    }

    public boolean isForceDataMapCatalog() {
        return configPanel.getForceDataMapCatalog().isSelected();
    }

    public boolean isForceDataMapSchema() {
        return configPanel.getForceDataMapSchema().isSelected();
    }

    public boolean isUsePrimitives() {
        return configPanel.getUsePrimitives().isSelected();
    }

    public boolean isUseJava7Typed() {
        return configPanel.getUseJava7Types().isSelected();
    }

    public String getMeaningfulPk() {
        return "".equals(configPanel.getMeaningfulPk().getComponent().getText())
                ? null : configPanel.getMeaningfulPk().getComponent().getText();
    }

    public String getNamingStrategy() {
        return (String) configPanel.getStrategyCombo().getSelectedItem();
    }

    public String getStripFromTableNames() {
        return configPanel.getStripFromTableNames().getComponent().getText();
    }

    public String getDefaultPackage() {
        return configPanel.getDefaultPackage().getComponent().getText();
    }

}
