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

package org.apache.cayenne.tools

import org.apache.cayenne.tools.tool.CgenConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @since 4.0
 */
class CayennePlugin implements Plugin<Project> {

    void apply(Project project) {
        //NamedDomainObjectContainer<CustomObject> customObjectContainer = project.container(CustomObject)
        //project.extensions.add('customObj', customObjectContainer)

        //NamedDomainObjectContainer<CgenConfigExtension> cgenConfigExtensions = project.container(CgenConfigExtension)
        //project.extensions.add('cgenConfig', cgenConfigExtensions)
        //project.extensions.create("cgenConfig", CgenConfigExtension, project)

        CgenConfigExtension cgenExtension = project.extensions.create('cgenConfig', CgenConfigExtension)
        CgenConfigExtension cdbgenExtension = project.extensions.create('cdbgenConfig', CgenConfigExtension)
        //extension.hardObject = project.getExtensions().create('hardObject', HardObject)


        project.task('cgen', type: CayenneGeneratorTask)
        project.task('cdbgen', type: DbGeneratorTask)
        project.task('cdbimport', type: DbImporterTask)
    }
}
