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
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;

import javax.swing.JTree;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

/**
 * @since 4.1
 */
public class ColorTreeRenderer extends DbImportTreeCellRenderer {

    private static final Color ACCEPT_COLOR = new Color(60,179,113);
    private static final Color EXCLUDE_COLOR = new Color(178, 0, 0);
    private static final Color NON_INCLUDE_COLOR = Color.LIGHT_GRAY;
    private static final int EXCLUDE_TABLE_RATE = -10000;

    private DbImportTree reverseEngineeringTree;
    private boolean existFirstLevelIncludeTable;
    private boolean existCatalogsOrSchemas;
    private boolean flag;

    public ColorTreeRenderer() {
        super();
    }

    private ArrayList<DbImportTreeNode> getParents(DbImportTreeNode node) {
        ArrayList<DbImportTreeNode> parents = new ArrayList<>();
        DbImportTreeNode tmpNode = node;
        while (tmpNode.getParent() != null) {
            parents.add((DbImportTreeNode) tmpNode.getParent());
            tmpNode = (DbImportTreeNode) tmpNode.getParent();
        }
        return parents;
    }

    // Compare parent chains
    private boolean parentsIsEqual(DbImportTreeNode reverseEngineeringNode) {
        ArrayList<DbImportTreeNode> reverseEngineeringNodeParents = getParents(reverseEngineeringNode);
        ArrayList<DbImportTreeNode> dbNodeParents = getParents(node);
        for (DbImportTreeNode node : reverseEngineeringNodeParents) {
            int deleteIndex = -1;
            for (int i = 0; i < dbNodeParents.size(); i++) {
                if (node.getSimpleNodeName().equals(dbNodeParents.get(i).getSimpleNodeName())) {
                    deleteIndex = i;
                    break;
                }
            }
            if (deleteIndex != -1) {
                dbNodeParents.remove(deleteIndex);
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean namesIsEqual(DbImportTreeNode reverseEngineeringNode) {
        if ((reverseEngineeringNode.getUserObject().getClass() == Schema.class)
            || (reverseEngineeringNode.getUserObject().getClass() == Catalog.class)) {
            return node.getSimpleNodeName().equals(reverseEngineeringNode.getSimpleNodeName());
        } else {
            return (node.getSimpleNodeName().matches(reverseEngineeringNode.getSimpleNodeName()));
        }
    }

    // Compare node with current rendered node
    private boolean nodesIsEqual(DbImportTreeNode reverseEngineeringNode) {
        if ((nodesClassesComparation(reverseEngineeringNode.getUserObject().getClass(), node.getUserObject().getClass()))
                && namesIsEqual(reverseEngineeringNode)
                && (node.getLevel() >= reverseEngineeringNode.getLevel())
                && (parentsIsEqual(reverseEngineeringNode))) {
            return true;
        }
        return false;
    }

    private boolean parentsIsEquals(DbImportTreeNode reverseEngineeringNode) {
        ArrayList<DbImportTreeNode> reverseEngineeringNodeParents = getParents(reverseEngineeringNode);
        ArrayList<DbImportTreeNode> dbNodeParents = getParents((DbImportTreeNode) node.getParent());
        for (DbImportTreeNode node : reverseEngineeringNodeParents) {
            int deleteIndex = -1;
            for (int i = 0; i < dbNodeParents.size(); i++) {
                if (node.getSimpleNodeName().equals(dbNodeParents.get(i).getSimpleNodeName())) {
                    deleteIndex = i;
                    break;
                }
            }
            if (deleteIndex != -1) {
                dbNodeParents.remove(deleteIndex);
            } else {
                return false;
            }
        }
        return true;
    }

    // Compare reverseEngineeringNode with node.getParent()
    private boolean compareWithParent(DbImportTreeNode reverseEngineeringNode) {
        if ((reverseEngineeringNode == null) || (node.getParent() == null)) {
            return false;
        }
        if ((((DbImportTreeNode)node.getParent()).getUserObject().getClass() == reverseEngineeringNode.getUserObject().getClass())
                && (((DbImportTreeNode)node.getParent()).getSimpleNodeName().equals(reverseEngineeringNode.getSimpleNodeName()))
                && (((DbImportTreeNode)node.getParent()).getLevel() >= reverseEngineeringNode.getLevel())
                && (parentsIsEquals(reverseEngineeringNode))) {
            return true;
        }
        return false;
    }

    // Get child include table count in node, if exists
    private int getChildIncludeTableCount(DbImportTreeNode parentNode) {
        if (parentNode.getUserObject().getClass() == IncludeTable.class) {
            return 1;
        }
        int childCount = parentNode.getChildCount();
        int result = 0;
        for (int i = 0; i < childCount; i++) {
            DbImportTreeNode tmpNode = (DbImportTreeNode) parentNode.getChildAt(i);
            if (tmpNode.getUserObject().getClass() == IncludeTable.class) {
                result++;
            }
        }
        return result;
    }

    private int getChildIncludeProcedureCount(DbImportTreeNode parentNode) {
        if (parentNode.getUserObject().getClass() == IncludeProcedure.class) {
            return 1;
        }
        int childCount = parentNode.getChildCount();
        int result = 0;
        for (int i = 0; i < childCount; i++) {
            DbImportTreeNode tmpNode = (DbImportTreeNode) parentNode.getChildAt(i);
            if (tmpNode.getUserObject().getClass() == IncludeProcedure.class) {
                result++;
            }
        }
        return result;
    }

    private int bypassTree(DbImportTreeNode rootNode) {
        int bypassResult = 0;
        int childCount = rootNode.getChildCount();
        boolean hasProcedures = false;

        // Case for empty reverse engineering, which has a include/exclude tables/procedures
        if ((childCount == 0) && (nodesIsEqual(rootNode))) {
            bypassResult++;
        }

        ReverseEngineering reverseEngineering = reverseEngineeringTree.getReverseEngineering();
        if ((reverseEngineering.getCatalogs().isEmpty()) && (reverseEngineering.getSchemas().isEmpty()) && (reverseEngineering.getIncludeTables().isEmpty()) && (node.getUserObject().getClass() != IncludeProcedure.class)) {
            bypassResult++;
        }

        if ((nodesIsEqual(rootNode) && (getChildIncludeTableCount(rootNode) == 0)) && (!existFirstLevelIncludeTable)) {
            flag = true;
            return 1;
        }

        if (flag) {
            for (int i = 0; i < childCount; i++) {
                DbImportTreeNode tmpNode = (DbImportTreeNode) rootNode.getChildAt(i);
                if ((node.getUserObject().getClass() == IncludeProcedure.class) && (nodesIsEqual(tmpNode))) {
                    int tmpNodeChildCount = tmpNode.getChildCount();
                    if (tmpNodeChildCount > 0) {
                        bypassResult += bypassTree((DbImportTreeNode) rootNode.getChildAt(i));
                    }
                    bypassResult++;
                    hasProcedures = true;
                }
            }
            if ((rootNode.getUserObject().getClass() != ExcludeTable.class) && (!nodesIsEqual(rootNode))
                    && (node.getUserObject().getClass() != IncludeProcedure.class)) {
                bypassResult++;
            } else {
                if ((!hasProcedures) && (node.getUserObject().getClass() != IncludeProcedure.class)) {
                    bypassResult += EXCLUDE_TABLE_RATE;
                }
            }
        }

        for (int i = 0; i < childCount; i++) {
            DbImportTreeNode tmpNode = (DbImportTreeNode) rootNode.getChildAt(i);
            if (tmpNode.getChildCount() > 0) {
                bypassResult += bypassTree(tmpNode);
                if ((tmpNode.getUserObject().getClass() == ExcludeTable.class)
                        || (tmpNode.getUserObject().getClass() == ExcludeProcedure.class)) {
                    bypassResult = EXCLUDE_TABLE_RATE;
                }
            } else if (compareWithParent(tmpNode) && !(existFirstLevelIncludeTable)) {
                if (node.getUserObject().getClass() != IncludeProcedure.class) {
                    bypassResult++;
                }
            }
            if (node.getParent() != null) {
                if (nodesIsEqual(tmpNode)) {
                    if ((tmpNode.getUserObject().getClass() == ExcludeTable.class)
                            || (tmpNode.getUserObject().getClass() == ExcludeProcedure.class)) {
                        bypassResult = EXCLUDE_TABLE_RATE;
                    }
                    bypassResult++;
                }
            }
        }
        return bypassResult;
    }

    private Color getColorByNodeType(DbImportTreeNode node) {
        if ((node.getUserObject().getClass() == ExcludeTable.class)
                || (node.getUserObject().getClass() == ExcludeProcedure.class)) {
            return EXCLUDE_COLOR;
        } else {
            return ACCEPT_COLOR;
        }
    }

    private void findFirstLevelIncludeTable() {
        DbImportTreeNode root = (DbImportTreeNode) reverseEngineeringTree.getModel().getRoot();
        int childCount = root.getChildCount();
        existFirstLevelIncludeTable = false;
        existCatalogsOrSchemas = false;
        for (int i = 0; i < childCount; i++) {
            DbImportTreeNode tmpNode = (DbImportTreeNode) root.getChildAt(i);
            if (tmpNode.getUserObject().getClass() == IncludeTable.class) {
                existFirstLevelIncludeTable = true;
            }
            if ((tmpNode.getUserObject().getClass() == Schema.class) || (tmpNode.getUserObject().getClass() == Catalog.class)) {
                existCatalogsOrSchemas = true;
            }
        }
    }

    private boolean reverseEngineeringHasNoTables(ReverseEngineering reverseEngineering) {
        return ((reverseEngineering.getCatalogs().size() == 0) && (reverseEngineering.getSchemas().size() == 0));
    }

    // Check, is DatabaseTree started with IncludeTable or IncludeProcedure
    private boolean isFirstNodeIsPrimitive(JTree tree) {
        final int firstChildIndex = 0;
        DbImportTreeNode root = (DbImportTreeNode) tree.getModel().getRoot();
        if (root.getChildCount() == 0) {
            return false;
        }
        DbImportTreeNode firstElement = (DbImportTreeNode) root.getChildAt(firstChildIndex);
        if ((firstElement.getUserObject().getClass() == IncludeTable.class)
                || (firstElement.getUserObject().getClass() == IncludeProcedure.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);
        if ((node.getUserObject().getClass() == Schema.class) || (node.getUserObject().getClass() == Catalog.class)
                || (isFirstNodeIsPrimitive(tree))) {
            flag = false;
        }
        if (selected) {
            setForeground(Color.WHITE);
            node.setColorized(true);
            return this;
        }
        DbImportTreeNode root;
        findFirstLevelIncludeTable();

        if (reverseEngineeringTree.getSelectionPath() != null) {
            root = (DbImportTreeNode) reverseEngineeringTree.getSelectionPath().getLastPathComponent();
        } else {
            root = (DbImportTreeNode) reverseEngineeringTree.getModel().getRoot();
        }
        ((DbImportTreeNode) tree.getModel().getRoot()).setColorized(true);

        if (bypassTree(root) > 0) {
            if (!existCatalogsOrSchemas) {
                setForeground(getColorByNodeType(root));
                node.setColorized(true);
                return this;
            }
            if ((node.getParent() != null) && (((DbImportTreeNode) node.getParent()).isColorized())) {
                setForeground(getColorByNodeType(root));
                node.setColorized(true);
                return this;
            }
        } else {
            setForeground(NON_INCLUDE_COLOR);
            node.setColorized(false);
            return this;
        }
        if (((node.getParent() != null) && (((DbImportTreeNode) node.getParent()).isColorized()))
                || (reverseEngineeringTree.getSelectionPath() != null)) {
            setForeground(getColorByNodeType(root));
            node.setColorized(true);
            return this;
        } else {
            setForeground(NON_INCLUDE_COLOR);
            node.setColorized(false);
            return this;
        }

    }

    private boolean nodesClassesComparation(Class firstClass, Class secondClass) {
        if (firstClass.equals(secondClass)) {
            return true;
        }
        if ((firstClass.equals(IncludeTable.class)) && (secondClass.equals(ExcludeTable.class))) {
            return true;
        }
        if ((firstClass.equals(ExcludeTable.class)) && (secondClass.equals(IncludeTable.class))) {
            return true;
        }
        if ((firstClass.equals(IncludeProcedure.class)) && (secondClass.equals(ExcludeProcedure.class))) {
            return true;
        }
        if ((firstClass.equals(ExcludeProcedure.class)) && (secondClass.equals(IncludeProcedure.class))) {
            return true;
        }
        return false;
    }

    public void setReverseEngineeringTree(DbImportTree reverseEngineeringTree) {
        this.reverseEngineeringTree = reverseEngineeringTree;
    }
}
