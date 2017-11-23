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

import org.apache.cayenne.dbsync.reverse.dbimport.Catalog;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.action.AddCatalogAction;
import org.apache.cayenne.modeler.action.AddExcludeColumnAction;
import org.apache.cayenne.modeler.action.AddExcludeProcedureAction;
import org.apache.cayenne.modeler.action.AddExcludeTableAction;
import org.apache.cayenne.modeler.action.AddIncludeColumnAction;
import org.apache.cayenne.modeler.action.AddIncludeProcedureAction;
import org.apache.cayenne.modeler.action.AddIncludeTableAction;
import org.apache.cayenne.modeler.action.AddPatternParamAction;
import org.apache.cayenne.modeler.action.AddSchemaAction;
import org.apache.cayenne.modeler.action.DeleteNodeAction;
import org.apache.cayenne.modeler.action.EditNodeAction;
import org.apache.cayenne.modeler.action.GetDbConnectionAction;
import org.apache.cayenne.modeler.action.TreeManipulationAction;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;

import javax.swing.JButton;
import javax.swing.JToolBar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 4.1
 */
class TreeToolbarPanel extends JToolBar {

    private JButton schemaButton;
    private JButton catalogButton;
    private JButton includeTableButton;
    private JButton excludeTableButton;
    private JButton includeColumnButton;
    private JButton excludeColumnButton;
    private JButton includeProcedureButton;
    private JButton excludeProcedureButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton configureButton;
    private DbImportTree reverseEngineeringTree;

    private Map<Class, List<JButton>> levels;
    private ProjectController projectController;

    TreeToolbarPanel(ProjectController projectController, DbImportTree reverseEngineeringTree) {
        this.projectController = projectController;
        this.reverseEngineeringTree = reverseEngineeringTree;
        createButtons();
        initLevels();
        addButtons();
    }

    void unlockButtons() {
        changeToolbarButtonsState(true);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    void lockButtons() {
        if (reverseEngineeringTree.getLastSelectedPathComponent() != null) {
            DbImportTreeNode selectedNode = ((DbImportTreeNode) reverseEngineeringTree.getLastSelectedPathComponent());
            DbImportTreeNode parentNode = (DbImportTreeNode) selectedNode.getParent();
            if (parentNode != null) {
                lockButtons(parentNode.getUserObject());
            } else {
                unlockButtons();
            }
        } else {
            changeToolbarButtonsState(true);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        if (reverseEngineeringTree.getSelectionPaths() != null) {
            if (reverseEngineeringTree.getSelectionPaths().length > 1) {
                changeToolbarButtonsState(false);
                deleteButton.setEnabled(true);
            }
        }
    }

    private void initLevels() {
        levels = new HashMap<>();

        List<JButton> rootLevelButtons = new ArrayList<>();
        rootLevelButtons.add(catalogButton);
        rootLevelButtons.add(schemaButton);
        rootLevelButtons.add(includeTableButton);
        rootLevelButtons.add(excludeTableButton);
        rootLevelButtons.add(includeColumnButton);
        rootLevelButtons.add(excludeColumnButton);
        rootLevelButtons.add(includeProcedureButton);
        rootLevelButtons.add(excludeProcedureButton);

        List<JButton> catalogLevelButtons = new ArrayList<>();
        catalogLevelButtons.add(schemaButton);
        catalogLevelButtons.add(includeTableButton);
        catalogLevelButtons.add(excludeTableButton);
        catalogLevelButtons.add(includeColumnButton);
        catalogLevelButtons.add(excludeColumnButton);
        catalogLevelButtons.add(includeProcedureButton);
        catalogLevelButtons.add(excludeProcedureButton);

        List<JButton> schemaLevelButtons = new ArrayList<>();
        schemaLevelButtons.add(includeTableButton);
        schemaLevelButtons.add(excludeTableButton);
        schemaLevelButtons.add(includeColumnButton);
        schemaLevelButtons.add(excludeColumnButton);
        schemaLevelButtons.add(includeProcedureButton);
        schemaLevelButtons.add(excludeProcedureButton);

        List<JButton> includeTableLevelButtons = new ArrayList<>();
        includeTableLevelButtons.add(includeColumnButton);
        includeTableLevelButtons.add(excludeColumnButton);

        levels.put(ReverseEngineering.class, rootLevelButtons);
        levels.put(Catalog.class, catalogLevelButtons);
        levels.put(Schema.class, schemaLevelButtons);
        levels.put(IncludeTable.class, includeTableLevelButtons);
    }

    private void addButtons() {
        this.setFloatable(false);
        this.add(catalogButton);
        this.add(schemaButton);
        this.addSeparator();
        this.add(includeTableButton);
        this.add(excludeTableButton);
        this.add(includeColumnButton);
        this.add(excludeColumnButton);
        this.add(includeProcedureButton);
        this.add(excludeProcedureButton);
        this.add(editButton);
        this.addSeparator();
        this.add(deleteButton);
        this.add(configureButton);
    }

    private void changeToolbarButtonsState(boolean state) {
        schemaButton.setEnabled(state);
        catalogButton.setEnabled(state);
        includeTableButton.setEnabled(state);
        excludeTableButton.setEnabled(state);
        includeColumnButton.setEnabled(state);
        excludeColumnButton.setEnabled(state);
        includeProcedureButton.setEnabled(state);
        excludeProcedureButton.setEnabled(state);
        editButton.setEnabled(state);
        deleteButton.setEnabled(state);
    }

    private void lockButtons(Object userObject) {
        changeToolbarButtonsState(false);
        List<JButton> buttons = levels.get(userObject.getClass());
        for (JButton button : buttons) {
            button.setEnabled(true);
        }
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private <T extends TreeManipulationAction> JButton createButton(Class<T> actionClass, int position) {
        TreeManipulationAction action = projectController.getApplication().getActionManager().getAction(actionClass);
        action.setTree(reverseEngineeringTree);
        return action.buildButton(position);
    }

    private <T extends AddPatternParamAction> JButton createButton(Class<T> actionClass, int position, Class paramClass) {
        AddPatternParamAction action = projectController.getApplication().getActionManager().getAction(actionClass);
        action.setTree(reverseEngineeringTree);
        action.setParamClass(paramClass);
        return action.buildButton(position);
    }

    private void createButtons() {
        schemaButton = createButton(AddSchemaAction.class, 0);
        catalogButton = createButton(AddCatalogAction.class, 0);
        includeTableButton = createButton(AddIncludeTableAction.class, 1);
        excludeTableButton = createButton(AddExcludeTableAction.class, 2, ExcludeTable.class);
        includeColumnButton = createButton(AddIncludeColumnAction.class, 2, IncludeColumn.class);
        excludeColumnButton = createButton(AddExcludeColumnAction.class, 2, ExcludeColumn.class);
        includeProcedureButton = createButton(AddIncludeProcedureAction.class, 2, IncludeProcedure.class);
        excludeProcedureButton = createButton(AddExcludeProcedureAction.class, 3, ExcludeProcedure.class);
        editButton = createButton(EditNodeAction.class, 0);
        deleteButton = createButton(DeleteNodeAction.class, 0);
        GetDbConnectionAction action = projectController.getApplication().getActionManager().getAction(GetDbConnectionAction.class);
        configureButton = action.buildButton(0);
    }
}
