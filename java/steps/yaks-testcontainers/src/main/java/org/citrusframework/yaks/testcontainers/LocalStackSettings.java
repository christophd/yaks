/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.yaks.testcontainers;

/**
 * @author Christoph Deppisch
 */
public class LocalStackSettings {

    private static final String LOCALSTACK_PROPERTY_PREFIX = TestContainersSettings.TESTCONTAINERS_PROPERTY_PREFIX + "localstack.";
    private static final String LOCALSTACK_ENV_PREFIX = TestContainersSettings.TESTCONTAINERS_ENV_PREFIX + "LOCALSTACK_";

    private static final String VERSION_PROPERTY = LOCALSTACK_PROPERTY_PREFIX + "version";
    private static final String VERSION_ENV = LOCALSTACK_ENV_PREFIX + "VERSION";
    public static final String VERSION_DEFAULT = "3.3.0";

    private static final String SERVICE_NAME_PROPERTY = LOCALSTACK_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = LOCALSTACK_ENV_PREFIX + "SERVICE_NAME";
    public static final String SERVICE_NAME_DEFAULT = "yaks-localstack";

    private static final String STARTUP_TIMEOUT_PROPERTY = LOCALSTACK_PROPERTY_PREFIX + "startup.timeout";
    private static final String STARTUP_TIMEOUT_ENV = LOCALSTACK_ENV_PREFIX + "STARTUP_TIMEOUT";
    private static final String STARTUP_TIMEOUT_DEFAULT = "180";

    private LocalStackSettings() {
        // prevent instantiation of utility class
    }

    /**
     * LocalStack version.
     * @return default Docker image version.
     */
    public static String getVersion() {
        return System.getProperty(VERSION_PROPERTY,
                System.getenv(VERSION_ENV) != null ? System.getenv(VERSION_ENV) : VERSION_DEFAULT);
    }

    /**
     * LocalStack service name.
     * @return the service name.
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * Time in seconds to wait for the container to startup and accept connections.
     * @return
     */
    public static int getStartupTimeout() {
        return Integer.parseInt(System.getProperty(STARTUP_TIMEOUT_PROPERTY,
                System.getenv(STARTUP_TIMEOUT_ENV) != null ? System.getenv(STARTUP_TIMEOUT_ENV) : STARTUP_TIMEOUT_DEFAULT));
    }
}
