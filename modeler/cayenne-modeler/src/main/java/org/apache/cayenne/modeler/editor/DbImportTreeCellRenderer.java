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
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.modeler.util.ModelerUtil;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 4.1
 */
public class DbImportTreeCellRenderer extends DefaultTreeCellRenderer {

    protected DbImportTreeNode node;
    private Map<Class, String> icons;

    public DbImportTreeCellRenderer() {
        super();
        initIcons();
    }

    private void initIcons() {
        icons = new HashMap<>();
        icons.put(ReverseEngineering.class, "icon-copy.png");
        icons.put(Catalog.class, "icon-copy.png");
        icons.put(Schema.class, "icon-save-as-image.png");
        icons.put(IncludeTable.class, "icon-dbentity.png");
        icons.put(ExcludeTable.class, "icon-dbentity.png");
        icons.put(IncludeColumn.class, "icon-attribute.png");
        icons.put(ExcludeColumn.class, "icon-attribute.png");
        icons.put(IncludeProcedure.class, "icon-stored-procedure.png");
        icons.put(ExcludeProcedure.class, "icon-stored-procedure.png");
    }

    private ImageIcon getIconByNodeType(Class nodeClass) {
        String iconName = icons.get(nodeClass);
        return ModelerUtil.buildIcon(iconName);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        node = (DbImportTreeNode) value;
        if (icons.get(node.getUserObject().getClass()) != null) {
            setIcon(getIconByNodeType(node.getUserObject().getClass()));
        } else {
            setIcon(null);
        }
        return this;
    }
}
