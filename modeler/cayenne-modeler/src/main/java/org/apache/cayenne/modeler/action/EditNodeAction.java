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

import org.apache.cayenne.dbsync.reverse.dbimport.FilterContainer;
import org.apache.cayenne.dbsync.reverse.dbimport.PatternParam;
import org.apache.cayenne.modeler.Application;
import org.apache.cayenne.modeler.dialog.db.load.DbImportTreeNode;
import org.apache.cayenne.util.Util;

import java.awt.event.ActionEvent;

/**
 * @since 4.1
 */
public class EditNodeAction extends TreeManipulationAction {

    private static final String ACTION_NAME = "Rename";
    private static final String ICON_NAME = "icon-edit.png";

    public EditNodeAction(Application application) {
        super(ACTION_NAME, application);
    }

    public String getIconName() {
        return ICON_NAME;
    }

    @Override
    public void performAction(ActionEvent e) {
        if (tree.getSelectionPath() != null) {
            selectedElement = (DbImportTreeNode) tree.getSelectionPath().getLastPathComponent();
            parentElement = (DbImportTreeNode) selectedElement.getParent();
            if (parentElement != null) {
                Object selectedObject = this.selectedElement.getUserObject();
                String name = getNewName(selectedElement.getSimpleNodeName());
                if (!Util.isEmptyString(name)) {
                    if (selectedObject instanceof FilterContainer) {
                        ((FilterContainer) selectedObject).setName(name);
                    } else if (selectedObject instanceof PatternParam) {
                        ((PatternParam) selectedObject).setPattern(name);
                    }
                    updateModel();
                }
            }
        }
    }
}
