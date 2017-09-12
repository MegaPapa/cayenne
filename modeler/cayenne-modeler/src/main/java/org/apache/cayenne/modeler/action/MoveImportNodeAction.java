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

package org.apache.cayenne.modeler.action;

import org.apache.cayenne.modeler.Application;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.modeler.editor.DraggableTreePanel;
import org.apache.cayenne.modeler.util.CayenneAction;

import javax.swing.JTree;
import java.awt.event.ActionEvent;

/**
 * @since 4.1
 */
public class MoveImportNodeAction extends CayenneAction {

    private static final String ICON_NAME = "icon-backward.png";
    private static final String ACTION_NAME = "Move Node";

    private JTree sourceTree;
    private JTree targetTree;
    private DraggableTreePanel panel;

    public MoveImportNodeAction(Application application) {
        super(ACTION_NAME, application);
    }

    public String getIconName() {
        return ICON_NAME;
    }

    @Override
    public void performAction(ActionEvent e) {
        if (sourceTree.getSelectionPath() != null) {
            DbImportTreeNode selectedElement = (DbImportTreeNode) sourceTree.getSelectionPath().getLastPathComponent();
            TreeManipulationAction action = panel.getActionByNodeType(selectedElement.getUserObject().getClass());
            if (action != null) {
                action.setInsertableNodeName(selectedElement.getSimpleNodeName());
                action.setTree(targetTree);
                action.actionPerformed(e);
            }
            //panel.canBeMoved();
        }
    }

    public void setSourceTree(JTree sourceTree) {
        this.sourceTree = sourceTree;
    }

    public void setTargetTree(JTree targetTree) {
        this.targetTree = targetTree;
    }

    public void setPanel(DraggableTreePanel panel) {
        this.panel = panel;
    }
}
