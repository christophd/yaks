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

package org.citrusframework.yaks.kubernetes;

import org.citrusframework.yaks.YaksSettings;

/**
 * @author Christoph Deppisch
 */
public class KubernetesSettings {

    private static final String KUBERNETES_PROPERTY_PREFIX = "citrus.kubernetes.";
    private static final String KUBERNETES_ENV_PREFIX = "CITRUS_KUBERNETES_";

    private static final String SERVICE_TIMEOUT_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "service.timeout";
    private static final String SERVICE_TIMEOUT_ENV = KUBERNETES_ENV_PREFIX + "SERVICE_TIMEOUT";
    private static final String SERVICE_TIMEOUT_DEFAULT = "2000";

    private static final String NAMESPACE_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "namespace";
    private static final String NAMESPACE_ENV = KUBERNETES_ENV_PREFIX + "NAMESPACE";

    private static final String API_VERSION_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "api.version";
    private static final String API_VERSION_ENV = KUBERNETES_ENV_PREFIX + "API_VERSION";
    private static final String API_VERSION_DEFAULT = "v1";

    private static final String SERVICE_NAME_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = KUBERNETES_ENV_PREFIX + "SERVICE_NAME";
    private static final String SERVICE_NAME_DEFAULT = "yaks-k8s-service";

    private static final String SERVICE_PORT_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "service.port";
    private static final String SERVICE_PORT_ENV = KUBERNETES_ENV_PREFIX + "SERVICE_PORT";
    private static final String SERVICE_PORT_DEFAULT = "8080";

    private static final String AUTO_REMOVE_RESOURCES_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "auto.remove.resources";
    private static final String AUTO_REMOVE_RESOURCES_ENV = KUBERNETES_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    private static final String AUTO_REMOVE_RESOURCES_DEFAULT = "false";

    private static final String MAX_ATTEMPTS_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "max.attempts";
    private static final String MAX_ATTEMPTS_ENV = KUBERNETES_ENV_PREFIX + "MAX_ATTEMPTS";
    private static final String MAX_ATTEMPTS_DEFAULT = "150";

    private static final String DELAY_BETWEEN_ATTEMPTS_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "delay.between.attempts";
    private static final String DELAY_BETWEEN_ATTEMPTS_ENV = KUBERNETES_ENV_PREFIX + "DELAY_BETWEEN_ATTEMPTS";
    private static final String DELAY_BETWEEN_ATTEMPTS_DEFAULT = "2000";

    private static final String PRINT_POD_LOGS_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "print.pod.logs";
    private static final String PRINT_POD_LOGS_ENV = KUBERNETES_ENV_PREFIX + "PRINT_POD_LOGS";
    private static final String PRINT_POD_LOGS_DEFAULT = "true";

    private static final String WATCH_LOGS_TIMEOUT_PROPERTY = KUBERNETES_PROPERTY_PREFIX + "watch.logs.timeout";
    private static final String WATCH_LOGS_TIMEOUT_ENV = KUBERNETES_ENV_PREFIX + "WATCH_LOGS_TIMEOUT";
    private static final String WATCH_LOGS_TIMEOUT_DEFAULT = "60000";

    private KubernetesSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Request timeout when receiving cloud events.
     * @return
     */
    public static long getServiceTimeout() {
        return Long.parseLong(System.getProperty(SERVICE_TIMEOUT_PROPERTY,
                System.getenv(SERVICE_TIMEOUT_ENV) != null ? System.getenv(SERVICE_TIMEOUT_ENV) : SERVICE_TIMEOUT_DEFAULT));
    }

    /**
     * Namespace to work on when performing Kubernetes client operations such as creating triggers, services and so on.
     * @return
     */
    public static String getNamespace() {
        return System.getProperty(NAMESPACE_PROPERTY,
                System.getenv(NAMESPACE_ENV) != null ? System.getenv(NAMESPACE_ENV) : YaksSettings.getDefaultNamespace());
    }

    /**
     * Api version for current Kubernetes installation.
     * @return
     */
    public static String getApiVersion() {
        return System.getProperty(API_VERSION_PROPERTY,
                System.getenv(API_VERSION_ENV) != null ? System.getenv(API_VERSION_ENV) : API_VERSION_DEFAULT);
    }

    /**
     * Service name to use when creating a new service for cloud event subscriptions.
     * @return
     */
    public static String getServiceName() {
        return System.getProperty(SERVICE_NAME_PROPERTY,
                System.getenv(SERVICE_NAME_ENV) != null ? System.getenv(SERVICE_NAME_ENV) : SERVICE_NAME_DEFAULT);
    }

    /**
     * Service port used when consuming cloud events via Http.
     * @return
     */
    public static String getServicePort() {
        return System.getProperty(SERVICE_PORT_PROPERTY,
                System.getenv(SERVICE_PORT_ENV) != null ? System.getenv(SERVICE_PORT_ENV) : SERVICE_PORT_DEFAULT);
    }

    /**
     * When set to true Kubernetes resources (e.g. services) created during the test are
     * automatically removed after the test.
     * @return
     */
    public static boolean isAutoRemoveResources() {
        return Boolean.parseBoolean(System.getProperty(AUTO_REMOVE_RESOURCES_PROPERTY,
                System.getenv(AUTO_REMOVE_RESOURCES_ENV) != null ? System.getenv(AUTO_REMOVE_RESOURCES_ENV) : AUTO_REMOVE_RESOURCES_DEFAULT));
    }

    /**
     * When set to true test will print pod logs e.g. while waiting for a pod log message.
     * @return
     */
    public static boolean isPrintPodLogs() {
        return Boolean.parseBoolean(System.getProperty(PRINT_POD_LOGS_PROPERTY,
                System.getenv(PRINT_POD_LOGS_ENV) != null ? System.getenv(PRINT_POD_LOGS_ENV) : PRINT_POD_LOGS_DEFAULT));
    }

    /**
     * Maximum number of attempts when polling for running state and log messages.
     * @return
     */
    public static int getMaxAttempts() {
        return Integer.parseInt(System.getProperty(MAX_ATTEMPTS_PROPERTY,
                System.getenv(MAX_ATTEMPTS_ENV) != null ? System.getenv(MAX_ATTEMPTS_ENV) : MAX_ATTEMPTS_DEFAULT));
    }

    /**
     * Delay in milliseconds to wait after polling attempt.
     * @return
     */
    public static long getDelayBetweenAttempts() {
        return Long.parseLong(System.getProperty(DELAY_BETWEEN_ATTEMPTS_PROPERTY,
                System.getenv(DELAY_BETWEEN_ATTEMPTS_ENV) != null ? System.getenv(DELAY_BETWEEN_ATTEMPTS_ENV) : DELAY_BETWEEN_ATTEMPTS_DEFAULT));
    }

    /**
     * Duration in milliseconds to watch pod logs.
     * @return
     */
    public static long getWatchLogsTimeout() {
        return Long.parseLong(System.getProperty(WATCH_LOGS_TIMEOUT_PROPERTY,
                System.getenv(WATCH_LOGS_TIMEOUT_ENV) != null ? System.getenv(WATCH_LOGS_TIMEOUT_ENV) : WATCH_LOGS_TIMEOUT_DEFAULT));
    }
}
