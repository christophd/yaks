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
import org.apache.camel.v1.Pipe;
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

public class CreatePipeActionTest {

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
    public void shouldCreateLocalPipe() {
        System.setProperty("citrus.jbang.camel.dump.integration.output", "true");

        CreatePipeAction action = new CreatePipeAction.Builder()
                .client(kubernetesClient)
                .apiVersion(CamelKSettings.V1)
                .pipe("timer-to-log-pipe")
                .clusterType(YaksClusterType.LOCAL)
                .resource(Resources.fromClasspath("timer-to-log-pipe.yaml"))
                .build();

        action.execute(context);

        Assert.assertNotNull(context.getVariable("timer-to-log-pipe:pid"));

        Long pid = context.getVariable("timer-to-log-pipe:pid", Long.class);

        try {
            await().atMost(30000L, TimeUnit.MILLISECONDS).until(() -> {
                Map<String, String> integration = camel().get(pid);

                if (integration.isEmpty() || integration.get("STATUS").equals("Starting")) {
                    LOG.info("Waiting for Camel integration to start ...");
                    return false;
                }

                Assert.assertEquals("timer-to-log-pipe", integration.get("NAME"));
                Assert.assertEquals("Running", integration.get("STATUS"));

                return true;
            });
        } finally {
            camel().stop(pid);
        }
    }

    @Test
    public void shouldCreatePipe() {
        CreatePipeAction action = new CreatePipeAction.Builder()
                .client(kubernetesClient)
                .apiVersion(CamelKSettings.V1)
                .pipe("kafka-source-pipe")
                .resource(Resources.fromClasspath("kafka-source-pipe.yaml"))
                .build();

        context.setVariable("CITRUS_NAMESPACE", "default");
        context.setVariable("bootstrap.server.host", "my-cluster-kafka-bootstrap");
        context.setVariable("bootstrap.server.port", "9092");
        context.setVariable("topic", "my-topic");

        action.execute(context);

        Pipe pipe = kubernetesClient.resources(Pipe.class).inNamespace(KubernetesSettings.getNamespace()).withName("kafka-source-pipe").get();
        Assert.assertNotNull(pipe);
        Assert.assertNotNull(pipe.getSpec().getSource().getRef());
        Assert.assertEquals("kafka-source", pipe.getSpec().getSource().getRef().getName());
        Assert.assertNotNull(pipe.getSpec().getSource().getProperties());
        Assert.assertEquals(6L, pipe.getSpec().getSource().getProperties().getAdditionalProperties().size());
        Assert.assertNotNull(pipe.getSpec().getSink().getUri());
    }
}
