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

package org.citrusframework.yaks.knative;

import java.util.Optional;

import org.citrusframework.yaks.YaksSettings;
import org.citrusframework.yaks.kubernetes.KubernetesSettings;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class KnativeSettings {

    private static final String KNATIVE_PROPERTY_PREFIX = "citrus.knative.";
    private static final String KNATIVE_ENV_PREFIX = "CITRUS_KNATIVE_";

    private static final String EVENT_PRODUCER_TIMEOUT_PROPERTY = KNATIVE_PROPERTY_PREFIX + "event.producer.timeout";
    private static final String EVENT_PRODUCER_TIMEOUT_ENV = KNATIVE_ENV_PREFIX + "EVENT_PRODUCER_TIMEOUT";
    private static final String EVENT_PRODUCER_TIMEOUT_DEFAULT = "2000";

    private static final String EVENT_CONSUMER_TIMEOUT_PROPERTY = KNATIVE_PROPERTY_PREFIX + "event.consumer.timeout";
    private static final String EVENT_CONSUMER_TIMEOUT_ENV = KNATIVE_ENV_PREFIX + "EVENT_CONSUMER_TIMEOUT";
    private static final String EVENT_CONSUMER_TIMEOUT_DEFAULT = "2000";

    private static final String NAMESPACE_PROPERTY = KNATIVE_PROPERTY_PREFIX + "namespace";
    private static final String NAMESPACE_ENV = KNATIVE_ENV_PREFIX + "NAMESPACE";

    private static final String API_VERSION_PROPERTY = KNATIVE_PROPERTY_PREFIX + "api.version";
    private static final String API_VERSION_ENV = KNATIVE_ENV_PREFIX + "API_VERSION";
    private static final String API_VERSION_DEFAULT = "v1";

    private static final String BROKER_HOST_PROPERTY = KNATIVE_PROPERTY_PREFIX + "broker.host";
    private static final String BROKER_HOST_ENV = KNATIVE_ENV_PREFIX + "BROKER_HOST";
    private static final String BROKER_HOST_DEFAULT = String.format("broker-ingress.knative-eventing.%s", YaksSettings.DEFAULT_DOMAIN_SUFFIX);

    private static final String BROKER_NAME_PROPERTY = KNATIVE_PROPERTY_PREFIX + "broker.name";
    private static final String BROKER_NAME_ENV = KNATIVE_ENV_PREFIX + "BROKER_NAME";
    private static final String BROKER_NAME_DEFAULT = "default";

    private static final String BROKER_PORT_PROPERTY = KNATIVE_PROPERTY_PREFIX + "broker.port";
    private static final String BROKER_PORT_ENV = KNATIVE_ENV_PREFIX + "BROKER_PORT";
    private static final String BROKER_PORT_DEFAULT = "8080";

    private static final String BROKER_URL_PROPERTY = KNATIVE_PROPERTY_PREFIX + "broker.url";
    private static final String BROKER_URL_ENV = KNATIVE_ENV_PREFIX + "BROKER_URL";

    private static final String SERVICE_NAME_PROPERTY = KNATIVE_PROPERTY_PREFIX + "service.name";
    private static final String SERVICE_NAME_ENV = KNATIVE_ENV_PREFIX + "SERVICE_NAME";
    private static final String SERVICE_NAME_DEFAULT = "yaks-knative-service";

    private static final String SERVICE_PORT_PROPERTY = KNATIVE_PROPERTY_PREFIX + "service.port";
    private static final String SERVICE_PORT_ENV = KNATIVE_ENV_PREFIX + "SERVICE_PORT";

    private static final String AUTO_REMOVE_RESOURCES_PROPERTY = KNATIVE_PROPERTY_PREFIX + "auto.remove.resources";
    private static final String AUTO_REMOVE_RESOURCES_ENV = KNATIVE_ENV_PREFIX + "AUTO_REMOVE_RESOURCES";
    private static final String AUTO_REMOVE_RESOURCES_DEFAULT = "false";

    private KnativeSettings() {
        // prevent instantiation of utility class
    }

    /**
     * Request timeout when sending cloud events.
     * @return
     */
    public static long getEventProducerTimeout() {
        return Long.parseLong(System.getProperty(EVENT_PRODUCER_TIMEOUT_PROPERTY,
                System.getenv(EVENT_PRODUCER_TIMEOUT_ENV) != null ? System.getenv(EVENT_PRODUCER_TIMEOUT_ENV) : EVENT_PRODUCER_TIMEOUT_DEFAULT));
    }

    /**
     * Request timeout when receiving cloud events.
     * @return
     */
    public static long getEventConsumerTimeout() {
        return Long.parseLong(System.getProperty(EVENT_CONSUMER_TIMEOUT_PROPERTY,
                System.getenv(EVENT_CONSUMER_TIMEOUT_ENV) != null ? System.getenv(EVENT_CONSUMER_TIMEOUT_ENV) : EVENT_CONSUMER_TIMEOUT_DEFAULT));
    }

    /**
     * Namespace to work on when performing Knative client operations such as creating triggers, services and so on.
     * @return
     */
    public static String getNamespace() {
        return System.getProperty(NAMESPACE_PROPERTY,
                System.getenv(NAMESPACE_ENV) != null ? System.getenv(NAMESPACE_ENV) : YaksSettings.getDefaultNamespace());
    }

    /**
     * Api version for current Knative installation.
     * @return
     */
    public static String getApiVersion() {
        return System.getProperty(API_VERSION_PROPERTY,
                System.getenv(API_VERSION_ENV) != null ? System.getenv(API_VERSION_ENV) : API_VERSION_DEFAULT);
    }

    /**
     * Broker host used as Http header when creating cloud events.
     * @return
     */
    public static String getBrokerHost() {
        String brokerHostDefault;

        if (YaksSettings.isKubernetesCluster() || YaksSettings.isOpenshiftCluster()) {
            brokerHostDefault = BROKER_HOST_DEFAULT;
        } else {
            brokerHostDefault = "localhost";
        }

        return System.getProperty(BROKER_HOST_PROPERTY,
                System.getenv(BROKER_HOST_ENV) != null ? System.getenv(BROKER_HOST_ENV) : brokerHostDefault);
    }

    /**
     * Broker to use when producing/consuming cloud events.
     * @return
     */
    public static String getBrokerName() {
        return System.getProperty(BROKER_NAME_PROPERTY,
                System.getenv(BROKER_NAME_ENV) != null ? System.getenv(BROKER_NAME_ENV) : BROKER_NAME_DEFAULT);
    }

    /**
     * Broker port to use when producing/consuming cloud events in local environment.
     * @return
     */
    public static String getBrokerPort() {
        return System.getProperty(BROKER_PORT_PROPERTY,
                System.getenv(BROKER_PORT_ENV) != null ? System.getenv(BROKER_PORT_ENV) : BROKER_PORT_DEFAULT);
    }

    /**
     * Broker URL to use when producing/consuming cloud events.
     * @return
     */
    public static String getBrokerUrl() {
        String brokerUrlDefault;
        if (YaksSettings.isKubernetesCluster() || YaksSettings.isOpenshiftCluster()) {
            brokerUrlDefault = String.format("http://%s/%s/${%s}", getBrokerHost(), getNamespace(), KnativeVariableNames.BROKER_NAME.value());
        } else if (YaksSettings.isLocal()) {
            brokerUrlDefault = String.format("http://%s%s", getBrokerHost(), StringUtils.hasText(getBrokerPort()) ? ":" + getBrokerPort() : "");
        } else {
            brokerUrlDefault = String.format("http://%s", getBrokerHost());
        }

        return System.getProperty(BROKER_URL_PROPERTY,
                System.getenv(BROKER_URL_ENV) != null ? System.getenv(BROKER_URL_ENV) : brokerUrlDefault);
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
        return Optional.ofNullable(System.getProperty(SERVICE_PORT_PROPERTY, System.getenv(SERVICE_PORT_ENV)))
                .orElseGet(KubernetesSettings::getServicePort);
    }

    /**
     * When set to true Knative resources (triggers, subscriptions, brokers, etc.) created during the test are
     * automatically removed after the test.
     * @return
     */
    public static boolean isAutoRemoveResources() {
        return Boolean.parseBoolean(System.getProperty(AUTO_REMOVE_RESOURCES_PROPERTY,
                System.getenv(AUTO_REMOVE_RESOURCES_ENV) != null ? System.getenv(AUTO_REMOVE_RESOURCES_ENV) : AUTO_REMOVE_RESOURCES_DEFAULT));
    }

    public static String getKnativeMessagingGroup() {
        return "messaging.knative.dev";
    }

    public static String getKnativeEventingGroup() {
        return "eventing.knative.dev";
    }
}
