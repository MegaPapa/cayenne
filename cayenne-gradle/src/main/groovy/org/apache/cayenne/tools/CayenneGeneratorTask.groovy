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

package org.apache.cayenne.tools

import org.apache.cayenne.dbsync.filter.NamePatternMatcher
import org.apache.cayenne.map.DataMap
import org.apache.cayenne.gen.ClassGenerationAction;
import org.apache.cayenne.gen.ClientClassGenerationAction;
import org.apache.cayenne.tools.tool.CgenConfigExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

/**
 * @since 4.0
 */
class CayenneGeneratorTask extends DefaultTask {

    private static final File[] NO_FILES = new File[0]

    private File additionalMaps

    private boolean client = false

    private File destDir = project.projectDir

    private String encoding

    private String excludeEntities

    private String includeEntities

    private boolean makePairs = true

    private File map

    private String mode

    private String outputPattern

    private boolean overwrite = false

    private String superPkg

    private String superTemplate

    private String template

    private String embeddableSuperTemplate

    private String embeddableTemplate

    private boolean usePkgPath = true

    private boolean createPropertyNames = false

    private def fillData() {
        if (CgenConfigExtension.additionalMaps != null) {
            this.additionalMaps = new File(CgenConfigExtension.additionalMaps)
        }
        this.client = CgenConfigExtension.client
        if (CgenConfigExtension.destDir != null) {
            this.destDir = new File(CgenConfigExtension.destDir)
        }
        this.encoding = CgenConfigExtension.encoding
        this.excludeEntities = CgenConfigExtension.excludeEntities
        this.includeEntities = CgenConfigExtension.includeEntities
        this.makePairs = CgenConfigExtension.makePairs
        this.map = new File(CgenConfigExtension.map)
        this.mode = CgenConfigExtension.mode
        this.outputPattern = CgenConfigExtension.outputPattern
        this.overwrite = CgenConfigExtension.overwrite
        this.superPkg = CgenConfigExtension.superPkg
        this.superTemplate = CgenConfigExtension.superTemplate
        this.template = CgenConfigExtension.template
        this.embeddableSuperTemplate = CgenConfigExtension.embeddableSuperTemplate
        this.embeddableTemplate = CgenConfigExtension.embeddableTemplate
        this.usePkgPath = CgenConfigExtension.usePkgPath
        this.createPropertyNames = CgenConfigExtension.createPropertyNames
    }

    @TaskAction
    def generate() {

        fillData()
        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        CayenneGeneratorMapLoaderAction loaderAction = new CayenneGeneratorMapLoaderAction();

        loaderAction.setMainDataMapFile(map)

        CayenneGeneratorEntityFilterAction filterAction = new CayenneGeneratorEntityFilterAction()
        filterAction.setClient(client)
        filterAction.setNameFilter(NamePatternMatcher.build(logger, includeEntities, excludeEntities))


        try {
            loaderAction.setAdditionalDataMapFiles(convertAdditionalDataMaps())

            DataMap dataMap = loaderAction.getMainDataMap()

            ClassGenerationAction generator = this.createGenerator()
            generator.setLogger(logger)
            generator.setTimestamp(map.lastModified())
            generator.setDataMap(dataMap)
            generator.addEntities(filterAction.getFilteredEntities(dataMap))
            generator.addEmbeddables(dataMap.getEmbeddables())
            generator.addQueries(dataMap.getQueryDescriptors())
            generator.execute()
        } catch (Exception exception) {
            throw new GradleException("Error generating classes: ", exception)
        }
    }

    private def convertAdditionalDataMaps() throws Exception {
        if (additionalMaps == null) {
            return NO_FILES
        }

        if (!additionalMaps.isDirectory()) {
            throw new GradleException("'additionalMaps' must be a directory.")
        }

        FilenameFilter mapFilter = new FilenameFilter() {
            boolean accept(File dir, String name) {
                return name != null &&
                        name.toLowerCase().endsWith(".map.xml")
            }
        }
        return additionalMaps.listFiles(mapFilter)
    }

    private def createGenerator() {
        ClassGenerationAction action
        if (client) {
            action = new ClientClassGenerationAction()
        } else {
            action = new ClassGenerationAction()
        }

        action.setDestDir(destDir)
        action.setEncoding(encoding)
        action.setMakePairs(makePairs)
        action.setArtifactsGenerationMode(mode)
        action.setOutputPattern(outputPattern)
        action.setOverwrite(overwrite)
        action.setSuperPkg(superPkg)
        action.setSuperTemplate(superTemplate)
        action.setTemplate(template)
        action.setEmbeddableSuperTemplate(embeddableSuperTemplate)
        action.setEmbeddableTemplate(embeddableTemplate)
        action.setUsePkgPath(usePkgPath)
        action.setCreatePropertyNames(createPropertyNames)

        return action
    }
}
