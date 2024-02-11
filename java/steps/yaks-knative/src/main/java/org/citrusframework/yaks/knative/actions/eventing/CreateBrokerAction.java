/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.yaks.knative.actions.eventing;

import io.fabric8.knative.eventing.v1.Broker;
import io.fabric8.knative.eventing.v1.BrokerBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.yaks.YaksSettings;
import org.citrusframework.yaks.knative.KnativeSettings;
import org.citrusframework.yaks.knative.KnativeSupport;
import org.citrusframework.yaks.knative.KnativeVariableNames;
import org.citrusframework.yaks.knative.actions.AbstractKnativeAction;

/**
 * @author Christoph Deppisch
 */
public class CreateBrokerAction extends AbstractKnativeAction {

    private final String brokerName;

    public CreateBrokerAction(Builder builder) {
        super("create-broker", builder);

        this.brokerName = builder.brokerName;
    }

    @Override
    public void doExecute(TestContext context) {
        if (YaksSettings.isLocal(clusterType(context))) {
            createLocalBroker(context);
        } else {
            createBroker(context);
        }
    }

    /**
     * Creates Http server as a local Knative broker.
     * @param context
     */
    private void createLocalBroker(TestContext context) {
        String resolvedBrokerName = context.replaceDynamicContentInString(brokerName);

        if (!context.getReferenceResolver().isResolvable(resolvedBrokerName, HttpServer.class)) {
            HttpServer brokerServer = new HttpServerBuilder()
                    .autoStart(true)
                    .port(Integer.parseInt(KnativeSettings.getServicePort()))
                    .referenceResolver(context.getReferenceResolver())
                    .build();

            brokerServer.initialize();
            context.getReferenceResolver()
                    .bind(resolvedBrokerName, brokerServer);

            context.setVariable(KnativeVariableNames.BROKER_PORT.value(), brokerServer.getPort());
        }
    }

    /**
     * Creates Knative broker on current namespace.
     * @param context
     */
    private void createBroker(TestContext context) {
        Broker broker = new BrokerBuilder()
                .withApiVersion(String.format("%s/%s", KnativeSupport.knativeEventingGroup(), KnativeSupport.knativeApiVersion()))
                .withNewMetadata()
                .withNamespace(namespace(context))
                .withName(context.replaceDynamicContentInString(brokerName))
                .withLabels(KnativeSettings.getDefaultLabels())
                .endMetadata()
                .build();

        getKnativeClient().brokers()
                .inNamespace(namespace(context))
                .createOrReplace(broker);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<CreateBrokerAction, Builder> {

        private String brokerName;

        public Builder name(String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        @Override
        public CreateBrokerAction build() {
            return new CreateBrokerAction(this);
        }
    }
}
