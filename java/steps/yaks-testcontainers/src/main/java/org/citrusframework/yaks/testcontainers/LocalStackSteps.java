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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.LocalStackSettings;

import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class LocalStackSteps {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    private String localStackVersion = LocalStackSettings.getVersion();
    private int startupTimeout = LocalStackSettings.getStartupTimeout();

    private Set<LocalStackContainer.Service> services = new HashSet<>();

    private Map<String, String> env = new HashMap<>();

    private String serviceName = LocalStackSettings.getServiceName();

    @Before
    public void before(Scenario scenario) {
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(LocalStackSettings.getContainerName(), LocalStackContainer.class)) {
            LocalStackContainer aws2Container = citrus.getCitrusContext().getReferenceResolver().resolve(LocalStackSettings.getContainerName(), LocalStackContainer.class);
            LocalStackSettings.exposeConnectionSettings(aws2Container, serviceName, context);
        }
    }

    @Given("^LocalStack version (^\\s+)$")
    public void setLocalStackVersion(String version) {
        this.localStackVersion = version;
    }

    @Given("^LocalStack service name (^\\s+)$")
    public void setLocalStackServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Given("^LocalStack env settings$")
    public void setEnvSettings(DataTable settings) {
        this.env.putAll(settings.asMap());
    }

    @Given("^LocalStack startup timeout is (\\d+)(?: s| seconds)$")
    public void setStartupTimeout(int timeout) {
        this.startupTimeout = timeout;
    }

    @Given("^Enable service (S3|KINESIS|SQS|SNS|DYNAMODB|DYNAMODB_STREAMS|IAM|API_GATEWAY|FIREHOSE|LAMBDA)$")
    public void enableService(String service) {
        services.add(LocalStackContainer.Service.valueOf(service));
    }

    @Given("^start LocalStack container$")
    public void startLocalStack() {
        runner.run(testcontainers()
                .localstack()
                .start()
                .version(localStackVersion)
                .serviceName(serviceName)
                .withStartupTimeout(startupTimeout)
                .withEnv(env)
                .withServices(services)
                .autoRemove(TestContainersSteps.autoRemoveResources));
    }

    @Given("^stop LocalStack container$")
    public void stopLocalStack() {
        runner.run(testcontainers()
                .localstack()
                .stop()
                .containerName(LocalStackSettings.getContainerName()));

        env = new HashMap<>();
        services = new HashSet<>();
    }
}
