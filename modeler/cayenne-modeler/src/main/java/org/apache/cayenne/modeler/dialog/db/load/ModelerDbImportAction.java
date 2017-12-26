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

package org.apache.cayenne.modeler.dialog.db.load;

import org.apache.cayenne.configuration.DataMapLoader;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.DbAdapterFactory;
import org.apache.cayenne.configuration.xml.DataChannelMetaData;
import org.apache.cayenne.dbsync.merge.factory.MergerTokenFactoryProvider;
import org.apache.cayenne.dbsync.merge.token.MergerToken;
import org.apache.cayenne.dbsync.reverse.filters.FiltersConfig;
import org.apache.cayenne.dbsync.reverse.filters.PatternFilter;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.Procedure;
import org.apache.cayenne.project.ProjectSaver;
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportConfiguration;
import org.apache.cayenne.dbsync.reverse.dbimport.DefaultDbImportAction;
import org.slf4j.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ModelerDbImportAction extends DefaultDbImportAction {

    private static final String DIALOG_TITLE = "Reverse Engineering Result";

    @Inject
    private DataMap targetMap;

    private DbLoadResultDialog resultDialog;
    private boolean isNothingChanged;

    public ModelerDbImportAction(@Inject Logger logger,
                                 @Inject ProjectSaver projectSaver,
                                 @Inject DataSourceFactory dataSourceFactory,
                                 @Inject DbAdapterFactory adapterFactory,
                                 @Inject DataMapLoader mapLoader,
                                 @Inject MergerTokenFactoryProvider mergerTokenFactoryProvider,
                                 @Inject DataChannelMetaData metaData) {
        super(logger, projectSaver, dataSourceFactory, adapterFactory, mapLoader, mergerTokenFactoryProvider, metaData);
    }

    @Override
    protected Collection<MergerToken> log(List<MergerToken> tokens) {
        resultDialog = new DbLoadResultDialog(DIALOG_TITLE);
        logger.info("");
        if (tokens.isEmpty()) {
            logger.info("Detected changes: No changes to import.");
            isNothingChanged = true;
            return tokens;
        }

        logger.info("Detected changes: ");
        for (MergerToken token : tokens) {
            String logString = String.format("    %-20s %s", token.getTokenName(), token.getTokenValue());
            logger.info(logString);
            resultDialog.addRowToOutput(logString);
            isNothingChanged = false;
        }

        logger.info("");
        resultDialog.setVisible(true);

        return tokens;
    }

    @Override
    protected boolean syncProcedures(DataMap targetDataMap, DataMap loadedDataMap, FiltersConfig filters) {
        Collection<Procedure> procedures = loadedDataMap.getProcedures();

        boolean hasChanges = false;
        for (Procedure procedure : procedures) {
            PatternFilter proceduresFilter = filters.proceduresFilter(procedure.getCatalog(), procedure.getSchema());
            if (proceduresFilter == null || !proceduresFilter.isIncluded(procedure.getName())) {
                continue;
            }

            Procedure oldProcedure = targetDataMap.getProcedure(procedure.getName());
            // maybe we need to compare oldProcedure's and procedure's fully qualified names?
            if (oldProcedure != null) {
                targetDataMap.removeProcedure(procedure.getName());
                String logString = String.format("    %-20s %s", "Replace procedure ", procedure.getName());
                logger.info(logString);
                resultDialog.addRowToOutput(logString);
            } else {
                String logString = String.format("    %-20s %s", "Add new procedure ", procedure.getName());
                logger.info(logString);
                resultDialog.addRowToOutput(logString);
            }
            targetDataMap.addProcedure(procedure);
            if (!resultDialog.isVisible()) {
                resultDialog.setVisible(true);
            }
            isNothingChanged = false;
            hasChanges = true;
        }
        if ((isNothingChanged)) {
            JOptionPane optionPane = new JOptionPane("Detected changes: No changes to import.", JOptionPane.PLAIN_MESSAGE);
            JDialog dialog = optionPane.createDialog(DIALOG_TITLE);
            dialog.setModal(false);
            dialog.setVisible(true);
        }
        return hasChanges;
    }

    @Override
    protected DataMap existingTargetMap(DbImportConfiguration configuration) throws IOException {
        return targetMap;
    }
}