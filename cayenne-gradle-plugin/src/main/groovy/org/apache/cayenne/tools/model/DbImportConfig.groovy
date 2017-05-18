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

import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering
import org.apache.cayenne.dbsync.reverse.dbimport.Schema
import org.gradle.util.ConfigureUtil

/**
 * @since 4.0
 */
class DbImportConfig extends SchemaContainer {

    boolean skipRelationshipsLoading

    boolean skipPrimaryKeyLoading

    /*
     * <p>
     * A default package for ObjEntity Java classes.
     * </p>
     * <p>
     * If not specified, and the existing DataMap already has the default package,
     * the existing package will be used.
     * </p>
     */
    String defaultPackage

    /**
     * <p>
     * Automatically tagging each DbEntity with the actual DB catalog/schema (default behavior) may sometimes be undesirable.
     * If this is the case then setting forceDataMapCatalog to true will set DbEntity catalog to one in the DataMap.
     * </p>
     * <p>
     * Default value is <b>false</b>.
     * </p>
     */
    boolean forceDataMapCatalog

    /**
     * <p>
     * Automatically tagging each DbEntity with the actual DB catalog/schema (default behavior) may sometimes be undesirable.
     * If this is the case then setting forceDataMapSchema to true will set DbEntity schema to one in the DataMap.
     * </p>
     * <p>
     * Default value is <b>false</b>.
     * </p>
     */
    boolean forceDataMapSchema

    /**
     * <p>
     * A comma-separated list of Perl5 patterns that defines which imported tables should have their primary key columns
     * mapped as ObjAttributes.
     * </p>
     * <p><b>"*"</b> would indicate all tables.</p>
     */
    String meaningfulPkTables

    /**
     * <p>
     * Object layer naming generator implementation.
     * Should be fully qualified Java class name implementing "org.apache.cayenne.dbsync.naming.ObjectNameGenerator".
     * </p>
     * <p>
     * The default is "org.apache.cayenne.dbsync.naming.DefaultObjectNameGenerator".
     * </p>
     */
    String namingStrategy = "org.apache.cayenne.dbsync.naming.DefaultObjectNameGenerator"

    /**
     * A regular expression that should match the part of the table name to strip before generating DB names.
     */
    String stripFromTableNames = ""

    /**
     * <p>If true, would use primitives instead of numeric and boolean classes.</p>
     * <p>Default is <b>"true"</b>, i.e. primitives will be used.</p>
     */
    boolean usePrimitives = true

    /**
     * Use old Java 7 date types
     */
    boolean useJava7Types = false

    /**
     * Typical types are: <ul>
     * <li> "TABLE"
     * <li> "VIEW"
     * <li> "SYSTEM TABLE"
     * <li> "GLOBAL TEMPORARY",
     * <li> "LOCAL TEMPORARY"
     * <li> "ALIAS"
     * <li> "SYNONYM"
     * </ul>
     */
    Collection<String> tableTypes = new LinkedList<>()

    Collection<SchemaContainer> catalogs = new LinkedList<>()

    void catalog(String name) {
        catalogs.add(new SchemaContainer(name))
    }

    void catalog(Closure<?> closure) {
        catalogs.add(ConfigureUtil.configure(closure, new SchemaContainer()))
    }

    ReverseEngineering toReverseEngineering() {
        ReverseEngineering reverseEngineering = new ReverseEngineering()
        reverseEngineering.skipRelationshipsLoading = skipRelationshipsLoading
        reverseEngineering.skipPrimaryKeyLoading = skipPrimaryKeyLoading
        reverseEngineering.forceDataMapCatalog = forceDataMapCatalog
        reverseEngineering.forceDataMapSchema = forceDataMapSchema
        reverseEngineering.defaultPackage = defaultPackage
        reverseEngineering.meaningfulPkTables = meaningfulPkTables
        reverseEngineering.namingStrategy = namingStrategy
        reverseEngineering.stripFromTableNames = stripFromTableNames
        reverseEngineering.usePrimitives = usePrimitives
        reverseEngineering.useJava7Types = useJava7Types
        reverseEngineering.tableTypes = tableTypes

        catalogs.each {reverseEngineering.addCatalog(it.toCatalog())}
        schemas.each {reverseEngineering.addSchema(it.fillContainer(new Schema()))}

        fillContainer(reverseEngineering)

        return reverseEngineering
    }
}
