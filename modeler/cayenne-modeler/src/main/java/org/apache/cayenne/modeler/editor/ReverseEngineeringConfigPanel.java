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
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.util.NameGeneratorPreferences;
import org.apache.cayenne.modeler.util.TextAdapter;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @since 4.1
 */
public class ReverseEngineeringConfigPanel extends JPanel {

    private static final String DATA_FIELDS_LAYOUT = "right:pref, 3dlu, fill:235dlu";

    private JComboBox<String> strategyCombo;
    private TextAdapter meaningfulPk;
    private TextAdapter stripFromTableNames;
    private TextAdapter defaultPackage;
    private JCheckBox skipRelationshipsLoading;
    private JCheckBox skipPrimaryKeyLoading;
    private JCheckBox forceDataMapCatalog;
    private JCheckBox forceDataMapSchema;
    private JCheckBox usePrimitives;
    private JCheckBox useJava7Types;

    private ProjectController projectController;

    ReverseEngineeringConfigPanel(ProjectController projectController) {
        this.projectController = projectController;
        initFormElements();
        initListeners();
        buildView();
    }

    private void buildView() {
        FormLayout panelLayout = new FormLayout(DATA_FIELDS_LAYOUT);
        DefaultFormBuilder panelBuilder = new DefaultFormBuilder(panelLayout);
        panelBuilder.setDefaultDialogBorder();

        panelBuilder.append("Tables with Meaningful PK Pattern:", meaningfulPk.getComponent());
        panelBuilder.append("Strip from table names:", stripFromTableNames.getComponent());
        panelBuilder.append("Default package:", defaultPackage.getComponent());
        panelBuilder.append("Skip relationships loading:", skipRelationshipsLoading);
        panelBuilder.append("Skip primary key loading:", skipPrimaryKeyLoading);
        panelBuilder.append("Force datamap catalog:", forceDataMapCatalog);
        panelBuilder.append("Force datamap schema:", forceDataMapSchema);
        panelBuilder.append("Use Java primitive types:", usePrimitives);
        panelBuilder.append("Use java.util.Date type:", useJava7Types);
        panelBuilder.append(strategyCombo);

        add(panelBuilder.getPanel());
    }

    void fillCheckboxes(ReverseEngineering reverseEngineering) {
        skipRelationshipsLoading.setSelected(reverseEngineering.getSkipRelationshipsLoading());
        skipPrimaryKeyLoading.setSelected(reverseEngineering.getSkipPrimaryKeyLoading());
        forceDataMapCatalog.setSelected(reverseEngineering.isForceDataMapCatalog());
        forceDataMapSchema.setSelected(reverseEngineering.isForceDataMapSchema());
        usePrimitives.setSelected(reverseEngineering.isUsePrimitives());
        useJava7Types.setSelected(reverseEngineering.isUseJava7Types());
    }

    void initializeTextFields(ReverseEngineering reverseEngineering) {
        meaningfulPk.setText(reverseEngineering.getMeaningfulPkTables());
        stripFromTableNames.setText(reverseEngineering.getStripFromTableNames());
        defaultPackage.setText(reverseEngineering.getDefaultPackage());
    }

    private ReverseEngineering getReverseEngineeringBySelectedMap() {
        DataMap dataMap = projectController.getCurrentDataMap();
        return projectController.getApplication().getMetaData().get(dataMap, ReverseEngineering.class);
    }

    private void initStrategy() {
        Vector<String> arr = NameGeneratorPreferences
                .getInstance()
                .getLastUsedStrategies();
        strategyCombo.setModel(new DefaultComboBoxModel<>(arr));
    }

    private void initFormElements() {
        strategyCombo = new JComboBox<>();
        strategyCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setNamingStrategy(
                        (String) ReverseEngineeringConfigPanel.this.getStrategyCombo().getSelectedItem()
                );
                projectController.setDirty(true);
            }
        });
        strategyCombo.setVisible(false);
        meaningfulPk = new TextAdapter(new JTextField()) {
            protected void updateModel(String text) {
                getReverseEngineeringBySelectedMap().setMeaningfulPkTables(text);
                projectController.setDirty(true);
            }
        };
        meaningfulPk.getComponent().setToolTipText("<html>Regular expression to filter tables with meaningful primary keys.<br>" +
                "Multiple expressions divided by comma can be used.<br>" +
                "Example: <b>^table1|^table2|^prefix.*|table_name</b></html>");
        stripFromTableNames = new TextAdapter(new JTextField()) {
            protected void updateModel(String text) {
                getReverseEngineeringBySelectedMap().setStripFromTableNames(text);
                projectController.setDirty(true);
            }
        };
        stripFromTableNames.getComponent().setToolTipText("<html>Regex that matches the part of the table name that needs to be stripped off " +
                "when generating ObjEntity name</html>");

        defaultPackage = new TextAdapter(new JTextField()) {
            protected void updateModel(String text) {
                getReverseEngineeringBySelectedMap().setDefaultPackage(text);
                projectController.setDirty(true);
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
        initStrategy();
    }

    private void initListeners() {
        skipRelationshipsLoading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setSkipRelationshipsLoading(skipRelationshipsLoading.isSelected());
                projectController.setDirty(true);
            }
        });
        skipPrimaryKeyLoading.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setSkipPrimaryKeyLoading(skipPrimaryKeyLoading.isSelected());
                projectController.setDirty(true);
            }
        });
        forceDataMapCatalog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setForceDataMapCatalog(forceDataMapCatalog.isSelected());
                projectController.setDirty(true);
            }
        });
        forceDataMapSchema.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setForceDataMapSchema(forceDataMapSchema.isSelected());
                projectController.setDirty(true);
            }
        });
        usePrimitives.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setUsePrimitives(usePrimitives.isSelected());
                projectController.setDirty(true);
            }
        });
        useJava7Types.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReverseEngineeringBySelectedMap().setUseJava7Types(useJava7Types.isSelected());
                projectController.setDirty(true);
            }
        });
    }

    JComboBox<String> getStrategyCombo() {
        return strategyCombo;
    }

    TextAdapter getMeaningfulPk() {
        return meaningfulPk;
    }

    TextAdapter getStripFromTableNames() {
        return stripFromTableNames;
    }

    public TextAdapter getDefaultPackage() {
        return defaultPackage;
    }

    JCheckBox getSkipRelationshipsLoading() {
        return skipRelationshipsLoading;
    }

    JCheckBox getSkipPrimaryKeyLoading() {
        return skipPrimaryKeyLoading;
    }

    JCheckBox getForceDataMapCatalog() {
        return forceDataMapCatalog;
    }

    JCheckBox getForceDataMapSchema() {
        return forceDataMapSchema;
    }

    JCheckBox getUsePrimitives() {
        return usePrimitives;
    }

    JCheckBox getUseJava7Types() {
        return useJava7Types;
    }

}
