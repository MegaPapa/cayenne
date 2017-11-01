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
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.dialog.codegen.StandardPanelComponent;
import org.apache.cayenne.modeler.event.DataMapDisplayEvent;
import org.apache.cayenne.modeler.event.DataMapDisplayListener;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 4.1
 */
public class CgenView extends JPanel {

    private static final String MAIN_COLUMN_LAUOUT = "fill:500dlu";
    private static final String MAIN_ROW_LAYOUT = "fill:200dlu";

    protected Collection<StandardPanelComponent> dataMapLines;

    private ProjectController projectController;
    private CodeGeneratorPanel codeGeneratorPanel;
    private CgenConfigPanel configPanel;

    public CgenView(ProjectController projectController) {
        this.projectController = projectController;
        this.dataMapLines = new ArrayList<>();
        initUIElements();
        buildUI();
        initListeners();
    }

    private void initListeners() {
        projectController.addDataMapDisplayListener(new DataMapDisplayListener() {
            @Override
            public void currentDataMapChanged(DataMapDisplayEvent e) {
                DataMap map = e.getDataMap();
                if (map != null) {
                    //codeGeneratorPanel.getClasses().getTable()
                }
            }
        });
    }

    private void buildUI() {
        FormLayout layout = new FormLayout(MAIN_COLUMN_LAUOUT, MAIN_ROW_LAYOUT);
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.append(codeGeneratorPanel);
        builder.nextLine();
        builder.append(configPanel);
        this.setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
    }

    private void initUIElements() {
        this.codeGeneratorPanel = new CodeGeneratorPanel(projectController);
        this.configPanel = new CgenConfigPanel();
    }

    public Collection<StandardPanelComponent> getDataMapLines() {
        return dataMapLines;
    }
}
