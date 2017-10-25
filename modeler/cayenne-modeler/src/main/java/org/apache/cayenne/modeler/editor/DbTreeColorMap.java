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

package org.apache.cayenne.modeler.editor;

import org.apache.cayenne.dbsync.reverse.dbimport.Catalog;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;
import org.apache.cayenne.dbsync.reverse.filters.CatalogFilter;
import org.apache.cayenne.dbsync.reverse.filters.FiltersConfig;
import org.apache.cayenne.dbsync.reverse.filters.FiltersConfigBuilder;
import org.apache.cayenne.dbsync.reverse.filters.IncludeTableFilter;
import org.apache.cayenne.dbsync.reverse.filters.SchemaFilter;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @since 4.1
 */
public class DbTreeColorMap {

    private Map<String, List<String>> catalogsSchema;
    private Map<String, List<String>> schemaTable;
    private DbImportTree reverseEngineeringTree;
    private DbImportTree dbSchemaTree;

    public DbTreeColorMap(DbImportTree reverseEngineeringTree, DbImportTree dbSchemaTree) {
        this.reverseEngineeringTree = reverseEngineeringTree;
        this.dbSchemaTree = dbSchemaTree;
        catalogsSchema = new HashMap<>();
        schemaTable = new HashMap<>();
    }

    public void buildColorMap() {
        clear();
        FiltersConfig reverseEngineeringConfig = createConfig(reverseEngineeringTree);
        FiltersConfig dbReverseEngineeringConfig = createConfig(dbSchemaTree);
        for (CatalogFilter catalogFilter : reverseEngineeringConfig.getCatalogs()) {
            for (CatalogFilter dbCatalogFilter : dbReverseEngineeringConfig.getCatalogs()) {
                if ((isNullCatalogName(catalogFilter, dbCatalogFilter))) {
                    bypassSchema(catalogFilter, dbCatalogFilter);
                } else if ((catalogFilter.name != null) && (dbCatalogFilter.name != null) && (catalogFilter.name.equals(dbCatalogFilter.name))) {
                    addCatalogWithSchema(catalogFilter.name, null);
                    bypassSchema(catalogFilter, dbCatalogFilter);
                }
            }
        }
        dbSchemaTree.repaint();
    }

    public void addCatalogWithSchema(String catalogName, String schemaName) {
        if (catalogsSchema.get(catalogName) == null) {
            List<String> list = new ArrayList<>();
            list.add(schemaName);
            catalogsSchema.put(catalogName, list);
        } else {
            if (schemaName != null) {
                catalogsSchema.get(catalogName).add(schemaName);
            }
        }
    }

    public void addSchemaWithTable(String schemaName, String tableName) {
        if (schemaTable.get(schemaName) == null) {
            List<String> list = new ArrayList<>();
            list.add(tableName);
            schemaTable.put(schemaName, list);
        } else {
            schemaTable.get(schemaName).add(tableName);
        }
    }

    public boolean existNode(String parent, String node, Class nodeClass) {
        for (Map.Entry<String, List<String>> catalog : catalogsSchema.entrySet()) {
            if (catalog.getKey() != null) {
                if ((catalog.getKey().equals(node)) && (nodeClass == Catalog.class)) {
                    return true;
                }
            }
            for (String schema : catalog.getValue()) {
                if (schema != null) {
                    if ((schema.equals(node)) && (nodeClass == Schema.class)) {
                        if ((parent == null) || (parent.equals(catalog.getKey()))) {
                            return true;
                        }
                    }
                }
                if (schemaTable.get(schema) != null) {
                    for (String table : schemaTable.get(schema)) {
                        if ((table.equals(node)) && ((nodeClass == IncludeTable.class))) {
                            if ((schema == null) || (parent.equals(schema))) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return inReverseEngineering(node, nodeClass);
    }

    private boolean inReverseEngineering(String node, Class nodeClass) {
        ReverseEngineering reverseEngineering = reverseEngineeringTree.getReverseEngineering();
        if (reverseEngineering.isEmptyContainer()) {
            return true;
        }
        /*for (ExcludeTable excludeTable : reverseEngineering.getExcludeTables()) {
            if (excludeTable.getPattern().equals(node)) {
                return false;
            }
        }
        for (IncludeTable includeTable : reverseEngineering.getIncludeTables()) {
            if ((includeTable.getPattern().equals(node)) && (nodeClass == IncludeTable.class)) {
                return true;
            }
        }*/
        return false;
    }

    public void clear() {
        catalogsSchema.clear();
        schemaTable.clear();
    }

    private void bypassTables(SchemaFilter schemaFilter, SchemaFilter dbSchemaFilter) {
        for (IncludeTableFilter dbIncludeTableFilter : dbSchemaFilter.tables.getIncludes()) {
            if (dbIncludeTableFilter.pattern != null) {
                if (schemaFilter.tables.isIncludeTable(dbIncludeTableFilter.pattern.pattern())) {
                    addSchemaWithTable(dbSchemaFilter.name, dbIncludeTableFilter.pattern.pattern());
                }
            }
        }
        for (Pattern dbIncludeTableFilter : dbSchemaFilter.procedures.getIncludes()) {
            if (dbIncludeTableFilter.pattern() != null) {
                if (schemaFilter.procedures.isIncluded(dbIncludeTableFilter.pattern())) {
                    // FUNCTIONS
                }
            }
        }
    }

    private void bypassSchema(CatalogFilter catalogFilter, CatalogFilter dbCatalogFilter) {
        for (SchemaFilter schemaFilter : catalogFilter.schemas) {
            for (SchemaFilter dbSchemaFilter : dbCatalogFilter.schemas) {
                if ((isNullSchemaName(schemaFilter, dbSchemaFilter))) {
                    bypassTables(schemaFilter, dbSchemaFilter);
                } else if ((schemaFilter.name != null) && (dbSchemaFilter.name != null) && (schemaFilter.name.equals(dbSchemaFilter.name))) {
                    addCatalogWithSchema(dbCatalogFilter.name, dbSchemaFilter.name);
                    bypassTables(schemaFilter, dbSchemaFilter);
                }
            }
        }
    }

    private boolean isNullSchemaName(SchemaFilter firstFilter, SchemaFilter secondFilter) {
        if ((firstFilter == null) || (secondFilter == null)) {
            return false;
        }
        if ((firstFilter.name == null) && (secondFilter.name == null)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNullCatalogName(CatalogFilter firstFilter, CatalogFilter secondFilter) {
        if ((firstFilter == null) || (secondFilter == null)) {
            return false;
        }
        if ((firstFilter.name == null) && (secondFilter.name == null)) {
            return true;
        } else {
            return false;
        }
    }

    private FiltersConfig createConfig(DbImportTree tree) {
        DbImportTreeNode root = (DbImportTreeNode) tree.getModel().getRoot();
        ReverseEngineering newReverseEngineering = new ReverseEngineering((ReverseEngineering) root.getUserObject());
        FiltersConfigBuilder builder = new FiltersConfigBuilder(newReverseEngineering);
        return builder.build();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<String>> catalog : catalogsSchema.entrySet()) {
            builder.append("\t");
            builder.append(catalog.getKey());
            builder.append("\n");
            for (String schema : catalog.getValue()) {
                builder.append("\t\t");
                builder.append(schema);
                builder.append("\n");
                if (schemaTable.get(schema) != null) {
                    for (String table : schemaTable.get(schema)) {
                        builder.append("\t\t\t");
                        builder.append(table);
                        builder.append("\n");
                    }
                }
            }
        }
        return builder.toString();
    }
}
