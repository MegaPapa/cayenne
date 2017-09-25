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
import org.apache.cayenne.modeler.action.EditNodeAction;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.util.Util;

import javax.swing.JTree;
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
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        return true;
    }

    @Override
    public void cancelCellEditing() {
        if (!Util.isEmptyString(super.getCellEditorValue().toString())) {
            EditNodeAction action = projectController.getApplication().getActionManager().getAction(EditNodeAction.class);
            action.setName(super.getCellEditorValue().toString());
            action.actionPerformed(null);
        }
    }

    public void setProjectController(ProjectController projectController) {
        this.projectController = projectController;
    }
}
