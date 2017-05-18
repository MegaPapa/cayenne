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

package org.apache.cayenne.tools.model

import org.gradle.api.InvalidUserDataException

/**
 * @since 4.0
 */
class DataSourceConfig {

    String driver
    String url
    String username
    String password

    def validate() {
        if(driver == null && url == null && username == null && password == null) {
            throw new InvalidUserDataException("Missing dataSource configuration.")
        }

        if(driver == null) {
            throw new InvalidUserDataException("Missing required 'driver' parameter in dataSource.")
        }

        if(url == null) {
            throw new InvalidUserDataException("Missing required 'url' parameter in dataSource.")
        }
    }
}
