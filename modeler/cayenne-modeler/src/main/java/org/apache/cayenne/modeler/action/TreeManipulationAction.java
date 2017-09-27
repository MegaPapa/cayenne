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
import org.apache.cayenne.modeler.util.CayenneAction;
import org.apache.cayenne.util.Util;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

/**
 * @since 4.1
 */
public abstract class TreeManipulationAction extends CayenneAction {

    protected static final int INIT_ELEMENT = 0;

    protected JTree tree;
    protected DbImportTreeNode selectedElement;
    protected DbImportTreeNode parentElement;
    protected String insertableNodeName;
    protected Class insertableNodeClass;

    public TreeManipulationAction(String name, Application application) {
        super(name, application);
    }

    public void setTree(JTree tree) {
        this.tree = tree;
    }

    public JTree getTree() {
        return tree;
    }

    protected void updateModel() {
        insertableNodeName = null;
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        getProjectController().setDirty(true);
        model.reload(selectedElement);
    }

    protected void updateAfterInsert() {
        updateModel();
        tree.startEditingAtPath(new TreePath(((DbImportTreeNode) selectedElement.getLastChild()).getPath()));
    }

    public void setInsertableNodeName(String nodeName) {
        this.insertableNodeName = nodeName;
    }
}
