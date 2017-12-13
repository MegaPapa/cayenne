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
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.ExcludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
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
    private Map<String, List<NodeColorType>> schemaTable;
    private List<NodeColorType> tables;
    private DbImportTree reverseEngineeringTree;
    private DbImportTree dbSchemaTree;

    public DbTreeColorMap(DbImportTree reverseEngineeringTree, DbImportTree dbSchemaTree) {
        this.reverseEngineeringTree = reverseEngineeringTree;
        this.dbSchemaTree = dbSchemaTree;
        catalogsSchema = new HashMap<>();
        schemaTable = new HashMap<>();
        tables = new ArrayList<>();
    }

    public void buildColorMap() {
        clear();
        FiltersConfig reverseEngineeringConfig = createConfig(reverseEngineeringTree);
        FiltersConfig dbReverseEngineeringConfig = createConfig(dbSchemaTree);
        if ((reverseEngineeringConfig == null) || (dbReverseEngineeringConfig == null)) {
            reverseEngineeringTree.startEditingAtPath(reverseEngineeringTree.getSelectionPath()); // STACKOVERFLOW
            return;
        }
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

    public boolean existNode(String parent, String node, Class nodeClass, boolean isInclude) {
        if (node == null) {
            return false;
        }
        /*for (NodeColorType table : tables) {
            System.out.println(table);
            if (nodeClass == ExcludeTable.class) {
                return false;
            }
            if (nodeClass == IncludeTable.class) {
                if (node.matches(table.getNodeName())) {
                    if (table.getParentCatalogName() != null) {
                        if (parent.equals(table.getParentCatalogName())) {
                            System.out.println("<><><><>");
                            return true;
                        }
                    }
                }
            }
        }*/
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
                    for (NodeColorType table : schemaTable.get(schema)) {
                        //System.out.println(table);
                        if ((table.getNodeClass() == ExcludeTable.class) && ((node.matches(table.getNodeName()))) && !isInclude) {
                            if (parent == null) {
                                return false;
                            }
                            if ((schema == null) || (parent.matches(schema))) {
                                return true;
                            }
                        }
                        if (((node.matches(table.getNodeName())) && ((nodeClass == table.getNodeClass())))) {
                            if (parent == null) {
                                return false;
                            }
                            if (catalog.getKey() != null) {
                                if ((catalog.getKey().equals(table.getParentCatalogName()))) {
                                    return true;
                                }
                            } else {
                                if ((schema == null) || (parent.matches(schema))) {
                                    System.out.println(parent + " ----- " + schema);
                                    return true;
                                }
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
        if ((reverseEngineering.getCatalogs().size() == 0) && (reverseEngineering.getSchemas().size() == 0)
                && (reverseEngineering.getIncludeTables().size() == 0) && (reverseEngineering.getExcludeTables().size() == 0)) {
            return true;
        }
        if ((reverseEngineering.getCatalogs().size() == 0) && (reverseEngineering.getSchemas().size() == 0)) {
            for (ExcludeTable excludeTable : reverseEngineering.getExcludeTables()) {

                if (node.matches(excludeTable.getPattern()) && (nodeClass == ExcludeTable.class)) {
                    return false;
                }
            }

            for (IncludeTable includeTable : reverseEngineering.getIncludeTables()) {
                if ((node.matches(includeTable.getPattern())) && (nodeClass == IncludeTable.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clear() {
        catalogsSchema.clear();
        schemaTable.clear();
    }

    private void bypassTables(SchemaFilter schemaFilter, SchemaFilter dbSchemaFilter, CatalogFilter catalogFilter) {
        for (IncludeTableFilter dbIncludeTableFilter : dbSchemaFilter.tables.getIncludes()) {
            if (dbIncludeTableFilter.pattern != null) {
                if (schemaFilter.tables.isIncludeTable(dbIncludeTableFilter.pattern.pattern())) {
                    addSchemaWithTable(dbSchemaFilter.name, dbIncludeTableFilter.pattern.pattern(), catalogFilter.name, IncludeTable.class);
                } else {
                    addSchemaWithTable(dbSchemaFilter.name, dbIncludeTableFilter.pattern.pattern(), catalogFilter.name, ExcludeTable.class);
                }

            }
        }
        for (Pattern dbIncludeFunctionFilter : dbSchemaFilter.procedures.getIncludes()) {
            if (dbIncludeFunctionFilter.pattern() != null) {
                if (schemaFilter.procedures.isIncluded(dbIncludeFunctionFilter.pattern())) {
                    // FUNCTIONS
                    addSchemaWithFunction(dbSchemaFilter.name, dbIncludeFunctionFilter.pattern(), IncludeProcedure.class);
                } else {
                    addSchemaWithFunction(dbSchemaFilter.name, dbIncludeFunctionFilter.pattern(), ExcludeProcedure.class);
                }
            }
        }
    }

    public void addSchemaWithTable(String schemaName, String tableName, String catalogName, Class tableClass) {
        NodeColorType nodeColorType = new NodeColorType();
        nodeColorType.setNodeClass(tableClass);
        nodeColorType.setNodeName(tableName);
        nodeColorType.setParentSchemaName(schemaName);
        nodeColorType.setParentCatalogName(catalogName);
        tables.add(nodeColorType);
        if (schemaTable.get(schemaName) == null) {
            List<NodeColorType> list = new ArrayList<>();
            list.add(nodeColorType);
            schemaTable.put(schemaName, list);
        } else {
            schemaTable.get(schemaName).add(nodeColorType);
        }
    }

    private void addSchemaWithFunction(String schemaName, String functionName, Class functionClass) {
        NodeColorType nodeColorType = new NodeColorType();
        nodeColorType.setNodeClass(functionClass);
        nodeColorType.setNodeName(functionName);
        if (schemaTable.get(schemaName) == null) {
            List<NodeColorType> list = new ArrayList<>();
            list.add(nodeColorType);
            schemaTable.put(schemaName, list);
        } else {
            schemaTable.get(schemaName).add(nodeColorType);
        }
    }

    private void bypassSchema(CatalogFilter catalogFilter, CatalogFilter dbCatalogFilter) {
        for (SchemaFilter schemaFilter : catalogFilter.schemas) {
            for (SchemaFilter dbSchemaFilter : dbCatalogFilter.schemas) {
                if ((isNullSchemaName(schemaFilter, dbSchemaFilter))) {
                    bypassTables(schemaFilter, dbSchemaFilter, dbCatalogFilter);
                } else if ((schemaFilter.name != null) && (dbSchemaFilter.name != null) && (schemaFilter.name.equals(dbSchemaFilter.name))) {
                    addCatalogWithSchema(dbCatalogFilter.name, schemaFilter.name);
                    bypassTables(schemaFilter, dbSchemaFilter, dbCatalogFilter);
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
        try {
            DbImportTreeNode root = (DbImportTreeNode) tree.getModel().getRoot();
            ReverseEngineering newReverseEngineering = new ReverseEngineering((ReverseEngineering) root.getUserObject());
            FiltersConfigBuilder builder = new FiltersConfigBuilder(newReverseEngineering);
            return builder.build();
        } catch (Exception exception) {

        }
        return null;
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
                    for (NodeColorType table : schemaTable.get(schema)) {
                        builder.append("\t\t\t");
                        builder.append(table);
                        builder.append("\n");
                    }
                }
            }
        }
        return builder.toString();
    }

    public boolean isExcludedNode(String node) {
        if (node == null) {
            return false;
        }
        FiltersConfig reverseEngineeringConfig = createConfig(reverseEngineeringTree);
        for (CatalogFilter catalogFilter : reverseEngineeringConfig.getCatalogs()) {
            for (SchemaFilter schemaFilter : catalogFilter.schemas) {
                if (schemaFilter.name != null) {
                    return false;
                }
                if (!schemaFilter.tables.isIncludeTable(node)) {
                    return true;
                }
            }
        }
        return true;
    }

    public boolean hasCatalogsOrSchemas() {
        if ((reverseEngineeringTree.getReverseEngineering().getCatalogs().size() == 0) || (reverseEngineeringTree.getReverseEngineering().getSchemas().size() == 0)) {
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        FiltersConfig reverseEngineeringConfig = createConfig(reverseEngineeringTree);
        if (reverseEngineeringConfig == null) {
            return false;
        }
        if (reverseEngineeringConfig.getCatalogs()[0].schemas[0].tables.getIncludes().first().pattern == null) {
            return true;
        } else {
            return false;
        }
    }

    public DbImportTree getDbSchemaTree() {
        return dbSchemaTree;
    }

    public DbImportTree getReverseEngineeringTree() {
        return reverseEngineeringTree;
    }
}
