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
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.action.AddCatalogAction;
import org.apache.cayenne.modeler.action.AddExcludeColumnAction;
import org.apache.cayenne.modeler.action.AddExcludeProcedureAction;
import org.apache.cayenne.modeler.action.AddExcludeTableAction;
import org.apache.cayenne.modeler.action.AddIncludeColumnAction;
import org.apache.cayenne.modeler.action.AddIncludeProcedureAction;
import org.apache.cayenne.modeler.action.AddIncludeTableAction;
import org.apache.cayenne.modeler.action.AddSchemaAction;
import org.apache.cayenne.modeler.action.MoveImportNodeAction;
import org.apache.cayenne.modeler.action.MoveInvertNodeAction;
import org.apache.cayenne.modeler.action.TreeManipulationAction;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.modeler.dialog.db.load.TransferableNode;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 4.1
 */
public class DraggableTreePanel extends JScrollPane {

    private static final int UNACCEPTABLE_DIFFERENCE = 2;
    private static final int ROOT_LEVEL = 14;
    private static final int FIRST_LEVEL = 11;
    private static final int SECOND_LEVEL = 8;
    private static final int THIRD_LEVEL = 5;
    private static final int FOURTH_LEVEL = 2;
    private static final int FIFTH_LEVEL = 3;

    private DbImportTree sourceTree;
    private DbImportTree targetTree;
    private JButton moveButton;
    private JButton moveInvertButton;

    private ProjectController projectController;
    private Map<Class, Integer> levels;
    private Map<Class, Class> actions;

    public DraggableTreePanel(ProjectController projectController, DbImportTree sourceTree, DbImportTree targetTree) {
        super(sourceTree);
        this.targetTree = targetTree;
        this.sourceTree = sourceTree;
        this.projectController = projectController;
        initLevels();
        initElement();
        initActions();
        initListeners();
    }

    private void initActions() {
        actions = new HashMap<>();
        actions.put(Catalog.class, AddCatalogAction.class);
        actions.put(Schema.class, AddSchemaAction.class);
        actions.put(IncludeTable.class, AddIncludeTableAction.class);
        actions.put(ExcludeTable.class, AddExcludeTableAction.class);
        actions.put(IncludeColumn.class, AddIncludeColumnAction.class);
        actions.put(ExcludeColumn.class, AddExcludeColumnAction.class);
        actions.put(IncludeProcedure.class, AddIncludeProcedureAction.class);
        actions.put(ExcludeProcedure.class, AddExcludeProcedureAction.class);
    }

    private void initListeners() {
        targetTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (canBeMoved()) {
                    moveButton.setEnabled(true);
                    if (canBeInverted()) {
                        moveInvertButton.setEnabled(true);
                    } else {
                        moveInvertButton.setEnabled(false);
                    }
                } else {
                    moveButton.setEnabled(false);
                    moveInvertButton.setEnabled(false);
                }
            }
        });

        targetTree.setDragEnabled(true);
        targetTree.setTransferHandler(new TransferHandler() {

            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                if (!support.isDrop()) {
                    return false;
                }
                JTree.DropLocation dropLocation =
                        (JTree.DropLocation)support.getDropLocation();
                return dropLocation.getPath() != null;
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                if (!canBeMoved()) {
                    return false;
                }
                Transferable transferable = support.getTransferable();
                Object transferData = null;
                try {
                    for (DataFlavor dataFlavor : transferable.getTransferDataFlavors()) {
                        transferData = transferable.getTransferData(dataFlavor);
                        if (transferData != null) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    return false;
                } catch (UnsupportedFlavorException e) {
                    return false;
                }
                if (transferData != null) {
                    TreeManipulationAction action = (TreeManipulationAction) projectController.getApplication().
                            getActionManager().getAction(actions.get(transferData.getClass()));
                    action.setInsertableNodeName(((TransferableNode) sourceTree.
                            getSelectionPath().getLastPathComponent()).getSimpleNodeName()
                    );
                    action.setTree(DraggableTreePanel.this.targetTree);
                    action.performAction(null);
                    return true;
                }
                return false;
            }
        });
    }

    private boolean canBeInverted() {
        if (sourceTree.getSelectionPath() != null) {
            DbImportTreeNode selectedElement = (DbImportTreeNode) sourceTree.getSelectionPath().getLastPathComponent();
            if (selectedElement == null) {
                return false;
            }
            if (levels.get(selectedElement.getUserObject().getClass()) < SECOND_LEVEL) {
                return true;
            }
        }
        return false;
    }

    private void initElement() {
        sourceTree.setDragEnabled(true);
        sourceTree.setDropMode(DropMode.INSERT);
        sourceTree.setTransferHandler(new TransferHandler() {

            @Override
            public int getSourceActions(JComponent c) {
                return COPY_OR_MOVE;
            }

            @Override
            public Transferable createTransferable(JComponent c) {
                JTree tree = (JTree) c;
                return (Transferable) tree.getSelectionPath().getLastPathComponent();
            }
        });
        sourceTree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (sourceTree.getLastSelectedPathComponent() != null) {
                    if (canBeMoved()) {
                        moveButton.setEnabled(true);
                        if (canBeInverted()) {
                            moveInvertButton.setEnabled(true);
                        } else {
                            moveInvertButton.setEnabled(false);
                        }
                    } else {
                        moveInvertButton.setEnabled(false);
                        moveButton.setEnabled(false);
                    }
                }
            }
        });

        MoveImportNodeAction action = projectController.getApplication().
                getActionManager().getAction(MoveImportNodeAction.class);
        action.setPanel(this);
        action.setSourceTree(sourceTree);
        action.setTargetTree(targetTree);
        moveButton = action.buildButton();
        MoveInvertNodeAction actionInv = projectController.getApplication().
                getActionManager().getAction(MoveInvertNodeAction.class);
        actionInv.setPanel(this);
        actionInv.setSourceTree(sourceTree);
        actionInv.setTargetTree(targetTree);
        moveInvertButton = actionInv.buildButton();


        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) sourceTree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
    }

    private void initLevels() {
        levels = new HashMap<>();
        levels.put(ReverseEngineering.class, ROOT_LEVEL);
        levels.put(Catalog.class, FIRST_LEVEL);
        levels.put(Schema.class, SECOND_LEVEL);
        levels.put(IncludeTable.class, THIRD_LEVEL);
        levels.put(IncludeColumn.class, FOURTH_LEVEL);
        levels.put(ExcludeColumn.class, FOURTH_LEVEL);
        levels.put(ExcludeTable.class, FIFTH_LEVEL);
        levels.put(IncludeProcedure.class, FIFTH_LEVEL);
        levels.put(ExcludeProcedure.class, FIFTH_LEVEL);
    }

    private boolean canBeMoved() {
        if (sourceTree.getSelectionPath() != null) {
            DbImportTreeNode selectedElement = (DbImportTreeNode) sourceTree.getSelectionPath().getLastPathComponent();
            if (selectedElement == null) {
                return false;
            }
            int targetLevel;
            int sourceLevel;
            sourceLevel = levels.get(selectedElement.getUserObject().getClass());
            if (targetTree.getSelectionPath() != null) {
                selectedElement = (DbImportTreeNode) targetTree.getSelectionPath().getLastPathComponent();
                if (selectedElement == null) {
                    return false;
                }
                targetLevel = levels.get(selectedElement.getUserObject().getClass());
            } else {
                targetLevel = ROOT_LEVEL;
            }
            return (targetLevel - sourceLevel) > UNACCEPTABLE_DIFFERENCE;
        }
        return false;
    }

    public JButton getMoveButton() {
        return moveButton;
    }

    public JButton getMoveInvertButton() {
        return moveInvertButton;
    }

    public TreeManipulationAction getActionByNodeType(Class nodeType) {
        Class actionClass = actions.get(nodeType);
        if (actionClass != null) {
            TreeManipulationAction action = (TreeManipulationAction) projectController.getApplication().
                    getActionManager().getAction(actionClass);
            return action;
        }
        return null;
    }

    public DbImportTree getSourceTree() {
        return sourceTree;
    }
}
