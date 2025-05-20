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

package org.citrusframework.yaks.camelk.actions.kamelet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import org.apache.camel.v1alpha1.KameletBinding;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.spi.Resources;
import org.citrusframework.yaks.YaksClusterType;
import org.citrusframework.yaks.camelk.CamelKSettings;
import org.citrusframework.yaks.camelk.actions.integration.CreateIntegrationActionTest;
import org.citrusframework.yaks.kubernetes.KubernetesSettings;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.awaitility.Awaitility.await;
import static org.citrusframework.yaks.camelk.jbang.CamelJBang.camel;

public class CreateKameletBindingActionTest {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CreateIntegrationActionTest.class);

    private final KubernetesMockServer k8sServer = new KubernetesMockServer(new Context(), new MockWebServer(),
            new HashMap<>(), new KubernetesCrudDispatcher(), false);

    private final KubernetesClient kubernetesClient = k8sServer.createClient();

    private final TestContext context = TestContextFactory.newInstance().getObject();

    @BeforeClass
    public static void setup() {
        camel().version();
    }

    @Test
    public void shouldCreateBinding() {
        CreateKameletBindingAction action = new CreateKameletBindingAction.Builder()
                .client(kubernetesClient)
                .apiVersion(CamelKSettings.V1ALPHA1)
                .binding("kafka-source-binding")
                .resource(Resources.fromClasspath("kafka-source-binding.yaml"))
                .build();

        context.setVariable("CITRUS_NAMESPACE", "default");
        context.setVariable("bootstrap.server.host", "my-cluster-kafka-bootstrap");
        context.setVariable("bootstrap.server.port", "9092");
        context.setVariable("topic", "my-topic");

        action.execute(context);

        KameletBinding binding = kubernetesClient.resources(KameletBinding.class).inNamespace(KubernetesSettings.getNamespace()).withName("kafka-source-binding").get();
        Assert.assertNotNull(binding.getSpec().getSource().getRef());
        Assert.assertEquals("kafka-source", binding.getSpec().getSource().getRef().getName());
        Assert.assertNotNull(binding.getSpec().getSource().getProperties());
        Assert.assertEquals(6L, binding.getSpec().getSource().getProperties().getAdditionalProperties().size());
        Assert.assertNotNull(binding.getSpec().getSink().getUri());
    }

    @Test
    public void shouldCreateLocalBinding() {
        System.setProperty("citrus.jbang.camel.dump.integration.output", "true");

        CreateKameletBindingAction action = new CreateKameletBindingAction.Builder()
                .client(kubernetesClient)
                .apiVersion(CamelKSettings.V1ALPHA1)
                .binding("timer-to-log-binding")
                .clusterType(YaksClusterType.LOCAL)
                .resource(Resources.fromClasspath("timer-to-log-binding.yaml"))
                .build();

        action.execute(context);

        Assert.assertNotNull(context.getVariable("timer-to-log-binding:pid"));

        Long pid = context.getVariable("timer-to-log-binding:pid", Long.class);

        try {
            await().atMost(30000L, TimeUnit.MILLISECONDS).until(() -> {
                Map<String, String> integration = camel().get(pid);

                if (integration.isEmpty() || integration.get("STATUS").equals("Starting")) {
                    LOG.info("Waiting for Camel integration to start ...");
                    return false;
                }

                Assert.assertEquals("timer-to-log-binding", integration.get("NAME"));
                Assert.assertEquals("Running", integration.get("STATUS"));

                return true;
            });
        } finally {
            camel().stop(pid);
        }
    }
}
