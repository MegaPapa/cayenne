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
import org.apache.cayenne.dbsync.reverse.dbimport.FilterContainer;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeColumn;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.PatternParam;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.dialog.db.load.CatalogPopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.modeler.dialog.db.load.DefaultPopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.IncludeTablePopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.RootPopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.SchemaPopUpMenu;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 4.1
 */
class ReverseEngineeringTreePanel extends JScrollPane {

    private JTree reverseEngineeringTree;

    private ProjectController projectController;
    private TreeToolbarPanel treeToolbar;
    private Map<Class, DefaultPopUpMenu> popups;

    ReverseEngineeringTreePanel(ProjectController projectController, JTree reverseEngineeringTree) {
        super(reverseEngineeringTree);
        this.projectController = projectController;
        this.reverseEngineeringTree = reverseEngineeringTree;
        initListeners();
        initPopupMenus();
        changeIcons();
    }

    void updateTree() {
        translateReverseEngineeringToTree(getReverseEngineeringBySelectedMap());
    }

    private void initPopupMenus() {
        popups = new HashMap<>();
        popups.put(Catalog.class, new CatalogPopUpMenu());
        popups.put(Schema.class, new SchemaPopUpMenu());
        popups.put(ReverseEngineering.class, new RootPopUpMenu());
        popups.put(IncludeTable.class, new IncludeTablePopUpMenu());
        popups.put(ExcludeTable.class, new DefaultPopUpMenu());
        popups.put(IncludeColumn.class, new DefaultPopUpMenu());
        popups.put(ExcludeColumn.class, new DefaultPopUpMenu());
        popups.put(IncludeProcedure.class, new DefaultPopUpMenu());
        popups.put(ExcludeProcedure.class, new DefaultPopUpMenu());
    }

    private void initListeners() {
        reverseEngineeringTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                treeToolbar.lockButtons();
            }
        });
        reverseEngineeringTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = reverseEngineeringTree.getClosestRowForLocation(e.getX(), e.getY());
                    reverseEngineeringTree.setSelectionRow(row);
                    DbImportTreeNode selectedElement
                            = (DbImportTreeNode) reverseEngineeringTree.getSelectionPath().getLastPathComponent();
                    DefaultPopUpMenu popupMenu = popups.get(selectedElement.getUserObject().getClass());
                    if (popupMenu != null) {
                        popupMenu.setProjectController(projectController);
                        popupMenu.setSelectedElement(selectedElement);
                        popupMenu.setParentElement((DbImportTreeNode) selectedElement.getParent());
                        popupMenu.setTree(reverseEngineeringTree);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private ReverseEngineering getReverseEngineeringBySelectedMap() {
        DataMap dataMap = projectController.getCurrentDataMap();
        return projectController.getApplication().getMetaData().get(dataMap, ReverseEngineering.class);
    }

    private void translateReverseEngineeringToTree(ReverseEngineering reverseEngineering) {
        DefaultTreeModel model = (DefaultTreeModel)reverseEngineeringTree.getModel();
        DbImportTreeNode root = (DbImportTreeNode) model.getRoot();
        root.removeAllChildren();
        root.setUserObject(reverseEngineering);
        printCatalogs(reverseEngineering.getCatalogs(), root);
        printSchemas(reverseEngineering.getSchemas(), root);
        printIncludeTables(reverseEngineering.getIncludeTables(), root);
        printParams(reverseEngineering.getExcludeTables(), root);
        printParams(reverseEngineering.getIncludeColumns(), root);
        printParams(reverseEngineering.getExcludeColumns(), root);
        printParams(reverseEngineering.getIncludeProcedures(), root);
        printParams(reverseEngineering.getExcludeProcedures(), root);
        model.reload();
    }

    private <T extends PatternParam> void printParams(Collection<T> collection, DbImportTreeNode parent) {
        for (T element : collection) {
            parent.add(new DbImportTreeNode(element));
        }
    }

    private void printIncludeTables(Collection<IncludeTable> collection, DbImportTreeNode parent) {
        for (IncludeTable includeTable : collection) {
            DbImportTreeNode node = new DbImportTreeNode(includeTable);
            printParams(includeTable.getIncludeColumns(), node);
            printParams(includeTable.getExcludeColumns(), node);
            parent.add(node);
        }
    }

    private void printChildren(FilterContainer container, DbImportTreeNode parent) {
        printIncludeTables(container.getIncludeTables(), parent);
        printParams(container.getExcludeTables(), parent);
        printParams(container.getIncludeColumns(), parent);
        printParams(container.getExcludeColumns(), parent);
        printParams(container.getIncludeProcedures(), parent);
        printParams(container.getExcludeProcedures(), parent);
    }

    private void printSchemas(Collection<Schema> schemas, DbImportTreeNode parent) {
        for (Schema schema : schemas) {
            DbImportTreeNode node = new DbImportTreeNode(schema);
            printChildren(schema, node);
            parent.add(node);
        }
    }

    private void printCatalogs(Collection<Catalog> catalogs, DbImportTreeNode parent) {
        for (Catalog catalog : catalogs) {
            DbImportTreeNode node = new DbImportTreeNode(catalog);
            printSchemas(catalog.getSchemas(), node);
            printChildren(catalog, node);
            parent.add(node);
        }
    }

    private void changeIcons() {
        // Deleting tree icons
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) reverseEngineeringTree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
    }

    void setTreeToolbar(TreeToolbarPanel treeToolbar) {
        this.treeToolbar = treeToolbar;
    }
}
