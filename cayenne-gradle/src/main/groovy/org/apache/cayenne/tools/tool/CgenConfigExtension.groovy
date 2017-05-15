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

package org.apache.cayenne.tools.tool

/**
 * @since 4.0
 */
class CgenConfigExtension {

    static String additionalMaps

    static boolean client = false

    static String destDir

    static String encoding

    static String excludeEntities

    static String includeEntities

    static boolean makePairs = true

    static String map

    static String mode = "entity"

    static String outputPattern = "*.java"

    static boolean overwrite = false

    static String superPkg

    static String superTemplate

    static String template

    static String embeddableSuperTemplate

    static String embeddableTemplate

    static boolean usePkgPath = true

    static boolean createPropertyNames = false
}
