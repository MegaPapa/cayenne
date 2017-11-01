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
import org.apache.cayenne.modeler.dialog.codegen.ClassesTabPanel;
import org.apache.cayenne.swing.BindingBuilder;
import org.apache.cayenne.swing.ImageRendererColumn;
import org.apache.cayenne.swing.ObjectBinding;
import org.apache.cayenne.swing.TableBindingBuilder;
import org.apache.cayenne.swing.components.TopBorder;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;

/**
 * @since 4.1
 */
public class CodeGeneratorPanel extends JPanel {

    private static final String MAIN_COLUMN_LAYOUT = "fill:339dlu";

    protected JButton generateButton;
    private ClassesTabPanel classes;
    protected JLabel classesCount;
    protected ObjectBinding tableBinding;
    private ProjectController projectController;

    public CodeGeneratorPanel(ProjectController projectController) {
        this.projectController = projectController;
        this.classes = new ClassesTabPanel();
        this.generateButton = new JButton("Generate");
        this.classesCount = new JLabel("No classes selected");
        classesCount.setFont(classesCount.getFont().deriveFont(10f));

        JScrollPane scrollPane = new JScrollPane(
                classes,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel messages = new JPanel(new BorderLayout());
        messages.add(classesCount);

        JPanel buttons = new JPanel();
        buttons.setBorder(TopBorder.create());
        buttons.setLayout(new BorderLayout());
        buttons.add(classesCount, BorderLayout.WEST);
        buttons.add(generateButton, BorderLayout.EAST);

        FormLayout layout = new FormLayout(MAIN_COLUMN_LAYOUT);
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.appendSeparator("Code generation");
        builder.append(scrollPane);
        builder.append(buttons);
        this.setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);
    }

    protected void initBindings() {

        BindingBuilder builder = new BindingBuilder(
                projectController.getApplication().getBindingFactory(),
                this);

        builder.bindToAction(classes.getCheckAll(), "checkAllAction()");

        TableBindingBuilder tableBuilder = new TableBindingBuilder(builder);

        tableBuilder.addColumn(
                "",
                "parent.setCurrentClass(#item), selected",
                Boolean.class,
                true,
                Boolean.TRUE);
        tableBuilder.addColumn(
                "Class",
                "parent.getItemName(#item)",
                JLabel.class,
                false,
                "XXXXXXXXXXXXXX");

        tableBuilder.addColumn(
                "Comments, Warnings",
                "parent.getProblem(#item)",
                String.class,
                false,
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

        this.tableBinding = tableBuilder.bindToTable(classes.getTable(), "parent.classes");
        classes.getTable().getColumnModel().getColumn(1).setCellRenderer(new ImageRendererColumn());
    }

    /*public boolean isSelected() {
        return projectController.getParentController().isSelected();
    }*/

    /*public void setSelected(boolean selected) {
        getParentController().setSelected(selected);
        classSelectedAction();
    }

    *//**
     * A callback action that updates the state of Select All checkbox.
     *//*
    public void classSelectedAction() {
        int selectedCount = getParentController().getSelectedEntitiesSize() + getParentController().getSelectedEmbeddablesSize() ;

        if (selectedCount == 0) {
            view.getCheckAll().setSelected(false);
        }
        else if (selectedCount == getParentController().getClasses().size()) {
            view.getCheckAll().setSelected(true);
        }
    }*/

    /**
     * An action that updates entity check boxes in response to the Select All state
     * change.
     */
    /*public void checkAllAction() {

        Predicate predicate = view.getCheckAll().isSelected() ? PredicateUtils
                .truePredicate() : PredicateUtils.falsePredicate();

        if (getParentController().updateSelection(predicate)) {
            tableBinding.updateView();
        }
    }

    public ClassesTabPanel getClasses() {
        return classes;
    }

    public JButton getGenerateButton() {
        return generateButton;
    }

    public JLabel getClassesCount() {
        return classesCount;
    }*/
}
