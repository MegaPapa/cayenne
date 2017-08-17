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

package org.apache.cayenne.tools;

import foundrylogic.vpp.VPPConfig;
import org.apache.cayenne.configuration.xml.DataChannelMetaData;
import org.apache.cayenne.dbsync.filter.NamePatternMatcher;
import org.apache.cayenne.dbsync.reverse.configuration.ToolsModule;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.gen.ArtifactsGenerationMode;
import org.apache.cayenne.gen.CgenModule;
import org.apache.cayenne.gen.ClassGenerationAction;
import org.apache.cayenne.gen.ClientClassGenerationAction;
import org.apache.cayenne.gen.xml.CgenConfiguration;
import org.apache.cayenne.map.DataMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.velocity.VelocityContext;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;

/**
 * An Ant task to perform class generation based on CayenneDataMap.
 * 
 * @since 3.0
 */
public class CayenneGeneratorTask extends CayenneTask {

    private static final File[] NO_FILES = new File[0];

    protected String includeEntitiesPattern;
    protected String excludeEntitiesPattern;
    protected VPPConfig vppConfig;

    protected File map;
    protected File additionalMaps[];
    protected boolean client;
    protected File destDir;
    protected String encoding;
    protected boolean makepairs;
    protected String mode;
    protected String outputPattern;
    protected boolean overwrite;
    protected String superpkg;
    protected String supertemplate;
    protected String template;
    protected String embeddabletemplate;
    protected String embeddablesupertemplate;
    protected String querytemplate;
    protected String querysupertemplate;
    protected boolean usepkgpath;
    protected boolean createpropertynames;

    private boolean useAntConfiguration;

    public CayenneGeneratorTask() {
        this.makepairs = true;
        this.mode = ArtifactsGenerationMode.ENTITY.getLabel();
        this.outputPattern = "*.java";
        this.usepkgpath = true;
    }

    protected VelocityContext getVppContext() {
        initializeVppConfig();
        return vppConfig.getVelocityContext();
    }

    ClassGenerationAction newGeneratorInstance(boolean client) {
        return client ? new ClientClassGenerationAction() : new ClassGenerationAction();
    }

    private ClassGenerationAction createGeneratorAction(CgenConfiguration configuration) {
        ClassGenerationAction action = newGeneratorInstance(configuration.isClient());

        action.setContext(getVppContext());
        action.setDestDir(configuration.getDestDir());
        action.setEncoding(configuration.getEncoding());
        action.setMakePairs(configuration.isMakePairs());
        action.setArtifactsGenerationMode(configuration.getArtifactsGenerationMode().getLabel());
        action.setOutputPattern(configuration.getOutputPattern());
        action.setOverwrite(configuration.isOverwrite());
        action.setSuperPkg(configuration.getSuperPkg());
        action.setSuperTemplate(configuration.getSuperTemplate());
        action.setTemplate(configuration.getTemplate());
        action.setEmbeddableSuperTemplate(configuration.getEmbeddableSuperTemplate());
        action.setEmbeddableTemplate(configuration.getEmbeddableTemplate());
        action.setQueryTemplate(configuration.getTemplate());
        action.setQuerySuperTemplate(configuration.getSuperTemplate());
        action.setUsePkgPath(configuration.isUsePkgPath());
        action.setCreatePropertyNames(configuration.isCreatePropertyNames());

        return action;
    }

    protected ClassGenerationAction createGeneratorAction() {
        ClassGenerationAction action = newGeneratorInstance(client);

        action.setContext(getVppContext());
        action.setDestDir(destDir);
        action.setEncoding(encoding);
        action.setMakePairs(makepairs);
        action.setArtifactsGenerationMode(mode);
        action.setOutputPattern(outputPattern);
        action.setOverwrite(overwrite);
        action.setSuperPkg(superpkg);
        action.setSuperTemplate(supertemplate);
        action.setTemplate(template);
        action.setEmbeddableSuperTemplate(embeddablesupertemplate);
        action.setEmbeddableTemplate(embeddabletemplate);
        action.setQueryTemplate(querytemplate);
        action.setQuerySuperTemplate(querysupertemplate);
        action.setUsePkgPath(usepkgpath);
        action.setCreatePropertyNames(createpropertynames);

        return action;
    }

    /**
     * Executes the task. It will be called by ant framework.
     */
    @Override
    public void execute() throws BuildException {
        if (destDir != null) {
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
        }

        validateAttributes();

        Injector injector = DIBootstrap.createInjector(new ToolsModule(LoggerFactory.getLogger(CayenneGeneratorTask.class)),
                                                        new CgenModule());

        AntLogger logger = new AntLogger(this);
        CayenneGeneratorMapLoaderAction loadAction = new CayenneGeneratorMapLoaderAction(injector);

        loadAction.setMainDataMapFile(map);

        CayenneGeneratorEntityFilterAction filterAction;

        try {
            loadAction.setAdditionalDataMapFiles(additionalMaps);

            DataMap dataMap = loadAction.getMainDataMap();
            DataChannelMetaData metaData = injector.getInstance(DataChannelMetaData.class);
            CgenConfiguration dataMapConfiguration = metaData.get(dataMap, CgenConfiguration.class);
            ClassGenerationAction generatorAction;

            if (dataMapConfiguration != null) {
                File metaAdditionalDataMaps = dataMapConfiguration.getAdditionalMaps();
                loadAction.setAdditionalDataMapFiles(convertAdditionalDataMaps(metaAdditionalDataMaps));
                dataMap = loadAction.getMainDataMap();
            }

            if ((useAntConfiguration) && (dataMapConfiguration != null)) {
                logger.warn("Found several cgen configurations. Configuration selected from build file.");
            }

            if ((dataMapConfiguration != null) && (!useAntConfiguration)) {
                filterAction = createFilterAction(dataMapConfiguration.isClient(), dataMapConfiguration.getIncludeEntities(),
                        dataMapConfiguration.getExcludeEntities(), logger);
                generatorAction = createGeneratorAction(dataMapConfiguration);
            } else {
                filterAction = createFilterAction(client, includeEntitiesPattern, excludeEntitiesPattern, logger);
                generatorAction = createGeneratorAction();
            }

            generatorAction.setLogger(logger);
            generatorAction.setTimestamp(map.lastModified());
            generatorAction.setDataMap(dataMap);
            generatorAction.addEntities(filterAction.getFilteredEntities(dataMap));
            generatorAction.addEmbeddables(filterAction.getFilteredEmbeddables(dataMap));
            generatorAction.addQueries(dataMap.getQueryDescriptors());
            generatorAction.execute();
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private CayenneGeneratorEntityFilterAction createFilterAction(boolean client, String includeEntities, String excludeEntities,
                                                                  AntLogger logger) {
        CayenneGeneratorEntityFilterAction filterAction = new CayenneGeneratorEntityFilterAction();
        filterAction.setClient(client);
        filterAction.setNameFilter(NamePatternMatcher.build(logger, includeEntities, excludeEntities));
        return filterAction;
    }

    protected File[] convertAdditionalDataMaps(File additionalMaps) throws Exception {

        if (additionalMaps == null) {
            return NO_FILES;
        }

        if (!additionalMaps.isDirectory()) {
            throw new BuildException(
                    "'additionalMaps' must be a directory.");
        }

        FilenameFilter mapFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null &&
                        name.toLowerCase().endsWith(".map.xml");
            }
        };
        return additionalMaps.listFiles(mapFilter);
    }

    /**
     * Validates attributes that are not related to internal DefaultClassGenerator. Throws
     * BuildException if attributes are invalid.
     */
    protected void validateAttributes() throws BuildException {
        if (map == null && this.getProject() == null) {
            throw new BuildException("either 'map' or 'project' is required.");
        }
    }

    /**
     * Sets the map.
     * 
     * @param map The map to set
     */
    public void setMap(File map) {
        this.map = map;
    }

    /**
     * Sets the additional DataMaps.
     * 
     * @param additionalMapsPath The additional DataMaps to set
     */
    public void setAdditionalMaps(Path additionalMapsPath) {
        String additionalMapFilenames[] = additionalMapsPath.list();
        this.additionalMaps = new File[additionalMapFilenames.length];

        for (int i = 0; i < additionalMapFilenames.length; i++) {
            additionalMaps[i] = new File(additionalMapFilenames[i]);
        }
    }

    /**
     * Sets the destDir.
     */
    public void setDestDir(File destDir) {
        this.destDir = destDir;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>overwrite</code> property.
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>makepairs</code> property.
     */
    public void setMakepairs(boolean makepairs) {
        this.makepairs = makepairs;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>template</code> property.
     */
    public void setTemplate(String template) {
        this.template = template;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>supertemplate</code> property.
     */
    public void setSupertemplate(String supertemplate) {
        this.supertemplate = supertemplate;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>querytemplate</code> property.
     */
    public void setQueryTemplate(String querytemplate) {
        this.querytemplate = querytemplate;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>querysupertemplate</code> property.
     */
    public void setQuerySupertemplate(String querysupertemplate) {
        this.querysupertemplate = querysupertemplate;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>usepkgpath</code> property.
     */
    public void setUsepkgpath(boolean usepkgpath) {
        this.usepkgpath = usepkgpath;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>superpkg</code> property.
     */
    public void setSuperpkg(String superpkg) {
        this.superpkg = superpkg;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>client</code> property.
     */
    public void setClient(boolean client) {
        this.client = client;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>encoding</code> property that allows to generate files using non-default
     * encoding.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>excludeEntitiesPattern</code> property.
     */
    public void setExcludeEntities(String excludeEntitiesPattern) {
        this.excludeEntitiesPattern = excludeEntitiesPattern;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>includeEntitiesPattern</code> property.
     */
    public void setIncludeEntities(String includeEntitiesPattern) {
        this.includeEntitiesPattern = includeEntitiesPattern;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>outputPattern</code> property.
     */
    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>mode</code> property.
     */
    public void setMode(String mode) {
        this.mode = mode;
        useAntConfiguration = true;
    }

    /**
     * Sets <code>createpropertynames</code> property.
     */
    public void setCreatepropertynames(boolean createpropertynames) {
        this.createpropertynames = createpropertynames;
        useAntConfiguration = true;
    }

    public void setEmbeddabletemplate(String embeddabletemplate) {
        this.embeddabletemplate = embeddabletemplate;
        useAntConfiguration = true;
    }

    public void setEmbeddablesupertemplate(String embeddablesupertemplate) {
        this.embeddablesupertemplate = embeddablesupertemplate;
        useAntConfiguration = true;
    }

    /**
     * Provides a <code>VPPConfig</code> object to configure. (Written with createConfig()
     * instead of addConfig() to avoid run-time dependency on VPP).
     */
    public Object createConfig() {
        this.vppConfig = new VPPConfig();
        return this.vppConfig;
    }

    /**
     * If no VppConfig element specified, use the default one.
     */
    private void initializeVppConfig() {
        if (vppConfig == null) {
            vppConfig = VPPConfig.getDefaultConfig(getProject());
        }
    }
}
