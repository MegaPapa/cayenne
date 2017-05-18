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

/**
 * @since 4.0
 */
class FilterContainer {
    String name
    Collection<IncludeTable> includeTables = new LinkedList<>()
    Collection<PatternParam> excludeTables = new LinkedList<>()
    Collection<PatternParam> includeColumns = new LinkedList<>()
    Collection<PatternParam> excludeColumns = new LinkedList<>()
    Collection<PatternParam> includeProcedures = new LinkedList<>()
    Collection<PatternParam> excludeProcedures = new LinkedList<>()

    FilterContainer() {
    }

    FilterContainer(String name) {
        this.name = name
    }

    void name(String name) {
        this.name = name
    }

    void includeTable(String pattern) {
        includeTables.add(new IncludeTable(pattern))
    }

    void includeTable(Closure<?> closure) {
        includeTables.add(ConfigureUtil.configure(closure, new IncludeTable()))
    }

    void excludeTable(String pattern) {
        addToCollection(excludeTables, pattern)
    }

    void excludeTable(Closure<?> closure) {
        addToCollection(excludeTables, closure)
    }

    void includeColumn(String pattern) {
        addToCollection(includeColumns, pattern)
    }

    void includeColumn(Closure<?> closure) {
        addToCollection(includeColumns, closure)
    }

    void excludeColumn(String pattern) {
        addToCollection(excludeColumns, pattern)
    }

    void excludeColumn(Closure<?> closure) {
        addToCollection(excludeColumns, closure)
    }

    void includeProcedure(String pattern) {
        addToCollection(includeProcedures, pattern)
    }

    void includeProcedure(Closure<?> closure) {
        addToCollection(includeProcedures, closure)
    }

    void excludeProcedure(String pattern) {
        addToCollection(excludeProcedures, pattern)
    }

    void excludeProcedure(Closure<?> closure) {
        addToCollection(excludeProcedures, closure)
    }

    private static void addToCollection(Collection<PatternParam> collection, Closure<?> closure) {
        collection.add(ConfigureUtil.configure(closure, new PatternParam()))
    }

    private static void addToCollection(Collection<PatternParam> collection, String name) {
        collection.add(new PatternParam(name))
    }

    public <C extends org.apache.cayenne.dbsync.reverse.dbimport.FilterContainer> C fillContainer(C container) {
        container.name = name
        includeTables.each {container.addIncludeTable(it.toIncludeTable())}
        excludeTables.each {container.addExcludeTable(it.toExcludeTable())}
        includeColumns.each {container.addIncludeColumn(it.toIncludeColumn())}
        excludeColumns.each {container.addExcludeColumn(it.toExcludeColumn())}
        includeProcedures.each {container.addIncludeProcedure(it.toIncludeProcedure())}
        excludeProcedures.each {container.addExcludeProcedure(it.toExcludeProcedure())}
        return container
    }

}
