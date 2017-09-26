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

import org.apache.cayenne.dbsync.reverse.dbimport.Catalog;
import org.apache.cayenne.dbsync.reverse.dbimport.FilterContainer;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeProcedure;
import org.apache.cayenne.dbsync.reverse.dbimport.IncludeTable;
import org.apache.cayenne.dbsync.reverse.dbimport.PatternParam;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.dbimport.Schema;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @since 4.1
 */
public class TransferableNode extends DbImportTreeNode implements Transferable {

    private static final DataFlavor catalogFlavor = new DataFlavor(Catalog.class, Catalog.class.getSimpleName());
    private static final DataFlavor schemaFlavor = new DataFlavor(Schema.class, Schema.class.getSimpleName());
    private static final DataFlavor includeTableFlavor = new DataFlavor(IncludeTable.class, IncludeTable.class.getSimpleName());
    private static final DataFlavor patternParamFlavor = new DataFlavor(PatternParam.class, PatternParam.class.getSimpleName());
    private static final DataFlavor[] flavors = new DataFlavor[] { catalogFlavor, schemaFlavor,
                                                                    includeTableFlavor, patternParamFlavor };

    public TransferableNode(Object userObject) {
        this.userObject = userObject;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor dataFlavor : flavors) {
            if (flavor.equals(dataFlavor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return userObject;
        } else {
            return null;
        }
    }

    public String getNodeName() {
        if (userObject instanceof FilterContainer) {
            return getFormattedName(userObject.getClass().getSimpleName(), ((FilterContainer) userObject).getName());
        } else if (userObject instanceof IncludeTable) {
            return getFormattedName("Table", ((PatternParam) userObject).getPattern());
        } else if (userObject instanceof IncludeProcedure) {
            return getFormattedName("Procedure", ((PatternParam) userObject).getPattern());
        }
        return "";
    }

    public String toString() {
        if (userObject == null) {
            return "";
        } else if (userObject instanceof ReverseEngineering) {
            return "Database:";
        } else {
            return getNodeName();
        }
    }
}