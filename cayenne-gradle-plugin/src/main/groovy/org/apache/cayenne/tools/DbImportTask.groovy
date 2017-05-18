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

import groovy.transform.CompileStatic
import org.apache.cayenne.dbsync.DbSyncModule
import org.apache.cayenne.dbsync.reverse.configuration.ToolsModule
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportAction
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportConfiguration
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportConfigurationValidator
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportModule
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering
import org.apache.cayenne.dbsync.reverse.filters.FiltersConfigBuilder
import org.apache.cayenne.di.DIBootstrap
import org.apache.cayenne.di.Injector
import org.apache.cayenne.tools.model.DataSourceConfig
import org.apache.cayenne.tools.model.DbImportConfig
import org.apache.cayenne.util.Util
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.slf4j.Logger

/**
 * @since 4.0
 */
@CompileStatic
class DbImportTask extends BaseCayenneTask {

    DataSourceConfig dataSource = new DataSourceConfig()

    DbImportConfig config = new DbImportConfig()

    String adapter

    private ReverseEngineering reverseEngineering

    @TaskAction
    def runImport() {
        dataSource.validate()
        reverseEngineering = config.toReverseEngineering()

        logger.warn(reverseEngineering.toString())

        DbImportConfiguration config = createConfig(logger)
        Injector injector = DIBootstrap.createInjector(new DbSyncModule(),
                new ToolsModule(logger), new DbImportModule())

        DbImportConfigurationValidator validator = new DbImportConfigurationValidator(reverseEngineering,
                config, injector)
        try {
            validator.validate()
        } catch (Exception ex) {
            throw new TaskExecutionException(this, ex)
        }

        try {
            injector.getInstance(DbImportAction.class).execute(config)
        } catch (Exception ex) {
            Throwable th = Util.unwindException(ex)

            String message = "Error importing database schema"

            if (th.getLocalizedMessage() != null) {
                message += ": " + th.getLocalizedMessage()
            }

            logger.error(message)
            throw new GradleException(message, th)
        }
    }

    DbImportConfiguration createConfig(Logger logger) {

        DbImportConfiguration config = new DbImportConfiguration()
        config.setAdapter(adapter)
        config.setDefaultPackage(reverseEngineering.getDefaultPackage())
        config.setDriver(dataSource.getDriver())
        config.setFiltersConfig(new FiltersConfigBuilder(reverseEngineering).build())
        config.setForceDataMapCatalog(reverseEngineering.isForceDataMapCatalog())
        config.setForceDataMapSchema(reverseEngineering.isForceDataMapSchema())
        config.setLogger(logger)
        config.setMeaningfulPkTables(reverseEngineering.getMeaningfulPkTables())
        config.setNamingStrategy(reverseEngineering.getNamingStrategy())
        config.setPassword(dataSource.getPassword())
        config.setSkipRelationshipsLoading(reverseEngineering.getSkipRelationshipsLoading())
        config.setSkipPrimaryKeyLoading(reverseEngineering.getSkipPrimaryKeyLoading())
        config.setStripFromTableNames(reverseEngineering.getStripFromTableNames())
        config.setTableTypes(reverseEngineering.getTableTypes())
        config.setTargetDataMap(getDataMapFile())
        config.setUrl(dataSource.getUrl())
        config.setUsername(dataSource.getUsername())
        config.setUsePrimitives(reverseEngineering.isUsePrimitives())
        config.setUseJava7Types(reverseEngineering.isUseJava7Types())

        return config
    }

    void dbImport(Closure<?> closure) {
        getProject().configure(config, closure)
    }

    void dbimport(Closure<?> closure) {
        dbImport(closure)
    }

    void dataSource(Closure<?> closure) {
        getProject().configure(dataSource, closure)
    }

}
