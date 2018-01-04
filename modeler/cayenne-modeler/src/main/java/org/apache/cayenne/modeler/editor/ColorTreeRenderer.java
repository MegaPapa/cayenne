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

import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;

import javax.swing.JTree;
import java.awt.Color;
import java.awt.Component;

import static org.apache.cayenne.modeler.editor.DbImportNodeHandler.LABEL_COLOR;
import static org.apache.cayenne.modeler.editor.DbImportNodeHandler.NON_INCLUDE_COLOR;

/**
 * @since 4.1
 */
public class ColorTreeRenderer extends DbImportTreeCellRenderer {

    private DbImportNodeHandler handler;
    private DbImportTree reverseEngineeringTree;

    public ColorTreeRenderer() {
        super();
        handler = new DbImportNodeHandler();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);
        handler.setDbSchemaNode(node);
        if (handler.isLabel(node)) {
            setForeground(LABEL_COLOR);
            return this;
        }
        if (handler.isContainer(node) || (handler.isFirstNodeIsPrimitive(tree))) {
            handler.setFlag(false);
        }
        if (selected) {
            setForeground(Color.WHITE);
            node.setColorized(true);
            return this;
        }
        DbImportTreeNode root;
        handler.findFirstLevelIncludeTable();

        if (reverseEngineeringTree.getSelectionPath() != null) {
            root = (DbImportTreeNode) reverseEngineeringTree.getSelectionPath().getLastPathComponent();
        } else {
            root = (DbImportTreeNode) reverseEngineeringTree.getModel().getRoot();
        }
        ((DbImportTreeNode) tree.getModel().getRoot()).setColorized(true);

        if (handler.bypassTree(root) > 0) {
            if (!handler.isExistCatalogsOrSchemas()) {
                setForeground(handler.getColorByNodeType(root));
                node.setColorized(true);
                return this;
            }
            if (handler.isParentIncluded()) {
                setForeground(handler.getColorByNodeType(root));
                node.setColorized(true);
                return this;
            }
        } else {
            setForeground(NON_INCLUDE_COLOR);
            node.setColorized(false);
            return this;
        }
        if ((handler.isParentIncluded()) || (reverseEngineeringTree.getSelectionPath() != null)) {
            setForeground(handler.getColorByNodeType(root));
            node.setColorized(true);
            return this;
        } else {
            setForeground(NON_INCLUDE_COLOR);
            node.setColorized(false);
            return this;
        }

    }

    public void setReverseEngineeringTree(DbImportTree reverseEngineeringTree) {
        this.reverseEngineeringTree = reverseEngineeringTree;
        handler.setReverseEngineeringTree(reverseEngineeringTree);
    }
}
