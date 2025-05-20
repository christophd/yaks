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


import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.yaks.YaksClusterType;
import org.citrusframework.yaks.camelk.CamelKSettings;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.awaitility.Awaitility.await;
import static org.citrusframework.yaks.camelk.jbang.CamelJBang.camel;

public class DeleteKameletBindingActionTest {

    private final KubernetesMockServer k8sServer = new KubernetesMockServer(new Context(), new MockWebServer(),
            new HashMap<>(), new KubernetesCrudDispatcher(), false);

    private final KubernetesClient kubernetesClient = k8sServer.createClient();

    private final TestContext context = TestContextFactory.newInstance().getObject();

    private static Path sampleBinding;

    @BeforeClass
    public static void setup() throws IOException {
        sampleBinding = new ClassPathResource("timer-to-log-binding.yaml").getFile().toPath();
        camel().version();
    }

    @Test
    public void shouldDeleteLocalBinding() {
        Long pid = camel().run("timer-to-log-binding.yaml", sampleBinding).getCamelProcessId();

        try {
            await().atMost(30000L, TimeUnit.MILLISECONDS).until(() -> !camel().get(pid).isEmpty());

            DeleteKameletBindingAction action = new DeleteKameletBindingAction.Builder()
                    .client(kubernetesClient)
                    .apiVersion(CamelKSettings.V1ALPHA1)
                    .binding("timer-to-log-binding.yaml")
                    .clusterType(YaksClusterType.LOCAL)
                    .build();

            context.setVariable("timer-to-log-binding.yaml:pid", pid);

            action.execute(context);

            await().atMost(15000L, TimeUnit.MILLISECONDS).until(() -> camel().get(pid).isEmpty());
        } finally {
            camel().stop(pid);
        }
    }

    @Test
    public void shouldDeleteLocalBindingByName() {
        Long pid = camel().run("timer-to-log-binding.yaml", sampleBinding).getCamelProcessId();

        try {
            await().atMost(30000L, TimeUnit.MILLISECONDS).until(() -> !camel().get(pid).isEmpty());

            DeleteKameletBindingAction action = new DeleteKameletBindingAction.Builder()
                    .client(kubernetesClient)
                    .apiVersion(CamelKSettings.V1ALPHA1)
                    .binding("timer-to-log-binding.yaml")
                    .clusterType(YaksClusterType.LOCAL)
                    .build();

            action.execute(context);

            await().atMost(15000L, TimeUnit.MILLISECONDS).until(() -> camel().get(pid).isEmpty());
        } finally {
            camel().stop(pid);
        }
    }
}
