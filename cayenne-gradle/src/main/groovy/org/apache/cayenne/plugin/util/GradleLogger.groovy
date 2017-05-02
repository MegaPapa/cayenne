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

package org.apache.cayenne.plugin.util

import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.slf4j.Marker
import org.gradle.api.logging.Logger

/**
 * @since 4.0
 */
class GradleLogger implements org.slf4j.Logger {

    private Logger logger;

    GradleLogger(DefaultTask task) {
        this.logger = task.logger
    }
    
    String getName() {
        return logger.getName()
    }

    boolean isTraceEnabled() {
        return logger.isEnabled(LogLevel.QUIET)
    }

    void trace(String message) {
        logger.trace(message)
    }

    void trace(String message, Object object) {
        logger.trace(message, object)
    }

    void trace(String message, Object object, Object secondObject) {
        logger.trace(message, object, secondObject)
    }

    void trace(String message, Object... objects) {
        logger.trace(message, objects)
    }

    void trace(String message, Throwable throwable) {
        logger.trace(message, throwable)
    }

    boolean isTraceEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.QUIET)
    }

    void trace(Marker marker, String message) {
        logger.trace(message)
    }

    void trace(Marker marker, String message, Object object) {
        logger.trace(message, object)
    }

    void trace(Marker marker, String message, Object object, Object secondObject) {
        logger.trace(message, object, secondObject)
    }

    void trace(Marker marker, String message, Object... objects) {
        logger.trace(message, objects)
    }

    void trace(Marker marker, String message, Throwable throwable) {
        logger.trace(message, throwable)
    }

    boolean isDebugEnabled() {
        return logger.isEnabled(LogLevel.DEBUG)
    }

    void debug(String message) {
        logger.debug(message)
    }

    void debug(String message, Object object) {
        logger.debug(message, object)
    }

    void debug(String message, Object object, Object secondObject) {
        logger.debug(message, object, secondObject)
    }

    void debug(String message, Object... objects) {
        logger.debug(message, objects)
    }

    void debug(String message, Throwable throwable) {
        logger.debug(message, throwable)
    }

    boolean isDebugEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.DEBUG)
    }

    void debug(Marker marker, String message) {
        logger.debug(message)
    }

    void debug(Marker marker, String message, Object object) {
        logger.debug(message, object)
    }

    void debug(Marker marker, String message, Object object, Object secondObject) {
        logger.debug(message, object, secondObject)
    }

    void debug(Marker marker, String message, Object... objects) {
        logger.debug(message, objects)
    }

    void debug(Marker marker, String message, Throwable throwable) {
        logger.debug(message, throwable)
    }

    boolean isInfoEnabled() {
        return logger.isEnabled(LogLevel.INFO)
    }

    void info(String message) {
        logger.info(message)
    }

    void info(String message, Object object) {
        logger.info(message, object)
    }

    void info(String message, Object object, Object secondObject) {
        logger.info(message, object, secondObject)
    }

    void info(String message, Object... objects) {
        logger.info(message, objects)
    }

    void info(String message, Throwable throwable) {
        logger.info(message, throwable)
    }

    boolean isInfoEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.INFO)
    }

    void info(Marker marker, String message) {
        logger.info(message)
    }

    void info(Marker marker, String message, Object object) {
        logger.info(message, object)
    }

    void info(Marker marker, String message, Object object, Object secondObject) {
        logger.info(message, object, secondObject)
    }

    void info(Marker marker, String message, Object... objects) {
        logger.info(message, objects)
    }

    void info(Marker marker, String message, Throwable throwable) {
        logger.info(message, throwable)
    }

    boolean isWarnEnabled() {
        return logger.isEnabled(LogLevel.WARN)
    }

    void warn(String message) {
        logger.warn(message)
    }

    void warn(String message, Object object) {
        logger.warn(message, object)
    }

    void warn(String message, Object... objects) {
        logger.warn(message, objects)
    }

    void warn(String message, Object object, Object secondObject) {
        logger.warn(message, object, secondObject)
    }

    void warn(String message, Throwable throwable) {
        logger.warn(message, throwable)
    }

    boolean isWarnEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.WARN)
    }

    void warn(Marker marker, String message) {
        logger.warn(message)
    }

    void warn(Marker marker, String message, Object object) {
        logger.warn(message, object)
    }

    void warn(Marker marker, String message, Object object, Object secondObject) {
        logger.warn(message, object, secondObject)
    }

    void warn(Marker marker, String message, Object... objects) {
        logger.warn(message, objects)
    }

    void warn(Marker marker, String message, Throwable throwable) {
        logger.warn(message, throwable)
    }

    boolean isErrorEnabled() {
        return logger.isEnabled(LogLevel.ERROR)
    }

    void error(String message) {
        logger.error(message)
    }

    void error(String message, Object object) {
        logger.error(message, object)
    }

    void error(String message, Object object, Object secondObject) {
        logger.error(message, object, secondObject)
    }

    void error(String message, Object... objects) {
        logger.error(message, objects)
    }

    void error(String message, Throwable throwable) {
        logger.error(message, throwable)
    }

    boolean isErrorEnabled(Marker marker) {
        return logger.isEnabled(LogLevel.ERROR)
    }

    void error(Marker marker, String message) {
        logger.error(message)
    }

    void error(Marker marker, String message, Object object) {
        logger.error(message, object)
    }

    void error(Marker marker, String message, Object object, Object secondObject) {
        logger.error(message, object, secondObject)
    }

    void error(Marker marker, String message, Object... objects) {
        logger.error(message, objects)
    }

    void error(Marker marker, String message, Throwable throwable) {
        logger.error(message, throwable)
    }
}
