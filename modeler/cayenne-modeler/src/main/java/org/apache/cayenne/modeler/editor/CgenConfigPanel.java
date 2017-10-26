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
import org.apache.cayenne.swing.control.ActionLink;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * @since 4.1
 */
public class CgenConfigPanel extends JPanel {

    private static final String MAIN_COLUMN_LAYOUT = "fill:80dlu, 3dlu, fill:200dlu, 6dlu, fill:50dlu";
    private static final int FULL_LINE_SPAN = 3;

    protected ActionLink manageTemplatesLink;
    protected JButton selectOutputFolder;
    protected JComboBox generationMode;
    protected JComboBox subclassTemplate;
    protected JComboBox superclassTemplate;
    protected JCheckBox pairs;
    protected JCheckBox overwrite;
    protected JCheckBox usePackagePath;
    protected JTextField outputPattern;
    protected JCheckBox createPropertyNames;
    protected JTextField outputFolder;

    private DefaultFormBuilder builder;

    public CgenConfigPanel() {
        initUIElements();
        initListeners();
        buildUI();
    }

    private void initListeners() {
        pairs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                superclassTemplate.setEnabled(pairs.isSelected());
                overwrite.setEnabled(!pairs.isSelected());
            }
        });
    }

    private void initUIElements() {
        this.outputFolder = new JTextField();
        this.selectOutputFolder = new JButton("Select");
        this.generationMode = new JComboBox();
        this.superclassTemplate = new JComboBox();
        this.subclassTemplate = new JComboBox();
        this.pairs = new JCheckBox();
        this.overwrite = new JCheckBox();
        this.usePackagePath = new JCheckBox();
        this.outputPattern = new JTextField();
        this.createPropertyNames = new JCheckBox();
        this.manageTemplatesLink = new ActionLink("Customize Templates...");
        manageTemplatesLink.setFont(manageTemplatesLink.getFont().deriveFont(10f));
    }

    private void buildUI() {
        FormLayout layout = new FormLayout(MAIN_COLUMN_LAYOUT);
        builder = new DefaultFormBuilder(layout);

        builder.append("Output Directory:", outputFolder, 1);
        builder.append(selectOutputFolder);
        builder.nextLine();

        builder.append("Generation Mode:", generationMode, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Subclass Template:", subclassTemplate, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Superclass Template:", superclassTemplate, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Output Pattern:", outputPattern, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Make Pairs:", pairs, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Use Package Path:", usePackagePath, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Overwrite Subclasses:", overwrite, FULL_LINE_SPAN);
        builder.nextLine();

        builder.append("Create Property Names:", createPropertyNames, FULL_LINE_SPAN);
        builder.nextLine();

        JPanel links = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        links.add(manageTemplatesLink);

        builder.append(links);

        setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
    }
}
