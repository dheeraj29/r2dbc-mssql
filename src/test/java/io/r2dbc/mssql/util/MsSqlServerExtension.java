/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.r2dbc.mssql.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;

import java.io.IOException;
import java.net.Socket;

/**
 * Test container extension for Microsoft SQL Server.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class MsSqlServerExtension implements BeforeAllCallback, AfterAllCallback {

    private final MSSQLServerContainer<?> container = new MSSQLServerContainer() {

        protected void configure() {
            this.addExposedPort(MS_SQL_SERVER_PORT);
            this.addEnv("ACCEPT_EULA", "Y");
            this.addEnv("SA_PASSWORD", getPassword());
        }
    };

    private final DatabaseContainer sqlServer = External.INSTANCE.isAvailable() ? External.INSTANCE : new TestContainer(container);

    private final boolean useTestContainer = sqlServer instanceof TestContainer;

    @Override
    public void beforeAll(ExtensionContext context) {

        if (this.useTestContainer) {
            this.container.start();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {

        if (this.useTestContainer) {
            this.container.stop();
        }
    }

    public String getHost() {
        return this.sqlServer.getHost();
    }

    public String getPassword() {
        return this.container.getPassword();
    }

    public int getPort() {
        return this.sqlServer.getPort();
    }

    public String getUsername() {
        return this.container.getUsername();
    }

    /**
     * Interface to be implemented by database providers (provided database, test container).
     */
    interface DatabaseContainer {

        String getHost();

        int getPort();
    }

    /**
     * Externally provided SQL Server instance.
     */
    static class External implements DatabaseContainer {

        public static final External INSTANCE = new External();

        @Override
        public String getHost() {
            return "localhost";
        }

        @Override
        public int getPort() {
            return 1433;
        }

        /**
         * Returns whether this container is available.
         *
         * @return
         */
        @SuppressWarnings("try")
        public boolean isAvailable() {

            try (Socket ignored = new Socket(getHost(), getPort())) {

                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /**
     * {@link DatabaseContainer} provided by {@link JdbcDatabaseContainer}.
     */
    static class TestContainer implements DatabaseContainer {

        private final JdbcDatabaseContainer<?> container;

        TestContainer(JdbcDatabaseContainer<?> container) {
            this.container = container;
        }

        @Override
        public String getHost() {
            return this.container.getContainerIpAddress();
        }

        @Override
        public int getPort() {
            return this.container.getMappedPort(MSSQLServerContainer.MS_SQL_SERVER_PORT);
        }
    }
}
