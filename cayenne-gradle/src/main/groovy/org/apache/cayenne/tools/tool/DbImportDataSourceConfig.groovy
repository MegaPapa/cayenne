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

package org.apache.cayenne.tools.tool

import org.gradle.api.GradleException

/**
 * @since 4.0
 */
class DbImportDataSourceConfig {

    DbImportDataSourceConfig() {

    }

    String driver

    /**
     * JDBC connection URL of a target database.
     */
    String url

    /**
     * Database user name.
     */
    String username

    /**
     * Database user password.
     */
    String password

    void validate() throws GradleException {
        if(driver == null && url == null && username == null && password == null) {
            throw new GradleException("Missing <dataSource> configuration.")
        }
        if(driver == null) {
            throw new GradleException("Missing <driver> parameter in <dataSource>.")
        }
        if(url == null) {
            throw new GradleException("Missing <url> parameter in <dataSource>.")
        }
    }

    String getDriver() {
        return driver
    }

    String getUrl() {
        return url
    }

    String getUsername() {
        return username
    }

    String getPassword() {
        return password
    }
}
