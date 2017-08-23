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
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.CodeTemplateManager;
import org.apache.cayenne.modeler.dialog.pref.PreferenceDialog;
import org.apache.cayenne.modeler.pref.DataMapDefaults;
import org.apache.cayenne.swing.BindingBuilder;
import org.apache.cayenne.swing.ObjectBinding;
import org.apache.cayenne.util.Util;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A controller for the custom generation mode.
 */
public class CustomModeController extends GeneratorController {

	// correspond to non-public constants on MapClassGenerator.
	static final String MODE_DATAMAP = "datamap";
	static final String MODE_ENTITY = "entity";
	static final String MODE_ALL = "all";

	static final String DATA_MAP_MODE_LABEL = "DataMap generation";
	static final String ENTITY_MODE_LABEL = "Entity and Embeddable generation";
	static final String ALL_MODE_LABEL = "Generate all";

	static final Map<String, String> modesByLabel = new HashMap<>();

	static {
		modesByLabel.put(DATA_MAP_MODE_LABEL, MODE_DATAMAP);
		modesByLabel.put(ENTITY_MODE_LABEL, MODE_ENTITY);
		modesByLabel.put(ALL_MODE_LABEL, MODE_ALL);
	}

	protected CustomModePanel view;
	protected CodeTemplateManager templateManager;

	protected ObjectBinding superTemplate;
	protected ObjectBinding subTemplate;

	private CustomPreferencesUpdater preferencesUpdater;
	private String metaSubTemplate;
	private String metaSuperTemplate;
	private boolean client;

	public CustomPreferencesUpdater getCustomPreferencesUpdater() {
		return preferencesUpdater;
	}

	public CustomModeController(CodeGeneratorControllerBase parent) {
		super(parent);

		Object[] modeChoices = new Object[] { ENTITY_MODE_LABEL, DATA_MAP_MODE_LABEL, ALL_MODE_LABEL };
		view.getGenerationMode().setModel(new DefaultComboBoxModel(modeChoices));

		// bind preferences and init defaults...

		Set<Entry<DataMap, DataMapDefaults>> entities = getMapPreferences().entrySet();

		for (Entry<DataMap, DataMapDefaults> entry : entities) {

			if (Util.isEmptyString(entry.getValue().getSuperclassTemplate())) {
				entry.getValue().setSuperclassTemplate(CodeTemplateManager.STANDARD_SERVER_SUPERCLASS);
			}

			if (Util.isEmptyString(entry.getValue().getSubclassTemplate())) {
				entry.getValue().setSubclassTemplate(CodeTemplateManager.STANDARD_SERVER_SUBCLASS);
			}

			if (Util.isEmptyString(entry.getValue().getProperty("mode"))) {
				entry.getValue().setProperty("mode", MODE_ENTITY);
			}

			if (Util.isEmptyString(entry.getValue().getProperty("overwrite"))) {
				entry.getValue().setBooleanProperty("overwrite", false);
			}

			if (Util.isEmptyString(entry.getValue().getProperty("pairs"))) {
				entry.getValue().setBooleanProperty("pairs", true);
			}

			if (Util.isEmptyString(entry.getValue().getProperty("usePackagePath"))) {
				entry.getValue().setBooleanProperty("usePackagePath", true);
			}

			if (Util.isEmptyString(entry.getValue().getProperty("outputPattern"))) {
				entry.getValue().setProperty("outputPattern", "*.java");
			}
		}

		BindingBuilder builder = new BindingBuilder(getApplication().getBindingFactory(), this);

		builder.bindToAction(view.getManageTemplatesLink(), "popPreferencesAction()");

		builder.bindToComboSelection(view.getGenerationMode(), "customPreferencesUpdater.mode").updateView();

		builder.bindToStateChange(view.getOverwrite(), "customPreferencesUpdater.overwrite").updateView();

		builder.bindToStateChange(view.getPairs(), "customPreferencesUpdater.pairs").updateView();

		builder.bindToStateChange(view.getUsePackagePath(), "customPreferencesUpdater.usePackagePath").updateView();

		subTemplate = builder.bindToComboSelection(view.getSubclassTemplate(),
				"customPreferencesUpdater.subclassTemplate");

		superTemplate = builder.bindToComboSelection(view.getSuperclassTemplate(),
				"customPreferencesUpdater.superclassTemplate");

		builder.bindToTextField(view.getOutputPattern(), "customPreferencesUpdater.outputPattern").updateView();

		builder.bindToStateChange(view.getCreatePropertyNames(), "customPreferencesUpdater.createPropertyNames")
				.updateView();

		getConfigFromMetaData();
		updateTemplates();
	}

	private int getGenerationModeIndex(String mode) {
		String modeLabel = "";
		switch (mode) {
			case MODE_ALL:
				modeLabel = ALL_MODE_LABEL;
				break;
			case MODE_DATAMAP:
				modeLabel = DATA_MAP_MODE_LABEL;
				break;
			case MODE_ENTITY:
				modeLabel = ENTITY_MODE_LABEL;
				break;
		}
		return getElementIndexByName(view.generationMode, modeLabel);
	}

	private int getElementIndexByName(JComboBox comboBox, String name) {
		int count = comboBox.getItemCount();
		for (int i = 0; i < count; i++) {
			if (comboBox.getItemAt(i).equals(name)) {
				return i;
			}
		}
		return 0;
	}

	private void getConfigFromMetaData() {
		Collection<DataMap> dataMaps = getParentController().getDataMaps();
		for (DataMap dataMap : dataMaps) {
			CgenConfiguration configuration = getApplication().getMetaData().get(dataMap, CgenConfiguration.class);
			if (configuration != null) {
				client = configuration.isClient();
				metaSubTemplate = configuration.getTemplate();
				metaSuperTemplate = configuration.getSuperTemplate();
				view.generationMode.setSelectedIndex(getGenerationModeIndex(configuration.getArtifactsGenerationMode().getLabel()));
				view.outputPattern.setText(configuration.getOutputPattern());
				view.createPropertyNames.setSelected(configuration.isCreatePropertyNames());
				view.overwrite.setSelected(configuration.isOverwrite());
				view.pairs.setSelected(configuration.isMakePairs());
				view.usePackagePath.setSelected(configuration.isUsePkgPath());
				if (configuration.isMakePairs()) {
					view.overwrite.setEnabled(false);
				} else {
					view.getSuperclassTemplate().setEnabled(false);
				}
			}
		}
	}

	protected void createDefaults() {
		TreeMap<DataMap, DataMapDefaults> map = new TreeMap<DataMap, DataMapDefaults>();
		Collection<DataMap> dataMaps = getParentController().getDataMaps();
		for (DataMap dataMap : dataMaps) {
			DataMapDefaults preferences;
			preferences = getApplication().getFrameController().getProjectController()
					.getDataMapPreferences(this.getClass().getName().replace(".", "/"), dataMap);
			CgenConfiguration configuration = getApplication().getMetaData().get(dataMap, CgenConfiguration.class);
			if (configuration != null) {
				preferences.setSuperclassPackage(configuration.getSuperPkg());
			} else {
				preferences.setSuperclassPackage("");
			}

			preferences.updateSuperclassPackage(dataMap, false);

			map.put(dataMap, preferences);

			if (getOutputPath() == null) {
				setOutputPath(preferences.getOutputPath());
			}
		}

		setMapPreferences(map);
		preferencesUpdater = new CustomPreferencesUpdater(map);
	}

	protected GeneratorControllerPanel createView() {
		this.view = new CustomModePanel();

		Set<Entry<DataMap, DataMapDefaults>> entities = getMapPreferences().entrySet();
		for (Entry<DataMap, DataMapDefaults> entry : entities) {
			StandardPanelComponent dataMapLine = createDataMapLineBy(entry.getKey(), entry.getValue());
			dataMapLine.getDataMapName().setText(dataMapLine.getDataMap().getName());
			BindingBuilder builder = new BindingBuilder(getApplication().getBindingFactory(), dataMapLine);
			builder.bindToTextField(dataMapLine.getSuperclassPackage(), "preferences.superclassPackage").updateView();
			this.view.addDataMapLine(dataMapLine);
		}
		return view;
	}

	private StandardPanelComponent createDataMapLineBy(DataMap dataMap, DataMapDefaults preferences) {
		StandardPanelComponent dataMapLine = new StandardPanelComponent();
		dataMapLine.setDataMap(dataMap);
		dataMapLine.setPreferences(preferences);

		return dataMapLine;
	}

	protected void updateTemplates() {
		this.templateManager = getApplication().getCodeTemplateManager();

		List<String> customTemplates = new ArrayList<>(templateManager.getCustomTemplates().keySet());
		Collections.sort(customTemplates);

		List<String> superTemplates = new ArrayList<>(templateManager.getStandardSuperclassTemplates());
		if ((metaSuperTemplate != null) && (!templateManager.isDefaultTemplate(metaSuperTemplate))) {
			templateManager.getCustomTemplates().put(metaSuperTemplate, metaSuperTemplate);
			superTemplates.add(metaSuperTemplate);
		}
		Collections.sort(superTemplates);
		superTemplates.addAll(customTemplates);

		List<String> subTemplates = new ArrayList<>(templateManager.getStandardSubclassTemplates());
		if ((metaSubTemplate != null) && (!templateManager.isDefaultTemplate(metaSubTemplate))) {
			templateManager.getCustomTemplates().put(metaSubTemplate, metaSuperTemplate);
			subTemplates.add(metaSubTemplate);
		}
		Collections.sort(subTemplates);
		subTemplates.addAll(customTemplates);

		this.view.getSubclassTemplate().setModel(new DefaultComboBoxModel(subTemplates.toArray()));
		this.view.getSuperclassTemplate().setModel(new DefaultComboBoxModel(superTemplates.toArray()));

		if ((!client) && (metaSubTemplate == null) && (metaSuperTemplate == null)) {
			view.getSuperclassTemplate().setSelectedIndex(getElementIndexByName(
					view.getSuperclassTemplate(), CodeTemplateManager.STANDARD_SERVER_SUPERCLASS)
			);
			view.getSubclassTemplate().setSelectedIndex(getElementIndexByName(
					view.getSubclassTemplate(), CodeTemplateManager.STANDARD_SERVER_SUBCLASS)
			);
		} else {
			view.getSuperclassTemplate().setSelectedIndex(getElementIndexByName(
					view.getSuperclassTemplate(), CodeTemplateManager.STANDARD_CLIENT_SUPERCLASS)
			);
			view.getSubclassTemplate().setSelectedIndex(getElementIndexByName(
					view.getSubclassTemplate(), CodeTemplateManager.STANDARD_CLIENT_SUBCLASS)
			);
		}

		if ((metaSubTemplate != null) && (!templateManager.isDefaultTemplate(metaSubTemplate)))  {
			view.getSubclassTemplate().setSelectedIndex(getElementIndexByName(view.getSubclassTemplate(), metaSubTemplate));
		}
		if ((metaSuperTemplate != null) && (!templateManager.isDefaultTemplate(metaSuperTemplate))) {
			view.getSuperclassTemplate().setSelectedIndex(getElementIndexByName(view.getSuperclassTemplate(), metaSuperTemplate));
		}

		superTemplate.updateView();
		subTemplate.updateView();
	}

	public Component getView() {
		return view;
	}

	public Collection<ClassGenerationAction> createGenerator() {

		mode = modesByLabel.get(view.getGenerationMode().getSelectedItem()).toString();

		Collection<ClassGenerationAction> generators = super.createGenerator();

		String superKey = view.getSuperclassTemplate().getSelectedItem().toString();
		String superTemplate = templateManager.getTemplatePath(superKey);

		String subKey = view.getSubclassTemplate().getSelectedItem().toString();
		String subTemplate = templateManager.getTemplatePath(subKey);

		for (ClassGenerationAction generator : generators) {
			generator.setSuperTemplate(superTemplate);
			generator.setTemplate(subTemplate);
			generator.setOverwrite(view.getOverwrite().isSelected());
			generator.setUsePkgPath(view.getUsePackagePath().isSelected());
			generator.setMakePairs(view.getPairs().isSelected());
			generator.setCreatePropertyNames(view.getCreatePropertyNames().isSelected());

			if (!Util.isEmptyString(view.getOutputPattern().getText())) {
				generator.setOutputPattern(view.getOutputPattern().getText());
			}
		}

		return generators;
	}

	public void popPreferencesAction() {
		new PreferenceDialog(getApplication().getFrameController()).startupAction(PreferenceDialog.TEMPLATES_KEY);
		updateTemplates();
	}

	@Override
	protected ClassGenerationAction newGenerator() {
		ClassGenerationAction action = new ClassGenerationAction();
		getApplication().getInjector().injectMembers(action);
		return action;
	}
}
