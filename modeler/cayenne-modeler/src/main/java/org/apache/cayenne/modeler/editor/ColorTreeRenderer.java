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

/**
 * @since 4.1
 */
public class ColorTreeRenderer extends DbImportTreeCellRenderer {

    private static final Color ACCEPT_COLOR = new Color(60,179,113);

    private DbTreeColorMap colorMap;

    public ColorTreeRenderer() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);
        DbImportTreeNode parentNode = (DbImportTreeNode) node.getParent();
        String parentName = parentNode != null ? parentNode.getSimpleNodeName() : null;
        if (colorMap.existNode(parentName, node.getSimpleNodeName(), node.getUserObject().getClass())) {
            setForeground(ACCEPT_COLOR);
        } else {
            setForeground(Color.BLACK);
        }
        return this;
    }


    public void setColorMap(DbTreeColorMap colorMap) {
        this.colorMap = colorMap;
    }
}
