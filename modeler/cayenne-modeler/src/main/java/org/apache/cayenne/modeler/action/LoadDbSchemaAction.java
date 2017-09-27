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

package org.apache.cayenne.modeler.action;

import org.apache.cayenne.dbsync.reverse.dbimport.Catalog;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.Application;
import org.apache.cayenne.modeler.dialog.db.DataSourceWizard;
import org.apache.cayenne.modeler.editor.DraggableTreePanel;
import org.apache.cayenne.modeler.util.CayenneAction;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @since 4.1
 */
public class LoadDbSchemaAction extends CayenneAction {

    private static final String ICON_NAME = "icon-sync.png";
    private static final String ACTION_NAME = "Load Db Schema";
    private static final String INCLUDE_ALL_PATTERN = "%";
    private static final int TABLE_INDEX = 3;
    private static final int SCHEMA_INDEX = 2;
    private static final int CATALOG_INDEX = 1;

    private ReverseEngineering databaseReverseEngineering;
    private DraggableTreePanel draggableTreePanel;

    public LoadDbSchemaAction(Application application) {
        super(ACTION_NAME, application);
    }

    public String getIconName() {
        return ICON_NAME;
    }

    @Override
    public void performAction(ActionEvent e) {
        DataSourceWizard connectWizard = new DataSourceWizard(getProjectController(), "Load Db Schema");
        if (!connectWizard.startupAction()) {
            return;
        }

        DataMap map = getProjectController().getCurrentDataMap();
        databaseReverseEngineering = new ReverseEngineering();

        try(Connection connection = connectWizard.getDataSource().getConnection()) {
            String[] types = {"TABLE"};
            ResultSet resultSet = connection.getMetaData().getTables(null, null, INCLUDE_ALL_PATTERN, types);
            while (resultSet.next()) {
                String tableName = resultSet.getString(TABLE_INDEX);
                String schemaName = resultSet.getString(SCHEMA_INDEX);
                String catalogName = resultSet.getString(CATALOG_INDEX);
                packTable(tableName, catalogName, schemaName);
            }
            packFunctions(connection);
            draggableTreePanel.getSourceTree().setEnabled(true);
            draggableTreePanel.getSourceTree().translateReverseEngineeringToTree(databaseReverseEngineering, true);
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    Application.getFrame(),
                    exception.getMessage(),
                    "Error db schema loading",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void packFunctions(Connection connection) throws SQLException {
        Collection<Catalog> catalogs = databaseReverseEngineering.getCatalogs();
        for (Catalog catalog : catalogs) {
            ResultSet funcResultSet = connection.getMetaData().getFunctions(catalog.getName(), null, "%");
            while (funcResultSet.next()) {
                IncludeProcedure includeProcedure = new IncludeProcedure(funcResultSet.getString(3));
                catalog.addIncludeProcedure(includeProcedure);
            }
        }
        for (Schema schema : databaseReverseEngineering.getSchemas()) {
            ResultSet funcResultSet = connection.getMetaData().getFunctions(null, schema.getName(), "%");
            while (funcResultSet.next()) {
                IncludeProcedure includeProcedure = new IncludeProcedure(funcResultSet.getString(3));
                schema.addIncludeProcedure(includeProcedure);
            }
        }
        for (Catalog catalog : catalogs) {
            for (Schema schema : catalog.getSchemas()) {
                ResultSet funcResultSet = connection.getMetaData().getFunctions(catalog.getName(), schema.getName(), "%");
                while (funcResultSet.next()) {
                    IncludeProcedure includeProcedure = new IncludeProcedure(funcResultSet.getString(3));
                    schema.addIncludeProcedure(includeProcedure);
                }
            }
        }
    }

    private void packTable(String tableName, String catalogName, String schemaName) {
        IncludeTable newTable = new IncludeTable();
        newTable.setPattern(tableName);
        if ((catalogName == null) && (schemaName == null)) {
            databaseReverseEngineering.addIncludeTable(newTable);
        }
        if ((catalogName != null) && (schemaName == null)) {
            Catalog parentCatalog = getCatalogByName(databaseReverseEngineering.getCatalogs(), catalogName);
            if (parentCatalog != null) {
                parentCatalog.addIncludeTable(newTable);
            } else {
                parentCatalog = new Catalog();
                parentCatalog.setName(catalogName);
                parentCatalog.addIncludeTable(newTable);
                databaseReverseEngineering.addCatalog(parentCatalog);
            }
        }
        if ((catalogName == null) && (schemaName != null)) {
            Schema parentSchema = getSchemaByName(databaseReverseEngineering.getSchemas(), schemaName);
            if (parentSchema != null) {
                parentSchema.addIncludeTable(newTable);
            } else {
                parentSchema = new Schema();
                parentSchema.setName(schemaName);
                parentSchema.addIncludeTable(newTable);
                databaseReverseEngineering.addSchema(parentSchema);
            }
        }
        if ((catalogName != null) && (schemaName != null)) {
            Catalog parentCatalog = getCatalogByName(databaseReverseEngineering.getCatalogs(), catalogName);
            Schema parentSchema;
            if (parentCatalog != null) {
                parentSchema = getSchemaByName(parentCatalog.getSchemas(), schemaName);
                if (parentSchema != null) {
                    parentSchema.addIncludeTable(newTable);
                } else {
                    parentSchema = new Schema();
                    parentSchema.setName(schemaName);
                    parentSchema.addIncludeTable(newTable);
                    parentCatalog.addSchema(parentSchema);
                }
            } else {
                parentCatalog = new Catalog();
                parentCatalog.setName(catalogName);
                parentSchema = new Schema();
                parentSchema.setName(schemaName);
                parentSchema.addIncludeTable(newTable);
                databaseReverseEngineering.addCatalog(parentCatalog);
            }
        }
    }

    private Catalog getCatalogByName(Collection<Catalog> catalogs, String catalogName) {
        for (Catalog catalog : catalogs) {
            if (catalog.getName().equals(catalogName)) {
                return catalog;
            }
        }
        return null;
    }

    private Schema getSchemaByName(Collection<Schema> schemas, String schemaName) {
        for (Schema schema : schemas) {
            if (schema.getName().equals(schemaName)) {
                return schema;
            }
        }
        return null;
    }

    public void setDraggableTreePanel(DraggableTreePanel draggableTreePanel) {
        this.draggableTreePanel = draggableTreePanel;
    }
}
