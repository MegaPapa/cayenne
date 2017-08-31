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

package org.apache.cayenne.modeler.dialog.db.load;

import org.apache.cayenne.modeler.action.AddCatalogAction;
import org.apache.cayenne.modeler.action.AddExcludeColumnAction;
import org.apache.cayenne.modeler.action.AddExcludeProcedureAction;
import org.apache.cayenne.modeler.action.AddExcludeTableAction;
import org.apache.cayenne.modeler.action.AddIncludeColumnAction;
import org.apache.cayenne.modeler.action.AddIncludeProcedureAction;
import org.apache.cayenne.modeler.action.AddIncludeTableAction;
import org.apache.cayenne.modeler.action.AddSchemaAction;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @since 4.1
 */
public class RootPopUpMenu extends DefaultPopUpMenu {

    private static final int FIRST_POSITION = 0;

    protected JMenuItem addItem;
    protected JMenuItem addCatalog;
    protected JMenuItem addSchema;
    protected JMenuItem addIncludeTable;
    protected JMenuItem addExcludeTable;
    protected JMenuItem addIncludeColumn;
    protected JMenuItem addExcludeColumn;
    protected JMenuItem addIncludeProcedure;
    protected JMenuItem addExcludeProcedure;


    public RootPopUpMenu() {
        initPopUpMenuElements();
        initListeners();
        this.add(addItem, FIRST_POSITION);
        delete.setVisible(false);
        rename.setVisible(false);
    }

    private void initListeners() {
        addCatalog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddCatalogAction.class).actionPerformed(e);
            }
        });
        addSchema.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddSchemaAction.class).actionPerformed(e);
            }
        });
        addIncludeTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddIncludeTableAction.class).actionPerformed(e);
            }
        });
        addExcludeTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddExcludeTableAction.class).actionPerformed(e);
            }
        });
        addIncludeColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddIncludeColumnAction.class).actionPerformed(e);
            }
        });
        addExcludeColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddExcludeColumnAction.class).actionPerformed(e);
            }
        });
        addIncludeProcedure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddIncludeProcedureAction.class).actionPerformed(e);
            }
        });
        addExcludeProcedure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectController.getApplication().getActionManager().getAction(AddExcludeProcedureAction.class).actionPerformed(e);
            }
        });
    }

    private void initPopUpMenuElements() {
        addItem = new JMenu("Add");
        addCatalog = new JMenuItem("Catalog");
        addSchema = new JMenuItem("Schema");
        addIncludeTable = new JMenuItem("Include Table");
        addExcludeTable = new JMenuItem("Exclude Table");
        addIncludeColumn = new JMenuItem("Include Column");
        addExcludeColumn = new JMenuItem("Exclude Column");
        addIncludeProcedure = new JMenuItem("Include Procedure");
        addExcludeProcedure = new JMenuItem("Exclude Procedure");

        addItem.add(addSchema);
        addItem.add(addCatalog);
        addItem.add(addIncludeTable);
        addItem.add(addExcludeTable);
        addItem.add(addIncludeColumn);
        addItem.add(addExcludeColumn);
        addItem.add(addIncludeProcedure);
        addItem.add(addExcludeProcedure);
    }
}
