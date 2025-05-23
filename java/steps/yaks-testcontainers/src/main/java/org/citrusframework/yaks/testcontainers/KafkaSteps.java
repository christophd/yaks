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

import java.util.HashMap;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.kafka.KafkaSettings;
import org.testcontainers.containers.KafkaContainer;

import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class KafkaSteps {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    private String kafkaVersion = KafkaSettings.getKafkaVersion();

    private int startupTimeout = KafkaSettings.getStartupTimeout();

    private String serviceName = KafkaSettings.getServiceName();

    private Map<String, String> env = new HashMap<>();

    @Before
    public void before(Scenario scenario) {
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(KafkaSettings.getContainerName(), KafkaContainer.class)) {
            KafkaContainer kafkaContainer = citrus.getCitrusContext().getReferenceResolver().resolve(KafkaSettings.getContainerName(), KafkaContainer.class);
            KafkaSettings.exposeConnectionSettings(kafkaContainer, serviceName, context);
        }
    }

    @Given("^Kafka container version (^\\s+)$")
    public void setKafkaVersion(String version) {
        this.kafkaVersion = version;
    }

    @Given("^Kafka service name (^\\s+)$")
    public void setKafkaServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Given("^Kafka container startup timeout is (\\d+)(?: s| seconds)$")
    public void setStartupTimeout(int timeout) {
        this.startupTimeout = timeout;
    }

    @Given("^Kafka container env settings$")
    public void setEnvSettings(DataTable settings) {
        this.env.putAll(settings.asMap());
    }

    @Given("^start Kafka container$")
    public void startKafka() {
        runner.run(testcontainers()
                .kafka()
                .start()
                .version(kafkaVersion)
                .serviceName(serviceName)
                .withStartupTimeout(startupTimeout)
                .withEnv(env)
                .autoRemove(TestContainersSteps.autoRemoveResources));
    }

    @Given("^stop Kafka container$")
    public void stopKafka() {
        runner.run(testcontainers()
                .stop()
                .containerName(KafkaSettings.getContainerName()));

        env = new HashMap<>();
    }
}
