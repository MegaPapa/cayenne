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

import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.util.regex.Pattern;

/**
 * @since 4.1
 */
public class ColorTreeRenderer extends DbImportTreeCellRenderer {

    private static final Color ACCEPT_COLOR = new Color(60,179,113);
    private static final Color EXCLUDE_COLOR = new Color(178, 0, 0);
    private static final Color NON_INCLUDE_COLOR = Color.LIGHT_GRAY;

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
        String parentName = parentNode != null ? parentNode.getSimpleNodeName() : "";
        TreePath path = colorMap.getReverseEngineeringTree().getSelectionPath();
        if (selected) {
            setForeground(Color.WHITE);
            return this;
        }
        if (colorMap.existNode(parentName, node.getSimpleNodeName(), node.getUserObject().getClass(), true)) {
            if (path == null) {
                setForeground(ACCEPT_COLOR);
                if (parentNode != null) {
                    parentNode.setColorized(true);
                }
                System.out.println("1");
                node.setColorized(true);
                return this;
            } else {
                colorizeSelected(parentNode, parentName, ACCEPT_COLOR, IncludeTable.class);
                return this;
            }
        }
        // ADD LOWERCASE CHECKING
        if (colorMap.existNode(parentName, node.getSimpleNodeName(), node.getUserObject().getClass(), false)) {
            if (path == null) {
                setForeground(NON_INCLUDE_COLOR);
                if (parentNode != null) {
                    parentNode.setColorized(true);
                }
                node.setColorized(true);
                return this;
            } else {
                DbImportTreeNode reverseEngineeringNode = (DbImportTreeNode) path.getLastPathComponent();
                DbImportTreeNode reverseEngineeringParentNode = (DbImportTreeNode) reverseEngineeringNode.getParent();
                String reverseEngineeringParentName = "";
                String reverseEngineeringName = "";
                if (reverseEngineeringParentNode != null) {
                    reverseEngineeringParentName = reverseEngineeringParentNode.getSimpleNodeName() == null ? "" : reverseEngineeringParentNode.getSimpleNodeName();
                }
                if (reverseEngineeringNode.getSimpleNodeName() != null) {
                    reverseEngineeringName = reverseEngineeringNode.getSimpleNodeName();
                }
                if ((!parentNode.isColorized()) && (node.getSimpleNodeName().matches(reverseEngineeringName.toLowerCase()))) {
                    if (reverseEngineeringNode.getUserObject().getClass() == IncludeTable.class) {
                        if (reverseEngineeringParentName.equals(parentName)) {
                            setForeground(ACCEPT_COLOR);
                            return this;
                        }
                    } else {
                        if (nodesClassesComparation(node.getUserObject().getClass(), reverseEngineeringNode.getUserObject().getClass())) {
                            setForeground(EXCLUDE_COLOR);
                            return this;
                        }
                    }
                }
            }
        } else {
            if (path != null) {
                DbImportTreeNode reverseEngineeringNode = (DbImportTreeNode) path.getLastPathComponent();
                if (colorMap.isEmpty()) {
                    if ((node.getUserObject().getClass() == IncludeTable.class)
                        && ((reverseEngineeringNode.getUserObject().getClass() == IncludeTable.class)
                            || (reverseEngineeringNode.getUserObject().getClass() == ExcludeTable.class))) {
                        if (customMatches(node.getSimpleNodeName(), reverseEngineeringNode.getSimpleNodeName().toLowerCase())) {
                            setForeground(EXCLUDE_COLOR);
                            return this;
                        }
                    }
                }
            } else if (colorMap.isEmpty()) {
                if (colorMap.isExcludedNode(node.getSimpleNodeName()) && (node.getUserObject().getClass() == IncludeTable.class)) {
                    setForeground(NON_INCLUDE_COLOR);
                    return this;
                } else {
                    setForeground(ACCEPT_COLOR);
                    return this;
                }

            }
            // Other
            setForeground(NON_INCLUDE_COLOR);
            System.out.println(parentName + " 8");
            node.setColorized(false);
            return this;
        }
        setForeground(NON_INCLUDE_COLOR); // MAYBE ACCEPT_COLOR???
        System.out.println(parentName + " 7");
        node.setColorized(false);
        return this;
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
        return false;
    }

    private boolean customMatches(String line, String regex) {
        if ((regex == null) || (line == null)) {
            return true;
        }
        return Pattern.matches(regex, line);
    }

    // COLORIZE ACCEPT
    private void colorizeSelected(DbImportTreeNode parentNode, String parentName, Color color, Class clazz) {

        DbImportTreeNode reverseEngineeringNode = (DbImportTreeNode) colorMap.getReverseEngineeringTree().
                getSelectionPath().getLastPathComponent();

        DbImportTreeNode reverseEngineeringParentNode = (DbImportTreeNode) reverseEngineeringNode.getParent();
        if (reverseEngineeringNode.getSimpleNodeName() != null) {
            if (node.getSimpleNodeName().toLowerCase().matches(reverseEngineeringNode.getSimpleNodeName().toLowerCase())) {
                if (reverseEngineeringNode.getUserObject().getClass().equals(node.getUserObject().getClass())) {
                    if (!parentNode.getUserObject().getClass().equals(ReverseEngineering.class)) {      //       ETO VOOBSHE NUZHNO????!!
                        if ((parentName.equals(reverseEngineeringParentNode.getSimpleNodeName()))) {
                            setForeground(color);
                            System.out.println("2");
                            node.setColorized(true);
                            return;
                        }
                    } else {
                        setForeground(color);
                        System.out.println("aaa");
                        node.setColorized(true);
                        return;
                    }
                }
            }
            if (parentNode.isColorized()) {
                setForeground(color);
                node.setColorized(true);
                System.out.println("3");
                return;
            } else {
                if (node.getSimpleNodeName().toLowerCase().matches(reverseEngineeringNode.getSimpleNodeName().toLowerCase())) { // ADD CHECK ON THIS IN NOT SCHEMA OR CATALOG
                    if ((node.getUserObject().getClass().equals(clazz))) {

                        if (parentName.equals(reverseEngineeringParentNode.getSimpleNodeName())) {
                            setForeground(color);
                            System.out.println("2.5");
                            node.setColorized(true);
                            return;
                        } else {
                            setForeground(NON_INCLUDE_COLOR);
                            node.setColorized(false);
                            System.out.println(parentName + " qqq");
                        }

                        if (reverseEngineeringParentNode.getUserObject().getClass().equals(ReverseEngineering.class)) {
                            if (node.getUserObject().getClass().equals(reverseEngineeringNode.getUserObject().getClass())) {
                                setForeground(color);
                                System.out.println("fff");
                                node.setColorized(true);
                                return;
                            }
                        }
                    } else {
                        setForeground(NON_INCLUDE_COLOR);
                        node.setColorized(false);
                        System.out.println(parentName + " 4.5");
                        return;
                    }
                } else {
                    setForeground(NON_INCLUDE_COLOR);
                    node.setColorized(false);
                    parentNode.setColorized(false);
                    System.out.println(parentName + " 4");
                    return;
                }
            }
        } else {
            setForeground(NON_INCLUDE_COLOR);
            node.setColorized(false);
            parentNode.setColorized(false);
            System.out.println(parentName + " 6");
            return;
        }
        return;
    }


    public void setColorMap(DbTreeColorMap colorMap) {
        this.colorMap = colorMap;
    }
}
