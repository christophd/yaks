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

package org.citrusframework.yaks.knative.actions;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.yaks.YaksClusterType;
import org.citrusframework.yaks.YaksSettings;
import org.citrusframework.yaks.knative.KnativeSettings;
import org.citrusframework.yaks.knative.KnativeVariableNames;

/**
 * Base action provides access to Knative properties such as broker name. These properties are read from
 * environment settings or explicitly set as part of the test case and get stored as test variables in the current context.
 * This base class gives convenient access to the test variables and provides a fallback if no variable is set.
 *
 * @author Christoph Deppisch
 */
public interface KnativeAction extends TestAction {

    /**
     * Gets the Kubernetes client.
     * @return
     */
    KubernetesClient getKubernetesClient();

    /**
     * Gets the Knative client.
     * @return
     */
    KnativeClient getKnativeClient();

    /**
     * Resolves namespace name from given test context using the stored test variable.
     * Fallback to the namespace given in Knative environment settings when no test variable is present.
     *
     * @param context
     * @return
     */
    default String namespace(TestContext context) {
        if (context.getVariables().containsKey(KnativeVariableNames.NAMESPACE.value())) {
            return context.getVariable(KnativeVariableNames.NAMESPACE.value());
        }

        return KnativeSettings.getNamespace();
    }

    /**
     * Resolves the current broker name that has been set in the test context as test variable.
     * Fallback to the broker given in Knative environment settings when no test variable is present.
     *
     * @param context
     * @return
     */
    default String brokerName(TestContext context) {
        if (context.getVariables().containsKey(KnativeVariableNames.BROKER_NAME.value())) {
            context.getVariable(KnativeVariableNames.BROKER_NAME.value());
        }

        return KnativeSettings.getBrokerName();
    }

    /**
     * Resolves cluster type from given test context using the stored test variable.
     * Fallback to retrieving the cluster type from environment settings when no test variable is present.
     *
     * @param context
     * @return
     */
    default YaksClusterType clusterType(TestContext context) {
        if (context.getVariables().containsKey(KnativeVariableNames.CLUSTER_TYPE.value())) {
            Object clusterType = context.getVariableObject(KnativeVariableNames.CLUSTER_TYPE.value());

            if (clusterType instanceof YaksClusterType) {
                return (YaksClusterType) clusterType;
            } else {
                return YaksClusterType.valueOf(clusterType.toString());
            }
        }

        return YaksSettings.getClusterType();
    }
}

