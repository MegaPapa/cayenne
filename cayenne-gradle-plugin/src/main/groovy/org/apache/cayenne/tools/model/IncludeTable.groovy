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

import org.gradle.util.ConfigureUtil

class IncludeTable extends PatternParam {

    Collection<PatternParam> includeColumns = new LinkedList<>()
    Collection<PatternParam> excludeColumns = new LinkedList<>()

    IncludeTable() {
    }

    IncludeTable(String pattern) {
        super(pattern)
    }

    void name(String name) {
        pattern = name
    }

    void includeColumn(String pattern) {
        includeColumns.add(new PatternParam(pattern))
    }

    void includeColumn(Closure<?> closure) {
        includeColumns.add(ConfigureUtil.configure(closure, new PatternParam()))
    }

    void excludeColumn(String pattern) {
        excludeColumns.add(new PatternParam(pattern))
    }

    void excludeColumn(Closure<?> closure) {
        excludeColumns.add(ConfigureUtil.configure(closure, new PatternParam()))
    }

    org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable toIncludeTable() {
        org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable table =
                new org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable()
        table.pattern = pattern
        includeColumns.each { table.addIncludeColumn(it.toIncludeColumn()) }
        excludeColumns.each { table.addExcludeColumn(it.toExcludeColumn()) }
        return table
    }
}
