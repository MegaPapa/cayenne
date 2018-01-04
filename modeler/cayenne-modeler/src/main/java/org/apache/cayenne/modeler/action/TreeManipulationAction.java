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

import org.apache.cayenne.dbsync.reverse.dbimport.Catalog;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;
import org.apache.cayenne.modeler.Application;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.modeler.editor.DbImportModel;
import org.apache.cayenne.modeler.editor.DbImportTree;
import org.apache.cayenne.modeler.util.CayenneAction;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 4.1
 */
public abstract class TreeManipulationAction extends CayenneAction {

    protected DbImportTree tree;
    protected DbImportTreeNode selectedElement;
    protected DbImportTreeNode parentElement;
    protected String insertableNodeName;
    protected Class insertableNodeClass;
    protected boolean isMultipleAction;
    private boolean movedFromDbSchema;
    Map<Class, List<Class>> levels;

    public TreeManipulationAction(String name, Application application) {
        super(name, application);
        initLevels();
    }

    protected boolean reverseEngineeringIsEmpty() {
        ReverseEngineering reverseEngineering = tree.getReverseEngineering();
        return ((reverseEngineering.getCatalogs().size() == 0) && (reverseEngineering.getSchemas().size() == 0)
                && (reverseEngineering.getIncludeTables().size() == 0) && (reverseEngineering.getExcludeTables().size() == 0)
                && (reverseEngineering.getIncludeColumns().size() == 0) && (reverseEngineering.getExcludeColumns().size() == 0)
                && (reverseEngineering.getIncludeProcedures().size() == 0) && (reverseEngineering.getExcludeProcedures().size() == 0));
    }

    private void initLevels() {
        levels = new HashMap<>();

        List<Class> rootChilds = new ArrayList<>();
        rootChilds.add(Schema.class);
        rootChilds.add(IncludeTable.class);
        rootChilds.add(ExcludeTable.class);
        rootChilds.add(IncludeColumn.class);
        rootChilds.add(ExcludeColumn.class);
        rootChilds.add(IncludeProcedure.class);
        rootChilds.add(ExcludeProcedure.class);
        levels.put(ReverseEngineering.class, rootChilds);

        List<Class> catalogChilds = new ArrayList<>();
        catalogChilds.add(Schema.class);
        catalogChilds.add(IncludeTable.class);
        catalogChilds.add(ExcludeTable.class);
        catalogChilds.add(IncludeColumn.class);
        catalogChilds.add(ExcludeColumn.class);
        catalogChilds.add(IncludeProcedure.class);
        catalogChilds.add(ExcludeProcedure.class);
        levels.put(Catalog.class, catalogChilds);

        List<Class> schemaChilds = new ArrayList<>();
        schemaChilds.add(IncludeTable.class);
        schemaChilds.add(ExcludeTable.class);
        schemaChilds.add(IncludeColumn.class);
        schemaChilds.add(ExcludeColumn.class);
        schemaChilds.add(IncludeProcedure.class);
        schemaChilds.add(ExcludeProcedure.class);
        levels.put(Schema.class, schemaChilds);

        List<Class> includeTableChilds = new ArrayList<>();
        includeTableChilds.add(IncludeColumn.class);
        includeTableChilds.add(ExcludeColumn.class);
        levels.put(IncludeTable.class, includeTableChilds);
        levels.put(ExcludeTable.class, null);
        levels.put(IncludeColumn.class, null);
        levels.put(ExcludeColumn.class, null);
        levels.put(IncludeProcedure.class, null);
        levels.put(ExcludeProcedure.class, null);
    }

    public void setTree(DbImportTree tree) {
        this.tree = tree;
    }

    public JTree getTree() {
        return tree;
    }

    protected boolean canBeInserted() {
        Class selectedObjectClass = selectedElement.getUserObject().getClass();
        List<Class> childs = levels.get(selectedObjectClass);
        return childs != null && childs.contains(insertableNodeClass);
    }

    protected void updateModel(boolean updateSelected) {
        insertableNodeName = null;
        DbImportModel model = (DbImportModel) tree.getModel();
        getProjectController().setDirty(true);
        TreePath savedPath = null;
        if (!updateSelected) {
            savedPath = new TreePath(parentElement.getPath());
        }
        model.reload(updateSelected ? selectedElement : parentElement);
        if ((savedPath != null) && (parentElement.getUserObject().getClass() != ReverseEngineering.class)) {
            tree.setSelectionPath(savedPath);
        }
    }

    protected void updateAfterInsert(boolean updateSelected) {
        updateModel(updateSelected);
        if (!movedFromDbSchema) {
            if (updateSelected) {
                tree.startEditingAtPath(new TreePath(((DbImportTreeNode) selectedElement.getLastChild()).getPath()));
            } else {
                tree.startEditingAtPath(new TreePath(((DbImportTreeNode) parentElement.getLastChild()).getPath()));
            }
        }
        movedFromDbSchema = false;
        isMultipleAction = false;
    }

    public void setInsertableNodeName(String nodeName) {
        this.insertableNodeName = nodeName;
    }

    public void setMultipleAction(boolean multipleAction) {
        isMultipleAction = multipleAction;
    }

    public void setMovedFromDbSchema(boolean movedFromDbSchema) {
        this.movedFromDbSchema = movedFromDbSchema;
    }
}
