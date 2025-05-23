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

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import org.citrusframework.Citrus;
import org.citrusframework.context.TestContext;
import org.citrusframework.yaks.YaksSettings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author Christoph Deppisch
 */
public final class KubernetesSupport {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
                .build()
                .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
    }

    private KubernetesSupport() {
        // prevent instantiation of utility class
    }

    /**
     * Dump given domain model object as YAML.
     * Uses Json conversion to generic map as intermediate step. This makes sure to properly write Json additional properties.
     * @param model
     * @return
     */
    public static String dumpYaml(Object model) {
        return yaml().dumpAsMap(json().convertValue(model, Map.class));
    }

    /**
     * Retrieve current Kubernetes client if set in Citrus context as bean reference.
     * Otherwise, create new default instance.
     * @param citrus holding the potential bean reference to the client instance.
     * @return
     */
    public static KubernetesClient getKubernetesClient(Citrus citrus) {
        if (YaksSettings.isLocal()) {
            return null;
        }

        if (citrus.getCitrusContext().getReferenceResolver().resolveAll(KubernetesClient.class).size() == 1L) {
            return citrus.getCitrusContext().getReferenceResolver().resolve(KubernetesClient.class);
        } else {
            return new KubernetesClientBuilder().build();
        }
    }

    /**
     * Retrieve current Kubernetes client if set in test context as bean reference.
     * Otherwise, create new default instance.
     * @param context holding the potential client bean reference.
     * @return
     */
    public static KubernetesClient getKubernetesClient(TestContext context) {
        if (context.getReferenceResolver().resolveAll(KubernetesClient.class).size() == 1L) {
            return context.getReferenceResolver().resolve(KubernetesClient.class);
        } else {
            return new KubernetesClientBuilder().build();
        }
    }

    /**
     * Retrieve current namespace set as test variable.
     * In case no suitable test variable is available use namespace loaded from Kubernetes settings via environment settings.
     * @param context potentially holding the namespace variable.
     * @return
     */
    public static String getNamespace(TestContext context) {
        if (context.getVariables().containsKey(KubernetesVariableNames.NAMESPACE.value())) {
            return context.getVariable(KubernetesVariableNames.NAMESPACE.value());
        }

        return KubernetesSettings.getNamespace();
    }

    public static Yaml yaml() {
        Representer representer = new Representer(new DumperOptions()) {
            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
                // if value of property is null, ignore it.
                if (propertyValue == null || (propertyValue instanceof Collection && ((Collection<?>) propertyValue).isEmpty()) ||
                    (propertyValue instanceof Map && ((Map<?, ?>) propertyValue).isEmpty())) {
                    return null;
                } else {
                    return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                }
            }
        };
        representer.getPropertyUtils().setSkipMissingProperties(true);
        return new Yaml(representer);
    }

    public static ObjectMapper json() {
        return OBJECT_MAPPER;
    }

    public static ResourceDefinitionContext crdContext(String resourceType, String group, String kind, String version) {
        return new ResourceDefinitionContext.Builder()
                .withGroup(group)
                .withKind(kind)
                .withVersion(version)
                .withPlural(resourceType.contains(".") ? resourceType.substring(0, resourceType.indexOf(".")) : resourceType)
                .withNamespaced(true)
                .build();
    }

    /**
     * Checks pod status with expected phase. If expected status is "Running" all
     * containers in the pod must be in ready state, too.
     * @param pod
     * @param status
     * @return
     */
    public static boolean verifyPodStatus(Pod pod, String status) {
        if (pod == null || pod.getStatus() == null ||
                !status.equals(pod.getStatus().getPhase())) {
            return false;
        }

        return !status.equals("Running") ||
                pod.getStatus().getContainerStatuses().stream().allMatch(ContainerStatus::getReady);
    }
}
