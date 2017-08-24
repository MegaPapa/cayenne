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

import org.apache.cayenne.gen.ClassGenerationAction;
import org.apache.cayenne.gen.xml.CgenConfiguration;
import org.apache.cayenne.modeler.util.CayenneController;
import org.apache.cayenne.pref.CayenneProjectPreferences;
import org.apache.cayenne.pref.PreferenceDetail;
import org.apache.cayenne.swing.BindingBuilder;
import org.apache.cayenne.util.Util;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class GeneratorTabController extends CayenneController {

    protected static final String STANDARD_OBJECTS_MODE = "Standard Persistent Objects";
    protected static final String CLIENT_OBJECTS_MODE = "Client Persistent Objects";
    protected static final String ADVANCED_MODE = "Advanced";

    public static final String GENERATOR_PROPERTY = "generator";

    private static final String[] GENERATION_MODES = new String[] {
            STANDARD_OBJECTS_MODE, CLIENT_OBJECTS_MODE, ADVANCED_MODE
    };

    protected GeneratorTabPanel view;
    protected Map controllers;
    protected PreferenceDetail preferences;

    public GeneratorTabController(CodeGeneratorControllerBase parent) {
        super(parent);

        this.controllers = new HashMap(5);
        controllers.put(STANDARD_OBJECTS_MODE, new StandardModeController(parent));
        controllers.put(CLIENT_OBJECTS_MODE, new ClientModeController(parent));
        controllers.put(ADVANCED_MODE, new CustomModeController(parent));

        Component[] modePanels = new Component[GENERATION_MODES.length];
        for (int i = 0; i < GENERATION_MODES.length; i++) {
            modePanels[i] = ((GeneratorController) controllers.get(GENERATION_MODES[i]))
                    .getView();
        }

        this.view = new GeneratorTabPanel(GENERATION_MODES, modePanels);
        initBindings();
    }

    public Component getView() {
        return view;
    }

    protected CodeGeneratorControllerBase getParentController() {
        return (CodeGeneratorControllerBase) getParent();
    }

    protected void initBindings() {

        // bind actions
        BindingBuilder builder = new BindingBuilder(
                getApplication().getBindingFactory(),
                this);

        builder.bindToAction(view.getGenerationMode(), "updateModeAction()");

        CayenneProjectPreferences cayPrPref = application.getCayenneProjectPreferences();

        this.preferences = (PreferenceDetail) cayPrPref.getProjectDetailObject(
                PreferenceDetail.class,
                getViewPreferences().node("controller"));

        if (Util.isEmptyString(preferences.getProperty("mode"))) {
            preferences.setProperty("mode", STANDARD_OBJECTS_MODE);
        }

        builder.bindToComboSelection(
                view.getGenerationMode(),
                "preferences.property['mode']").updateView();

        updateModeAction();
    }

    public PreferenceDetail getPreferences() {
        return preferences;
    }

    /**
     * Resets selection to default values for a given controller.
     */
    public void updateModeAction() {
        firePropertyChange(GENERATOR_PROPERTY, null, getGeneratorController());
    }

    public GeneratorController getGeneratorController() {
        Object name = view.getGenerationMode().getSelectedItem();
        return (GeneratorController) controllers.get(name);
    }

    private void getClientSupport(CgenConfiguration configuration) {
        if (getGeneratorController() instanceof CustomModeController) {
            configuration.setClient(configuration.isClient());
        } else if (getGeneratorController() instanceof ClientModeController) {
            configuration.setClient(true);
        } else {
            configuration.setClient(false);
        }
    }

    private void updateConfiguration(ClassGenerationAction generator) {
        CgenConfiguration configuration = getApplication().getMetaData().get(generator.getDataMap(), CgenConfiguration.class);
        if (configuration == null) {
            configuration = new CgenConfiguration();
            getApplication().getMetaData().add(generator.getDataMap(), configuration);
        }
        getClientSupport(configuration);
        configuration.setOutputPattern(generator.getOutputPattern());
        configuration.setDestDir(generator.getDestDir());
        configuration.setCreatePropertyNames(generator.getCreatePropertyNames());
        configuration.setUsePkgPath(generator.getUsePkgPath());
        configuration.setTemplate(generator.getTemplate());
        configuration.setSuperTemplate(generator.getSuperTemplate());
        configuration.setSuperPkg(generator.getSuperPkg());
        configuration.setOverwrite(generator.getOverwrite());
        configuration.setArtifactsGenerationMode(generator.getArtifactsGenerationMode());
        configuration.setMakePairs(generator.getMakePairs());
    }

    public Collection<ClassGenerationAction> getGenerator() {
        GeneratorController modeController = getGeneratorController();
        Collection<ClassGenerationAction> generators = null;
        if (modeController != null) {
            generators = modeController.createGenerator();
            if (generators != null) {
                for (ClassGenerationAction generator : generators) {
                    if (generator != null) {
                        updateConfiguration(generator);
                    }
                }
            }
        }
        return generators;
    }
}