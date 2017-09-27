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

import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.action.DeleteNodeAction;
import org.apache.cayenne.modeler.action.EditNodeAction;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.util.Util;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.util.EventObject;

/**
 * @since 4.1
 */
public class DbImportTreeCellEditor extends DefaultTreeCellEditor {

    private ProjectController projectController;

    public DbImportTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
        this.addCellEditorListener(new CellEditorListener() {

            @Override
            public void editingStopped(ChangeEvent e) {
                DbImportTreeCellEditor.this.cancelCellEditing();
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                editingStopped(e);
            }
        });

    }

    @Override
    public Object getCellEditorValue() {
        DbImportTreeNode node = (DbImportTreeNode) tree.getSelectionPath().getLastPathComponent();
        return node.getUserObject();
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                boolean isSelected, boolean expanded, boolean leaf, int row) {
        if (value instanceof DbImportTreeNode) {
            value = ((DbImportTreeNode) value). getSimpleNodeName();
        }
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded,
                leaf, row);
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (tree.getSelectionPath() != null) {
            if (tree.getSelectionPath().getLastPathComponent() == tree.getModel().getRoot()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void cancelCellEditing() {
        if (!Util.isEmptyString(super.getCellEditorValue().toString()) && !insertableNodeExist()) {
            EditNodeAction action = projectController.getApplication().getActionManager().getAction(EditNodeAction.class);
            action.setActionName(super.getCellEditorValue().toString());
            action.actionPerformed(null);
        } else {
            DeleteNodeAction action = projectController.getApplication().getActionManager().getAction(DeleteNodeAction.class);
            action.actionPerformed(null);
        }
    }

    private boolean equalNodes(int i, DbImportTreeNode parent, DbImportTreeNode selectedElement) {
        return super.getCellEditorValue().toString().equals(((DbImportTreeNode) parent.getChildAt(i)).getSimpleNodeName()) &&
                selectedElement.getUserObject().getClass().equals(((DbImportTreeNode) parent.getChildAt(i)).getUserObject().getClass());
    }

    private boolean insertableNodeExist() {
        DbImportTreeNode selectedElement;
        if (tree.getSelectionPath() == null) {
            selectedElement = (DbImportTreeNode) tree.getModel().getRoot();
        } else {
            selectedElement = (DbImportTreeNode) tree.getSelectionPath().getLastPathComponent();
        }
        int childCount = selectedElement.getParent().getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            if (equalNodes(i, (DbImportTreeNode) selectedElement.getParent(), selectedElement)) {
                return true;
            }

        }
        return false;
    }

    public void setProjectController(ProjectController projectController) {
        this.projectController = projectController;
    }
}
