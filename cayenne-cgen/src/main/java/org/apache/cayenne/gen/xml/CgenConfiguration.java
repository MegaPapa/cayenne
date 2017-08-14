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

package org.apache.cayenne.gen.xml;

import org.apache.cayenne.gen.ArtifactsGenerationMode;
import org.apache.cayenne.gen.ClassGenerationAction;
import org.apache.cayenne.gen.ClientClassGenerationAction;
import org.apache.cayenne.map.DataMap;

import java.io.File;

/**
 * @since 4.1
 */
public class CgenConfiguration {

    private ClassGenerationAction action;
    private DataMap dataMap;
    
    private String superPkg;
    private ArtifactsGenerationMode artifactsGenerationMode = ArtifactsGenerationMode.ENTITY;
    private boolean makePairs;
    private File destDir;
    private boolean overwrite;
    private boolean usePkgPath = true;
    private String template;
    private String superTemplate;
    private String embeddableTemplate;
    private String embeddableSuperTemplate;
    private String encoding;
    private boolean createPropertyNames;
    private String outputPattern = "*.java";

    private File additionalMaps;
    private String includeEntities;
    private String excludeEntities;
    private boolean client;
    
    private void packActionData(ClassGenerationAction action) {
        action.setCreatePropertyNames(this.isCreatePropertyNames());
        action.setUsePkgPath(this.isUsePkgPath());
        action.setTemplate(this.getTemplate());
        action.setSuperTemplate(this.getSuperTemplate());
        action.setSuperPkg(this.getSuperPkg());
        action.setOverwrite(this.isOverwrite());
        if (this.getArtifactsGenerationMode() != null) {
            action.setArtifactsGenerationMode(this.getArtifactsGenerationMode().getLabel());
        }
        action.setMakePairs(this.isMakePairs());
        action.setEncoding(this.getEncoding());
        action.setEmbeddableSuperTemplate(this.getEmbeddableSuperTemplate());
        action.setEmbeddableTemplate(this.getEmbeddableTemplate());
        action.setDestDir(this.getDestDir());
        action.setOutputPattern(this.getOutputPattern());
    }
    
    public ClassGenerationAction getAction() {
        if (action == null) {
            action = client ? new ClientClassGenerationAction() : new ClassGenerationAction();
            packActionData(action);
        }
        return action;
    }
    
    public String getSuperPkg() {
        return superPkg;
    }

    public void setSuperPkg(String superPkg) {
        this.superPkg = superPkg;
    }

    public ArtifactsGenerationMode getArtifactsGenerationMode() {
        return artifactsGenerationMode;
    }

    public void setArtifactsGenerationMode(String artifactsGenerationMode) {
        if (ArtifactsGenerationMode.ENTITY.getLabel().equalsIgnoreCase(artifactsGenerationMode)) {
            this.artifactsGenerationMode = ArtifactsGenerationMode.ENTITY;
        } else if (ArtifactsGenerationMode.DATAMAP.getLabel().equalsIgnoreCase(artifactsGenerationMode)) {
            this.artifactsGenerationMode = ArtifactsGenerationMode.DATAMAP;
        } else {
            this.artifactsGenerationMode = ArtifactsGenerationMode.ALL;
        }
    }

    public boolean isMakePairs() {
        return makePairs;
    }

    public void setMakePairs(boolean makePairs) {
        this.makePairs = makePairs;
    }

    public File getDestDir() {
        return destDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isUsePkgPath() {
        return usePkgPath;
    }

    public void setUsePkgPath(boolean usePkgPath) {
        this.usePkgPath = usePkgPath;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getSuperTemplate() {
        return superTemplate;
    }

    public void setSuperTemplate(String superTemplate) {
        this.superTemplate = superTemplate;
    }

    public String getEmbeddableTemplate() {
        return embeddableTemplate;
    }

    public void setEmbeddableTemplate(String embeddableTemplate) {
        this.embeddableTemplate = embeddableTemplate;
    }

    public String getEmbeddableSuperTemplate() {
        return embeddableSuperTemplate;
    }

    public void setEmbeddableSuperTemplate(String embeddableSuperTemplate) {
        this.embeddableSuperTemplate = embeddableSuperTemplate;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isCreatePropertyNames() {
        return createPropertyNames;
    }

    public void setCreatePropertyNames(boolean createPropertyNames) {
        this.createPropertyNames = createPropertyNames;
    }

    public String getIncludeEntities() {
        return includeEntities;
    }

    public void setIncludeEntities(String includeEntities) {
        this.includeEntities = includeEntities;
    }

    public String getExcludeEntities() {
        return excludeEntities;
    }

    public void setExcludeEntities(String excludeEntities) {
        this.excludeEntities = excludeEntities;
    }

    public boolean isClient() {
        return client;
    }

    public void setClient(boolean client) {
        this.client = client;
    }

    public File getAdditionalMaps() {
        return additionalMaps;
    }

    public void setAdditionalMaps(File additionalMaps) {
        this.additionalMaps = additionalMaps;
    }

    public DataMap getDataMap() {
        return dataMap;
    }

    public void setDataMap(DataMap dataMap) {
        this.dataMap = dataMap;
    }

    public String getOutputPattern() {
        return outputPattern;
    }

    public void setOutputPattern(String outputPattern) {
        this.outputPattern = outputPattern;
    }
}
