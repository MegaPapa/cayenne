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

import org.apache.cayenne.configuration.xml.DataChannelMetaData;
import org.apache.cayenne.configuration.xml.DataMapLoaderListener;
import org.apache.cayenne.configuration.xml.NamespaceAwareNestedTagHandler;
import org.apache.cayenne.map.DataMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;

/**
 * @since 4.1
 */
public class ConfigHandler extends NamespaceAwareNestedTagHandler {

    static final String CONFIG_TAG = "config";

    private static final String ADDITIONAL_MAPS_TAG = "additionalMaps";
    private static final String CLIENT_TAG = "client";
    private static final String DEST_DIR_TAG = "destDir";
    private static final String EMBEDDABLE_TEMPLATE_TAG = "embeddableTemplate";
    private static final String EMBEDDABLE__SUPER_TEMPLATE_TAG = "embeddableSuperTemplate";
    private static final String ENCODING_TAG = "encoding";
    private static final String EXCLUDE_ENTITIES_TAG = "excludeEntities";
    private static final String INCLUDE_ENTITIES_TAG = "includeEntities";
    private static final String MAKE_PAIRS_TAG = "makePairs";
    private static final String MODE_TAG = "mode";
    private static final String OVERWRITE_TAG = "overwrite";
    private static final String SUPER_PKG_TAG = "superPkg";
    private static final String SUPER_TEMPLATE_TAG = "superTemplate";
    private static final String TEMPLATE_TAG = "template";
    private static final String USE_PKG_PATH_TAG = "usePkgPath";
    private static final String CREATE_PROPERTY_NAMES_TAG = "createPropertyNames";
    private static final String OUTPUT_PATTERN_TAG = "outputPattern";

    private static final String TRUE = "true";

    private CgenConfiguration configuration;
    private DataChannelMetaData metaData;

    ConfigHandler(NamespaceAwareNestedTagHandler parentHandler, DataChannelMetaData metaData) {
        super(parentHandler);
        this.metaData = metaData;
        this.targetNamespace = CgenExtension.NAMESPACE;
    }

    @Override
    protected boolean processElement(String namespaceURI, String localName, Attributes attributes) throws SAXException {
        switch (localName) {
            case CONFIG_TAG:
                createConfig();
                return true;
        }

        return false;
    }

    @Override
    protected void processCharData(String localName, String data) {
        switch (localName) {
            case ADDITIONAL_MAPS_TAG:
                createAdditionalMaps(data);
                break;
            case CLIENT_TAG:
                createClient(data);
                break;
            case DEST_DIR_TAG:
                createDestDir(data);
                break;
            case EMBEDDABLE_TEMPLATE_TAG:
                createEmbeddableTemplate(data);
                break;
            case EMBEDDABLE__SUPER_TEMPLATE_TAG:
                createEmbeddableSuperTemplate(data);
                break;
            case ENCODING_TAG:
                createEncoding(data);
                break;
            case EXCLUDE_ENTITIES_TAG:
                createExcludeEntities(data);
                break;
            case INCLUDE_ENTITIES_TAG:
                createIncludeEntities(data);
                break;
            case MAKE_PAIRS_TAG:
                createMakePairs(data);
                break;
            case MODE_TAG:
                createMode(data);
                break;
            case OVERWRITE_TAG:
                createOverwrite(data);
                break;
            case SUPER_PKG_TAG:
                createSuperPkgPath(data);
                break;
            case SUPER_TEMPLATE_TAG:
                createSuperTemplate(data);
                break;
            case TEMPLATE_TAG:
                createTemplate(data);
                break;
            case USE_PKG_PATH_TAG:
                createUsePkgPath(data);
                break;
            case CREATE_PROPERTY_NAMES_TAG:
                createPropertyNames(data);
                break;
            case OUTPUT_PATTERN_TAG:
                createOutputPattern(data);
                break;
        }
    }

    private void createOutputPattern(String ouputPattern) {
        if (ouputPattern.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setOutputPattern(ouputPattern);
        }
    }

    private void createPropertyNames(String createPropertyNames) {
        if (createPropertyNames.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            if (createPropertyNames.equals(TRUE)) {
                configuration.setCreatePropertyNames(true);
            } else {
                configuration.setCreatePropertyNames(false);
            }
        }
    }

    private void createUsePkgPath(String usePkgPath) {
        if (usePkgPath.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            if (usePkgPath.equals(TRUE)) {
                configuration.setUsePkgPath(true);
            } else {
                configuration.setUsePkgPath(false);
            }
        }
    }

    private void createTemplate(String template) {
        if (template.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setTemplate(template);
        }
    }

    private void createSuperTemplate(String superTemplate) {
        if (superTemplate.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setSuperTemplate(superTemplate);
        }
    }

    private void createSuperPkgPath(String superPkgPath) {
        if (superPkgPath.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setSuperPkg(superPkgPath);
        }
    }

    private void createOverwrite(String overwrite) {
        if (overwrite.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            if (overwrite.equals(TRUE)) {
                configuration.setOverwrite(true);
            } else {
                configuration.setOverwrite(false);
            }
        }
    }

    private void createMode(String mode) {
        if (mode.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setArtifactsGenerationMode(mode);
        }
    }

    private void createMakePairs(String makePairs) {
        if (makePairs.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            if (makePairs.equals(TRUE)) {
                configuration.setMakePairs(true);
            } else {
                configuration.setMakePairs(false);
            }
        }
    }

    private void createIncludeEntities(String includeEntities) {
        if (includeEntities.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setIncludeEntities(includeEntities);
        }
    }

    private void createExcludeEntities(String excludeEntities) {
        if (excludeEntities.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setExcludeEntities(excludeEntities);
        }
    }

    private void createEncoding(String encoding) {
        if (encoding.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setEncoding(encoding);
        }
    }

    private void createEmbeddableSuperTemplate(String embeddableSuperTemplate) {
        if (embeddableSuperTemplate.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setEmbeddableSuperTemplate(embeddableSuperTemplate);
        }
    }

    private void createEmbeddableTemplate(String embeddableTemplate) {
        if (embeddableTemplate.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setEmbeddableTemplate(embeddableTemplate);
        }
    }

    private void createDestDir(String destDir) {
        if (destDir.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setDestDir(new File(destDir));
        }
    }

    private void createClient(String client) {
        if (client.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            if (client.equals(TRUE)) {
                configuration.setClient(true);
            }
        }
    }

    private void createAdditionalMaps(String additionalMaps) {
        if (additionalMaps.trim().length() == 0) {
            return;
        }

        if (configuration != null) {
            configuration.setAdditionalMaps(new File(additionalMaps));
        }
    }

    private void initConfiguration() {
        configuration.setArtifactsGenerationMode("entity");
        configuration.setOutputPattern("*.java");
        configuration.setUsePkgPath(true);
        configuration.setMakePairs(true);
    }

    private void createConfig() {
        configuration = new CgenConfiguration();
        initConfiguration();
        loaderContext.addDataMapListener(new DataMapLoaderListener() {
            @Override
            public void onDataMapLoaded(DataMap dataMap) {
                configuration.setDataMap(dataMap);
                ConfigHandler.this.metaData.add(dataMap, configuration);
            }
        });
    }
}
