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

package org.apache.cayenne.modeler.dialog.codegen;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.Embeddable;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.modeler.ProjectController;
import org.apache.cayenne.modeler.util.CayenneController;
import org.apache.cayenne.modeler.util.CellRenderers;
import org.apache.cayenne.swing.BindingBuilder;
import org.apache.cayenne.swing.ImageRendererColumn;
import org.apache.cayenne.swing.ObjectBinding;
import org.apache.cayenne.swing.TableBindingBuilder;
import org.apache.cayenne.validation.ValidationFailure;
import org.apache.cayenne.validation.ValidationResult;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;


public class ClassesTabController extends CayenneController {

    public static final String SELECTED_PROPERTY = "selected";
    public static final String GENERATE_PROPERTY = "generate";

    protected ClassesTabPanel view;
    protected ObjectBinding tableBinding;
    public List<Object> classes;
    protected Set selectedEntities;
    protected Set selectedEmbeddables;
    protected ValidationResult validation;
    private ProjectController projectController;
    private DataMap dataMap;
    private JLabel classesCount;

    protected transient Object currentClass;

    public ClassesTabController(CodeGeneratorControllerBase parent) {
        super(parent);

        this.view = new ClassesTabPanel();
        initBindings();
    }

    public ClassesTabController(ClassesTabPanel tabPanel, ProjectController projectController) {
        this.projectController = projectController;
        this.application = projectController.getApplication();
        this.view = tabPanel;
        classes = new ArrayList<>();
    }

    public Component getView() {
        return view;
    }

    public void getDatamapData() {
        this.dataMap = projectController.getCurrentDataMap();
        this.classes.addAll(new ArrayList(dataMap.getObjEntities()));
        this.classes.addAll(new ArrayList(dataMap.getEmbeddables()));
        this.selectedEntities = new HashSet();
        this.selectedEmbeddables = new HashSet();
    }

    public void initBindings() {
        //getDatamapData();
        BindingBuilder builder = new BindingBuilder(
                getApplication().getBindingFactory(),
                this);

        builder.bindToAction(view.getCheckAll(), "checkAllAction()");
        builder.bindToAction(this, "classesSelectedAction()", SELECTED_PROPERTY);

        TableBindingBuilder tableBuilder = new TableBindingBuilder(builder);
        
        tableBuilder.addColumn(
                "",
                "setCurrentClass(#item), selected",
                Boolean.class,
                true,
                Boolean.TRUE);
        tableBuilder.addColumn(
                "Class",
                "getItemName(#item)",
                JLabel.class,
                false,
                "XXXXXXXXXXXXXX");

        tableBuilder.addColumn(
                "Comments, Warnings",
                "getProblem(#item)",
                String.class,
                false,
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
       
        this.tableBinding = tableBuilder.bindToTable(view.getTable(), "classes");
        view.getTable().getColumnModel().getColumn(1).setCellRenderer(new ImageRendererColumn());
    }

    public void classesSelectedAction() {
        int size = getSelectedEntitiesSize();
        String label;

        if (size == 0) {
            label = "No entities selected";
        }
        else if (size == 1) {
            label = "One entity selected";
        }
        else {
            label = size + " entities selected";
        }

        label = label.concat("; ");

        int sizeEmb = getSelectedEmbeddablesSize();

        if (sizeEmb == 0) {
            label = label + "No embeddables selected";
        }
        else if (sizeEmb == 1) {
            label = label + "One embeddable selected";
        }
        else {
            label =label + sizeEmb + " embeddables selected";
        }

        classesCount.setText(label);
    }

    public boolean isSelected() {
        return isSelectedElement();
    }

    public boolean isSelectedElement() {
        if (currentClass instanceof ObjEntity) {
            return currentClass != null ? selectedEntities
                    .contains(((ObjEntity) currentClass).getName()) : false;
        }
        if (currentClass instanceof Embeddable) {
            return currentClass != null ? selectedEmbeddables
                    .contains(((Embeddable) currentClass).getClassName()) : false;
        }
        return false;

    }

    public void setSelected(boolean selected) {
        setSelectedFlag(selected);
        classSelectedAction();
    }

    public void setSelectedFlag(boolean selectedFlag) {
        if (currentClass == null) {
            return;
        }
        if (currentClass instanceof ObjEntity) {
            if (selectedFlag) {
                if (selectedEntities.add(((ObjEntity) currentClass).getName())) {
                    firePropertyChange(SELECTED_PROPERTY, null, null);
                }
            }
            else {
                if (selectedEntities.remove(((ObjEntity) currentClass).getName())) {
                    firePropertyChange(SELECTED_PROPERTY, null, null);
                }
            }
        }
        if (currentClass instanceof Embeddable) {
            if (selectedFlag) {
                if (selectedEmbeddables.add(((Embeddable) currentClass).getClassName())) {
                    firePropertyChange(SELECTED_PROPERTY, null, null);
                }
            }
            else {
                if (selectedEmbeddables
                        .remove(((Embeddable) currentClass).getClassName())) {
                    firePropertyChange(SELECTED_PROPERTY, null, null);
                }
            }
        }
    }

    /**
     * A callback action that updates the state of Select All checkbox.
     */
    public void classSelectedAction() {
        int selectedCount = getSelectedEntitiesSize() + getSelectedEmbeddablesSize() ;

        if (selectedCount == 0) {
            view.getCheckAll().setSelected(false);
        }
        else if (selectedCount == getClasses().size()) {
            view.getCheckAll().setSelected(true);
        }
    }

    /**
     * An action that updates entity check boxes in response to the Select All state
     * change.
     */
    public void checkAllAction() {

        Predicate predicate = view.getCheckAll().isSelected() ? PredicateUtils
                .truePredicate() : PredicateUtils.falsePredicate();

        if (updateSelection(predicate)) {
            tableBinding.updateView();
        }
    }

    public boolean updateSelection(Predicate predicate) {

        boolean modified = false;

        for (Object classObj : classes) {
            boolean select = predicate.evaluate(classObj);
            if (classObj instanceof ObjEntity) {

                if (select) {
                    if (selectedEntities.add(((ObjEntity) classObj).getName())) {
                        modified = true;
                    }
                }
                else {
                    if (selectedEntities.remove(((ObjEntity) classObj).getName())) {
                        modified = true;
                    }
                }
            }
            else if (classObj instanceof Embeddable) {
                if (select) {
                    if (selectedEmbeddables.add(((Embeddable) classObj).getClassName())) {
                        modified = true;
                    }
                }
                else {
                    if (selectedEmbeddables
                            .remove(((Embeddable) classObj).getClassName())) {
                        modified = true;
                    }
                }
            }

        }

        if (modified) {
            firePropertyChange(SELECTED_PROPERTY, null, null);
        }

        return modified;
    }

    public void setCurrentClass(Object currentClass) {
        this.currentClass = currentClass;
    }

    public JLabel getItemName(Object obj) {
        String className;
        Icon icon = null;
        if (obj instanceof Embeddable) {
            className = ((Embeddable) obj).getClassName();
            icon = CellRenderers.iconForObject(new Embeddable());
        } else {
            className = ((ObjEntity) obj).getName();
            icon = CellRenderers.iconForObject(new ObjEntity());
        }
        JLabel labelIcon = new JLabel();
        labelIcon.setIcon(icon);
        labelIcon.setVisible(true);
        labelIcon.setText(className);
        return labelIcon;
    }

    public String getProblem(Object obj) {

        String name = null;

        if (obj instanceof ObjEntity) {
            name = ((ObjEntity) obj).getName();
        }
        else if (obj instanceof Embeddable) {
            name = ((Embeddable) obj).getClassName();
        }

        if (validation == null) {
            return null;
        }

        List failures = validation.getFailures(name);
        if (failures.isEmpty()) {
            return null;
        }

        return ((ValidationFailure) failures.get(0)).getDescription();
    }

    public List<Embeddable> getSelectedEmbeddables() {

        List<Embeddable> selected = new ArrayList<>(selectedEmbeddables.size());

        for (Object classObj : classes) {
            if (classObj instanceof Embeddable
                    && selectedEmbeddables.contains(((Embeddable) classObj)
                    .getClassName())) {
                selected.add((Embeddable) classObj);
            }
        }

        return selected;
    }

    public List<ObjEntity> getSelectedEntities() {
        List<ObjEntity> selected = new ArrayList<>(selectedEntities.size());
        for (Object classObj : classes) {
            if (classObj instanceof ObjEntity
                    && selectedEntities.contains(((ObjEntity) classObj).getName())) {
                selected.add(((ObjEntity) classObj));
            }
        }

        return selected;
    }

    public int getSelectedEntitiesSize() {
        return selectedEntities.size();
    }

    public int getSelectedEmbeddablesSize() {
        return selectedEmbeddables.size();
    }

    public List<Object> getClasses() {
        return classes;
    }

    public void setClassesCount(JLabel classesCount) {
        this.classesCount = classesCount;
    }
}
