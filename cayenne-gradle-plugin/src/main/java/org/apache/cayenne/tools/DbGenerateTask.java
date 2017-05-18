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

import groovy.lang.Closure;
import org.apache.cayenne.access.DbGenerator;
import org.apache.cayenne.datasource.DriverDataSource;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.dba.JdbcAdapter;
import org.apache.cayenne.dbsync.DbSyncModule;
import org.apache.cayenne.dbsync.reverse.configuration.ToolsModule;
import org.apache.cayenne.di.AdhocObjectFactory;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.log.NoopJdbcEventLogger;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.MapLoader;
import org.apache.cayenne.tools.model.DataSourceConfig;
import org.apache.cayenne.util.Util;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.xml.sax.InputSource;

import java.io.File;
import java.sql.Driver;

/**
 * @since 4.0
 */
public class DbGenerateTask extends BaseCayenneTask {

    private String adapter;
    private DataSourceConfig dataSource;
    private boolean dropTables;
    private boolean dropPK;
    private boolean createTables;
    private boolean createPK;
    private boolean createFK;

    public DataSourceConfig dataSource(Closure closure) {
        dataSource = new DataSourceConfig();
        getProject().configure(dataSource, closure);
        return dataSource;
    }

    @TaskAction
    public void generateDb() throws GradleException {

        dataSource.validate();
        File dataMapFile = getDataMapFile();
        Injector injector = DIBootstrap.createInjector(new DbSyncModule(), new ToolsModule(getLogger()));
        AdhocObjectFactory objectFactory = injector.getInstance(AdhocObjectFactory.class);

        getLogger().info(String.format("connection settings - [driver: %s, url: %s, username: %s]",
                dataSource.getDriver(), dataSource.getUrl(), dataSource.getUsername()));

        getLogger().info(String.format(
                "generator options - [dropTables: %s, dropPK: %s, createTables: %s, createPK: %s, createFK: %s]",
                dropTables, dropPK, createTables, createPK, createFK));

        try {
            final DbAdapter adapterInst = (adapter == null) ? objectFactory.newInstance(
                    DbAdapter.class, JdbcAdapter.class.getName()) : objectFactory.newInstance(DbAdapter.class, adapter
            );

            DataMap dataMap = loadDataMap(dataMapFile);
            DbGenerator generator = new DbGenerator(adapterInst, dataMap, NoopJdbcEventLogger.getInstance());
            generator.setShouldCreateFKConstraints(createFK);
            generator.setShouldCreatePKSupport(createPK);
            generator.setShouldCreateTables(createTables);
            generator.setShouldDropPKSupport(dropPK);
            generator.setShouldDropTables(dropTables);

            DriverDataSource driverDataSource = new DriverDataSource((Driver) Class.class.forName(
                    dataSource.getDriver()).newInstance(), dataSource.getUrl(), dataSource.getUsername(),
                    dataSource.getPassword());

            generator.runGenerator(driverDataSource);
        } catch (Exception ex) {
            Throwable th = Util.unwindException(ex);
            String message = "Error generating database";
            if (th.getLocalizedMessage() != null) {
                message += ": " + th.getLocalizedMessage();
            }

            getLogger().error(message);
            throw new GradleException(message, th);
        }


    }

    private DataMap loadDataMap(File dataMapFile) throws Exception {

        InputSource inputSource = new InputSource(dataMapFile.getCanonicalPath());
        return new MapLoader().loadDataMap(inputSource);
    }

    public String getAdapter() {
        return adapter;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    public DataSourceConfig getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceConfig dataSource) {
        this.dataSource = dataSource;
    }

    public boolean getDropTables() {
        return dropTables;
    }

    public boolean isDropTables() {
        return dropTables;
    }

    public void setDropTables(boolean dropTables) {
        this.dropTables = dropTables;
    }

    public boolean getDropPK() {
        return dropPK;
    }

    public boolean isDropPK() {
        return dropPK;
    }

    public void setDropPK(boolean dropPK) {
        this.dropPK = dropPK;
    }

    public boolean getCreateTables() {
        return createTables;
    }

    public boolean isCreateTables() {
        return createTables;
    }

    public void setCreateTables(boolean createTables) {
        this.createTables = createTables;
    }

    public boolean getCreatePK() {
        return createPK;
    }

    public boolean isCreatePK() {
        return createPK;
    }

    public void setCreatePK(boolean createPK) {
        this.createPK = createPK;
    }

    public boolean getCreateFK() {
        return createFK;
    }

    public boolean isCreateFK() {
        return createFK;
    }

    public void setCreateFK(boolean createFK) {
        this.createFK = createFK;
    }
}

