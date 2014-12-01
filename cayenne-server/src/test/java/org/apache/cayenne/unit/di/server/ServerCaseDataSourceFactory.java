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
package org.apache.cayenne.unit.di.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.cayenne.conn.DataSourceInfo;
import org.apache.cayenne.datasource.DataSourceBuilder;
import org.apache.cayenne.di.AdhocObjectFactory;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.log.JdbcEventLogger;

public class ServerCaseDataSourceFactory {

	private DataSource sharedDataSource;
	private DataSourceInfo dataSourceInfo;
	private Map<String, DataSource> dataSources;
	private Set<String> mapsWithDedicatedDataSource;
	private AdhocObjectFactory objectFactory;
	private JdbcEventLogger logger;

	public ServerCaseDataSourceFactory(@Inject DataSourceInfo dataSourceInfo, @Inject AdhocObjectFactory objectFactory,
			@Inject JdbcEventLogger logger) {

		this.logger = logger;
		this.objectFactory = objectFactory;
		this.dataSourceInfo = dataSourceInfo;
		this.dataSources = new HashMap<String, DataSource>();
		this.mapsWithDedicatedDataSource = new HashSet<String>(Arrays.asList("map-db1", "map-db2"));

		this.sharedDataSource = createDataSource();
	}

	public DataSource getSharedDataSource() {
		return sharedDataSource;
	}

	public DataSource getDataSource(String dataMapName) {
		DataSource ds = dataSources.get(dataMapName);
		if (ds == null) {

			ds = mapsWithDedicatedDataSource.contains(dataMapName) ? createDataSource() : sharedDataSource;

			dataSources.put(dataMapName, ds);
		}

		return ds;
	}

	private DataSource createDataSource() {
		return DataSourceBuilder.builder(objectFactory, logger).driver(dataSourceInfo.getJdbcDriver())
				.url(dataSourceInfo.getDataSourceUrl()).userName(dataSourceInfo.getUserName())
				.password(dataSourceInfo.getPassword()).minConnections(dataSourceInfo.getMinConnections())
				.maxConnections(dataSourceInfo.getMaxConnections()).build();
	}

}