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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * @since 4.1
 */
public class DbImportModel extends DefaultTreeModel {

    private DbImportTree dbSchemaTree;

    public DbImportModel(TreeNode root) {
        super(root);
    }

    public void reload(TreeNode node) {
        super.reload(node);
        dbSchemaTree.repaint();
    }

    public void setDbSchemaTree(DbImportTree dbSchemaTree) {
        this.dbSchemaTree = dbSchemaTree;
    }
}
