package org.apache.cayenne.tools.tool

import org.gradle.api.GradleException

/**
 * @since 4.0
 */
class DbImportDataSourceConfig {

    private String driver

    /**
     * JDBC connection URL of a target database.
     */
    private String url

    /**
     * Database user name.
     */
    private String username

    /**
     * Database user password.
     */
    private String password

    void validate() throws GradleException {
        if(driver == null && url == null && username == null && password == null) {
            throw new GradleException("Missing <dataSource> configuration.")
        }
        if(driver == null) {
            throw new GradleException("Missing <driver> parameter in <dataSource>.")
        }
        if(url == null) {
            throw new GradleException("Missing <url> parameter in <dataSource>.")
        }
    }

    String getDriver() {
        return driver
    }

    String getUrl() {
        return url
    }

    String getUsername() {
        return username
    }

    String getPassword() {
        return password
    }
}
