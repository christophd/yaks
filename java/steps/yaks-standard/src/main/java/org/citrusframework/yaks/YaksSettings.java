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

package org.citrusframework.yaks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;

import org.citrusframework.CitrusSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public class YaksSettings {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(YaksSettings.class);

    private static final String CITRUS_PROPERTY_PREFIX = "citrus.";
    private static final String CITRUS_ENV_PREFIX = "CITRUS_";

    private static final String CLUSTER_WILDCARD_DOMAIN_PROPERTY = "cluster.wildcard.domain";
    private static final String CLUSTER_WILDCARD_DOMAIN_ENV = "CLUSTER_WILDCARD_DOMAIN";
    public static final String DEFAULT_DOMAIN_SUFFIX = "svc.cluster.local";

    private static final String NAMESPACE_PROPERTY = CITRUS_PROPERTY_PREFIX + "namespace";
    private static final String NAMESPACE_ENV = CITRUS_ENV_PREFIX + "NAMESPACE";
    private static final String NAMESPACE_DEFAULT = "default";

    private static final String OPERATOR_NAMESPACE_PROPERTY = CITRUS_PROPERTY_PREFIX + "namespace";
    private static final String OPERATOR_NAMESPACE_ENV = CITRUS_ENV_PREFIX + "NAMESPACE";

    private static final String CLUSTER_TYPE_PROPERTY = CITRUS_PROPERTY_PREFIX + "cluster.type";
    private static final String CLUSTER_TYPE_ENV = CITRUS_ENV_PREFIX + "CLUSTER_TYPE";
    private static final String CLUSTER_TYPE_DEFAULT = YaksClusterType.KUBERNETES.name();

    private static final String TEST_ID_PROPERTY = CITRUS_PROPERTY_PREFIX + "test.id";
    private static final String TEST_ID_ENV = CITRUS_ENV_PREFIX + "TEST_ID";
    private static final String TEST_ID_DEFAULT = "yaks-test";

    private static final String DEFAULT_MESSAGE_TYPE_PROPERTY = CITRUS_PROPERTY_PREFIX + "default.message.type";
    private static final String DEFAULT_MESSAGE_TYPE_ENV = CITRUS_ENV_PREFIX + "DEFAULT_MESSAGE_TYPE";
    private static final String DEFAULT_MESSAGE_TYPE_DEFAULT = CitrusSettings.DEFAULT_MESSAGE_TYPE;

    private static final String TERMINATION_LOG_PROPERTY = CITRUS_PROPERTY_PREFIX + "termination.log";
    private static final String TERMINATION_LOG_ENV = CITRUS_ENV_PREFIX + "TERMINATION_LOG";
    private static final String TERMINATION_LOG_DEFAULT = "target/termination.log";

    /**
     * Default YAKS operator namespace. If not set in environment vars use default according to the cluster type.
     * @return
     */
    public static String getOperatorNamespace() {
        return Optional.ofNullable(System.getProperty(OPERATOR_NAMESPACE_PROPERTY, System.getenv(OPERATOR_NAMESPACE_ENV)))
                .orElseGet(() -> getClusterType().operatorNamespace());
    }

    /**
     * Namespace to work on when performing Kubernetes/Knative client operations on resources.
     * @return
     */
    public static String getDefaultNamespace() {
        String systemNamespace = System.getProperty(NAMESPACE_PROPERTY, System.getenv(NAMESPACE_ENV));

        if (systemNamespace != null) {
            return systemNamespace;
        }

        final File namespace = new File("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        if (namespace.exists()){
            try {
                return Files.readString(namespace.toPath());
            } catch (IOException e) {
                LOG.warn("Failed to read Kubernetes namespace from filesystem {}", namespace, e);
            }
        }

        return NAMESPACE_DEFAULT;
    }

    /**
     * Cluster wildcard domain or default if non is set.
     * @return
     */
    public static String getClusterWildcardDomain() {
        return System.getProperty(CLUSTER_WILDCARD_DOMAIN_PROPERTY,
                System.getenv(CLUSTER_WILDCARD_DOMAIN_ENV) != null ? System.getenv(CLUSTER_WILDCARD_DOMAIN_ENV) : getDefaultNamespace() + "." + DEFAULT_DOMAIN_SUFFIX);
    }

    /**
     * Cluster type that YAKS is running on.
     * @return
     */
    public static YaksClusterType getClusterType() {
        return YaksClusterType.valueOf(System.getProperty(CLUSTER_TYPE_PROPERTY,
                System.getenv(CLUSTER_TYPE_ENV) != null ? System.getenv(CLUSTER_TYPE_ENV) : CLUSTER_TYPE_DEFAULT).toUpperCase(Locale.US));
    }

    /**
     * Termination log file path.
     * @return
     */
    public static Path getTerminationLog() {
        return Paths.get(System.getProperty(TERMINATION_LOG_PROPERTY,
                System.getenv(TERMINATION_LOG_ENV) != null ? System.getenv(TERMINATION_LOG_ENV) : TERMINATION_LOG_DEFAULT));
    }

    /**
     * Current test id that is also set as label on the Pod running the test.
     * @return
     */
    public static String getTestId() {
        return System.getProperty(TEST_ID_PROPERTY, Optional.ofNullable(System.getenv(TEST_ID_ENV)).orElse(TEST_ID_DEFAULT));
    }

    /**
     * Default message type.
     * @return
     */
    public static String getDefaultMessageType() {
        return System.getProperty(DEFAULT_MESSAGE_TYPE_PROPERTY, Optional.ofNullable(System.getenv(DEFAULT_MESSAGE_TYPE_ENV)).orElse(DEFAULT_MESSAGE_TYPE_DEFAULT));
    }

    /**
     * True when running on localhost.
     * @return
     */
    public static boolean isLocal() {
        return isLocal(getClusterType());
    }

    /**
     * True when running on localhost.
     * @return
     */
    public static boolean isLocal(YaksClusterType clusterType) {
        return YaksClusterType.LOCAL.equals(clusterType);
    }

    /**
     * True when running on Openshift.
     * @return
     */
    public static boolean isOpenshiftCluster() {
        return YaksClusterType.OPENSHIFT.equals(getClusterType());
    }

    /**
     * True when running on Kubernetes.
     * @return
     */
    public static boolean isKubernetesCluster() {
        return YaksClusterType.KUBERNETES.equals(getClusterType());
    }
}
