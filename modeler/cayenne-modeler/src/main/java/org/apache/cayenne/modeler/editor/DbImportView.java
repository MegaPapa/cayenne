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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
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
import org.apache.cayenne.modeler.action.AddCatalogAction;
import org.apache.cayenne.modeler.action.AddExcludeColumnAction;
import org.apache.cayenne.modeler.action.AddExcludeProcedureAction;
import org.apache.cayenne.modeler.action.AddExcludeTableAction;
import org.apache.cayenne.modeler.action.AddIncludeColumnAction;
import org.apache.cayenne.modeler.action.AddIncludeProcedureAction;
import org.apache.cayenne.modeler.action.AddIncludeTableAction;
import org.apache.cayenne.modeler.action.AddPatternParamAction;
import org.apache.cayenne.modeler.action.AddSchemaAction;
import org.apache.cayenne.modeler.action.DeleteNodeAction;
import org.apache.cayenne.modeler.action.EditNodeAction;
import org.apache.cayenne.modeler.action.TreeManipulationAction;
import org.apache.cayenne.modeler.dialog.db.load.CatalogPopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.modeler.dialog.db.load.DefaultPopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.IncludeTablePopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.RootPopUpMenu;
import org.apache.cayenne.modeler.dialog.db.load.SchemaPopUpMenu;
import org.apache.cayenne.modeler.event.DataMapDisplayEvent;
import org.apache.cayenne.modeler.event.DataMapDisplayListener;
import org.apache.cayenne.modeler.util.NameGeneratorPreferences;
import org.apache.cayenne.modeler.util.TextAdapter;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @since 4.1
 */
public class DbImportView extends JPanel {

    private static final String MAIN_LAYOUT = "fill:350dlu, 5dlu, fill:350dlu";
    private static final String DATA_FIELDS_LAYOUT = "right:pref, 3dlu, fill:235dlu";
    private static final int DEFAULT_LEVEL = -1;
    private static final int FIRST_LEVEL = 0;
    private static final int SECOND_LEVEL = 1;
    private static final int THIRD_LEVEL = 5;
    private static final int FOURTH_LEVEL = 7;
    private static final int ALL_LINE_SPAN = 2;

    private JComboBox<String> strategyCombo;
    private JTree includeTables;
    private JScrollPane scrollPane;
    private TextAdapter meaningfulPk;
    private TextAdapter stripFromTableNames;
    private TextAdapter defaultPackage;
    private JCheckBox skipRelationshipsLoading;
    private JCheckBox skipPrimaryKeyLoading;
    private JCheckBox forceDataMapCatalog;
    private JCheckBox forceDataMapSchema;
    private JCheckBox usePrimitives;
    private JCheckBox useJava7Types;
    private JPanel dataPanel;
    private JToolBar treeToolBar;
    private JButton schemaButton;
    private JButton catalogButton;
    private JButton includeTableButton;
    private JButton excludeTableButton;
    private JButton includeColumnButton;
    private JButton excludeColumnButton;
    private JButton includeProcedureButton;
    private JButton excludeProcedureButton;
    private JButton editButton;
    private JButton deleteButton;

    private ProjectController projectController;
    private Map<Class, DefaultPopUpMenu> popups;
    private Map<Class, Integer> levels;
    private JButton[] buttons;


    public DbImportView(ProjectController projectController) {
        this.projectController = projectController;
        initFormElements();
        initListeners();
        initPopupMenus();
        initLevels();
    }

    private void changeToolbarButtonsState(boolean state) {
        schemaButton.setEnabled(state);
        catalogButton.setEnabled(state);
        includeTableButton.setEnabled(state);
        excludeTableButton.setEnabled(state);
        includeColumnButton.setEnabled(state);
        excludeColumnButton.setEnabled(state);
        includeProcedureButton.setEnabled(state);
        excludeProcedureButton.setEnabled(state);
        editButton.setEnabled(state);
        deleteButton.setEnabled(state);
    }

    private void initLevels() {
        levels = new HashMap<>();
        levels.put(ReverseEngineering.class, DEFAULT_LEVEL);
        levels.put(Catalog.class, FIRST_LEVEL);
        levels.put(Schema.class, SECOND_LEVEL);
        levels.put(IncludeTable.class, THIRD_LEVEL);
        levels.put(ExcludeTable.class, FOURTH_LEVEL);
        levels.put(IncludeColumn.class, FOURTH_LEVEL);
        levels.put(ExcludeColumn.class, FOURTH_LEVEL);
        levels.put(IncludeProcedure.class, FOURTH_LEVEL);
        levels.put(ExcludeProcedure.class, FOURTH_LEVEL);
    }

    private void lockButtonsOnLevel(int level) {
        for (int i = 0; i <= level; i++) {
            buttons[i].setEnabled(false);
        }
    }

    private void lockButtons(Object userObject) {
        changeToolbarButtonsState(true);
        if (levels.get(userObject.getClass()) == DEFAULT_LEVEL) {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        lockButtonsOnLevel(levels.get(userObject.getClass()));
    }

    private void initListeners() {
        projectController.addDataMapDisplayListener(new DataMapDisplayListener() {

            public void currentDataMapChanged(DataMapDisplayEvent e) {
                DataMap map = e.getDataMap();
                if (map != null) {
                    changeToolbarButtonsState(true);
                    ReverseEngineering reverseEngineering = DbImportView.this.projectController.getApplication().
                            getMetaData().get(map, ReverseEngineering.class);
                    if (reverseEngineering == null) {
                        reverseEngineering = new ReverseEngineering();
                        DbImportView.this.projectController.getApplication().getMetaData().add(map, reverseEngineering);
                    }
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    fillCheckboxes(reverseEngineering);
                    initializeTextFields(reverseEngineering);
                    translateReverseEngineeringToTree(reverseEngineering);
                }
            }
        });
        includeTables.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (includeTables.getLastSelectedPathComponent() != null) {
                    lockButtons(((DbImportTreeNode) includeTables.getLastSelectedPathComponent()).getUserObject());
                } else {
                    changeToolbarButtonsState(true);
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                }
            }
        });
        includeTables.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = includeTables.getClosestRowForLocation(e.getX(), e.getY());
                    includeTables.setSelectionRow(row);
                    DbImportTreeNode selectedElement
                            = (DbImportTreeNode) includeTables.getSelectionPath().getLastPathComponent();
                    DefaultPopUpMenu popupMenu = popups.get(selectedElement.getUserObject().getClass());
                    if (popupMenu != null) {
                        popupMenu.setProjectController(projectController);
                        popupMenu.setSelectedElement(selectedElement);
                        popupMenu.setParentElement((DbImportTreeNode) selectedElement.getParent());
                        popupMenu.setTree(includeTables);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        skipRelationshipsLoading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setSkipRelationshipsLoading(skipRelationshipsLoading.isSelected());
            }
        });
        skipPrimaryKeyLoading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setSkipPrimaryKeyLoading(skipPrimaryKeyLoading.isSelected());
            }
        });
        forceDataMapCatalog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setForceDataMapCatalog(forceDataMapCatalog.isSelected());
            }
        });
        forceDataMapSchema.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setForceDataMapSchema(forceDataMapSchema.isSelected());
            }
        });
        usePrimitives.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setUsePrimitives(usePrimitives.isSelected());
            }
        });
        useJava7Types.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setUseJava7Types(useJava7Types.isSelected());
            }
        });
    }

    private ReverseEngineering getReverseEngineeringBySelectedMap() {
        DataMap dataMap = projectController.getCurrentDataMap();
        return projectController.getApplication().getMetaData().get(dataMap, ReverseEngineering.class);
    }

    private void initializeTextFields(ReverseEngineering reverseEngineering) {
        meaningfulPk.setText(reverseEngineering.getMeaningfulPkTables());
        stripFromTableNames.setText(reverseEngineering.getStripFromTableNames());
        defaultPackage.setText(reverseEngineering.getDefaultPackage());
    }

    private void translateReverseEngineeringToTree(ReverseEngineering reverseEngineering) {
        DefaultTreeModel model = (DefaultTreeModel)includeTables.getModel();
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

    private void fillCheckboxes(ReverseEngineering reverseEngineering) {
        skipRelationshipsLoading.setSelected(reverseEngineering.getSkipRelationshipsLoading());
        skipPrimaryKeyLoading.setSelected(reverseEngineering.getSkipPrimaryKeyLoading());
        forceDataMapCatalog.setSelected(reverseEngineering.isForceDataMapCatalog());
        forceDataMapSchema.setSelected(reverseEngineering.isForceDataMapSchema());
        usePrimitives.setSelected(reverseEngineering.isUsePrimitives());
        useJava7Types.setSelected(reverseEngineering.isUseJava7Types());
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

    private void initStrategy() {
        Vector<String> arr = NameGeneratorPreferences
                .getInstance()
                .getLastUsedStrategies();
        strategyCombo.setModel(new DefaultComboBoxModel<>(arr));
    }

    private <T extends TreeManipulationAction> JButton createButton(Class<T> actionClass, int position) {
        TreeManipulationAction action = projectController.getApplication().getActionManager().getAction(actionClass);
        action.setTree(includeTables);
        return action.buildButton(position);
    }

    private <T extends AddPatternParamAction> JButton createButton(Class<T> actionClass, int position, Class paramClass) {
        AddPatternParamAction action = projectController.getApplication().getActionManager().getAction(actionClass);
        action.setTree(includeTables);
        action.setParamClass(paramClass);
        return action.buildButton(position);
    }

    private void initFormElements() {
        strategyCombo = new JComboBox<>();
        includeTables = new JTree(new DbImportTreeNode());
        // Deleting tree icons
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) includeTables.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        scrollPane = new JScrollPane(includeTables);
        meaningfulPk = new TextAdapter(new JTextField()) {
            protected void updateModel(String text) {
                getReverseEngineeringBySelectedMap().setMeaningfulPkTables(text);
            }
        };
        meaningfulPk.getComponent().setToolTipText("<html>Regular expression to filter tables with meaningful primary keys.<br>" +
                "Multiple expressions divided by comma can be used.<br>" +
                "Example: <b>^table1|^table2|^prefix.*|table_name</b></html>");
        stripFromTableNames = new TextAdapter(new JTextField()) {
            protected void updateModel(String text) {
                getReverseEngineeringBySelectedMap().setStripFromTableNames(text);
            }
        };
        stripFromTableNames.getComponent().setToolTipText("<html>Regex that matches the part of the table name that needs to be stripped off " +
                "when generating ObjEntity name</html>");

        defaultPackage = new TextAdapter(new JTextField()) {
            protected void updateModel(String text) {
                getReverseEngineeringBySelectedMap().setDefaultPackage(text);
            }
        };
        defaultPackage.getComponent().setToolTipText("<html>A Java package that will be set as the imported DataMap default and a package " +
                "of all the persistent Java classes. This is a required attribute if the \"map\"<br> itself does not " +
                "already contain a default package, as otherwise all the persistent classes will be mapped with no " +
                "package, and will not compile.</html>");
        skipRelationshipsLoading = new JCheckBox();
        skipPrimaryKeyLoading = new JCheckBox();
        forceDataMapCatalog = new JCheckBox();
        forceDataMapSchema = new JCheckBox();
        useJava7Types = new JCheckBox();
        usePrimitives = new JCheckBox();
        treeToolBar = new JToolBar();
        treeToolBar.setBorderPainted(false);

        schemaButton = createButton(AddSchemaAction.class, 0);
        catalogButton = createButton(AddCatalogAction.class, 0);
        includeTableButton = createButton(AddIncludeTableAction.class, 1);
        excludeTableButton = createButton(AddExcludeTableAction.class, 2, ExcludeTable.class);
        includeColumnButton = createButton(AddIncludeColumnAction.class, 2, IncludeColumn.class);
        excludeColumnButton = createButton(AddExcludeColumnAction.class, 2, ExcludeColumn.class);
        includeProcedureButton = createButton(AddIncludeProcedureAction.class, 2, IncludeProcedure.class);
        excludeProcedureButton = createButton(AddExcludeProcedureAction.class, 3, ExcludeProcedure.class);
        editButton = createButton(EditNodeAction.class, 0);
        deleteButton = createButton(DeleteNodeAction.class, 0);

        buttons = new JButton[]{catalogButton, schemaButton, includeTableButton, excludeTableButton,
                includeProcedureButton, excludeProcedureButton ,includeColumnButton, excludeColumnButton};

        treeToolBar.setFloatable(false);
        treeToolBar.add(catalogButton);
        treeToolBar.add(schemaButton);
        treeToolBar.addSeparator();
        treeToolBar.add(includeTableButton);
        treeToolBar.add(excludeTableButton);
        treeToolBar.add(includeColumnButton);
        treeToolBar.add(excludeColumnButton);
        treeToolBar.add(includeProcedureButton);
        treeToolBar.add(excludeProcedureButton);
        treeToolBar.add(editButton);
        treeToolBar.addSeparator();
        treeToolBar.add(deleteButton);
        initStrategy();

        FormLayout panelLayout = new FormLayout(DATA_FIELDS_LAYOUT);
        DefaultFormBuilder panelBuilder = new DefaultFormBuilder(panelLayout);
        panelBuilder.setDefaultDialogBorder();
        dataPanel = new JPanel();
        panelBuilder.append("Naming Strategy:", strategyCombo);
        panelBuilder.append("Tables with Meaningful PK Pattern:", meaningfulPk.getComponent());
        panelBuilder.append("Strip from table names:", stripFromTableNames.getComponent());
        panelBuilder.append("Default package:", defaultPackage.getComponent());
        panelBuilder.append("Skip relationships loading:", skipRelationshipsLoading);
        panelBuilder.append("Skip primary key loading:", skipPrimaryKeyLoading);
        panelBuilder.append("Force datamap catalog:", forceDataMapCatalog);
        panelBuilder.append("Force datamap schema:", forceDataMapSchema);
        panelBuilder.append("Use Java primitive types:", usePrimitives);
        panelBuilder.append("Use old java.util.Data type:", useJava7Types);
        dataPanel.add(panelBuilder.getPanel());

        FormLayout layout = new FormLayout(MAIN_LAYOUT);
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        builder.appendSeparator("Database Import Configuration");
        builder.append(treeToolBar, ALL_LINE_SPAN);
        builder.append(scrollPane);
        builder.append(dataPanel);
        this.setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
    }

}
