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
import org.apache.cayenne.gen.ClassGenerationAction
import org.apache.cayenne.gen.ClientClassGenerationAction
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

/**
 * @since 4.0
 */
class CgenTask extends BaseCayenneTask {

    private static final File[] NO_FILES = new File[0]

    File additionalMaps

    boolean client

    File destDir

    String encoding

    String excludeEntities

    String includeEntities

    boolean makePairs

    String mode

    String outputPattern

    boolean overwrite

    String superPkg

    String superTemplate

    String template

    String embeddableSuperTemplate

    String embeddableTemplate

    boolean usePkgPath

    boolean createPropertyNames

    private String destDirName

    CgenTask() {
        client = false
        makePairs = true
        overwrite = false
        usePkgPath = true
        createPropertyNames = false
        mode = "entity"
        outputPattern = "*.java"
    }

    //тестируем то, что из gradle.build нормально заполнились поля
    //тестируем то, что из заполненных полей нормально заполнился генератор
    //тестируем то, что создаётся нормальная структура проекта с нужными файлами
    @TaskAction
    def generate() {
        File dataMapFile = getDataMapFile()
        def loaderAction = new CayenneGeneratorMapLoaderAction()
        loaderAction.setMainDataMapFile(dataMapFile)

        def filterAction = new CayenneGeneratorEntityFilterAction()
        filterAction.setClient(client)
        filterAction.setNameFilter(NamePatternMatcher.build(logger, includeEntities, excludeEntities))

        try {
            loaderAction.setAdditionalDataMapFiles(convertAdditionalDataMaps())

            def generator = this.createGenerator()
            def dataMap = loaderAction.getMainDataMap()

            generator.setLogger(logger)
            generator.setTimestamp(dataMapFile.lastModified())
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
                return name != null && name.toLowerCase().endsWith(".map.xml")
            }
        }
        return additionalMaps.listFiles(mapFilter)
    }

    private def createGenerator() {
        def action = client ? new ClientClassGenerationAction() : new ClassGenerationAction()

        action.setDestDir(getDestDirFile())
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

    protected File getDestDirFile() {
        File javaSourceDir = null

        if(destDir != null) {
            javaSourceDir = destDir
        } else if(destDirName != null) {
            javaSourceDir = getProject().file(destDirName)
        } else {
            getProject().getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
                @Override
                void execute(final JavaPlugin plugin) {
                    SourceSetContainer sourceSets = project.sourceSets

                    def sourceDirs = sourceSets.getByName("main").java.sourceDirectories?.files
                    if(!sourceDirs?.empty) {
                        // find java directory, if there is no such dir, take first
                        javaSourceDir = sourceDirs.find {it.name.endsWith('java')}
                        if(javaSourceDir == null) {
                            javaSourceDir = sourceDirs.first()
                        }
                    }
                }
            })
        }

        if(javaSourceDir == null) {
            throw new InvalidUserDataException("cgen.destDir is not set and there is no Java source sets found.")
        }

        if (!javaSourceDir.exists()) {
            javaSourceDir.mkdirs()
        }

        return javaSourceDir
    }

    void setDestDir(String destDir) {
        this.destDirName = destDir
    }

}
