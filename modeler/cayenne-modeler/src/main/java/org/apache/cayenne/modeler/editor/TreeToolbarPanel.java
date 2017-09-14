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
import org.apache.cayenne.modeler.action.LoadDbSchemaAction;
import org.apache.cayenne.modeler.action.ReverseEngineeringAction;
import org.apache.cayenne.modeler.action.TreeManipulationAction;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;

import javax.swing.JButton;
import javax.swing.JToolBar;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 4.1
 */
class TreeToolbarPanel extends JToolBar {

    private static final int DEFAULT_LEVEL = -1;
    private static final int FIRST_LEVEL = 0;
    private static final int SECOND_LEVEL = 1;
    private static final int THIRD_LEVEL = 5;
    private static final int FOURTH_LEVEL = 7;

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
    private JButton generateButton;
    private JButton loadDbSchema;
    private DbImportTree reverseEngineeringTree;
    private DraggableTreePanel draggableTreePanel;

    private JButton[] buttons;
    private Map<Class, Integer> levels;
    private ProjectController projectController;
    private ReverseEngineeringAction reverseEngineeringAction;

    TreeToolbarPanel(ProjectController projectController, DbImportTree reverseEngineeringTree, DraggableTreePanel draggableTreePanel) {
        this.projectController = projectController;
        this.reverseEngineeringTree = reverseEngineeringTree;
        this.draggableTreePanel = draggableTreePanel;
        initLevels();
        createButtons();
        addButtons();
        buttons = new JButton[]{catalogButton, schemaButton, includeTableButton, excludeTableButton,
                includeProcedureButton, excludeProcedureButton ,includeColumnButton, excludeColumnButton};
    }

    void unlockButtons() {
        changeToolbarButtonsState(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    void lockButtons() {
        if (reverseEngineeringTree.getLastSelectedPathComponent() != null) {
            lockButtons(((DbImportTreeNode) reverseEngineeringTree.getLastSelectedPathComponent()).getUserObject());
        } else {
            changeToolbarButtonsState(true);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void initLevels() {
        levels = new HashMap<>();
        levels.put(ReverseEngineering.class, DEFAULT_LEVEL);
        levels.put(Catalog.class, FIRST_LEVEL);
        levels.put(Schema.class, SECOND_LEVEL);
        levels.put(IncludeTable.class, THIRD_LEVEL);
        levels.put(ExcludeTable.class, FOURTH_LEVEL);
        levels.put(IncludeColumn.class, FOURTH_LEVEL);
        levels.put(ExcludeColumn.class, FOURTH_LEVEL);
        levels.put(IncludeProcedure.class, FOURTH_LEVEL);
        levels.put(ExcludeProcedure.class, FOURTH_LEVEL);
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
        this.addSeparator();
        this.add(generateButton);
        this.add(loadDbSchema);
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
        changeToolbarButtonsState(true);
        if (levels.get(userObject.getClass()) == DEFAULT_LEVEL) {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        lockButtonsOnLevel(levels.get(userObject.getClass()));
    }

    private void lockButtonsOnLevel(int level) {
        for (int i = 0; i <= level; i++) {
            buttons[i].setEnabled(false);
        }
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
        reverseEngineeringAction = projectController.getApplication().getActionManager().
                getAction(ReverseEngineeringAction.class);
        reverseEngineeringAction.setView((DbImportView) this.getParent());
        generateButton = reverseEngineeringAction.buildButton(0);
        LoadDbSchemaAction loadDbSchemaAction = projectController.getApplication().getActionManager().
                getAction(LoadDbSchemaAction.class);
        loadDbSchemaAction.setDraggableTreePanel(draggableTreePanel);
        loadDbSchema = loadDbSchemaAction.buildButton();

    }

    public void setParent(DbImportView parent) {
        reverseEngineeringAction.setView(parent);
    }
}
