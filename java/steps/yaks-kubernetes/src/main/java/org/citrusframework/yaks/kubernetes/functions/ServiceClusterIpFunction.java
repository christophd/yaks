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

package org.citrusframework.yaks.kubernetes.functions;

import java.util.List;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.yaks.YaksSettings;
import org.citrusframework.yaks.kubernetes.KubernetesSupport;

public class ServiceClusterIpFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (YaksSettings.isLocal()) {
            return "127.0.0.1";
        }

        if (parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty - please provide a proper service name");
        }

        String serviceName = parameterList.get(0);

        String namespace;
        if (parameterList.size() > 1) {
            namespace = parameterList.get(1);
        } else {
            namespace = KubernetesSupport.getNamespace(context);
        }

        KubernetesClient k8sClient = KubernetesSupport.getKubernetesClient(context);

        Service service = k8sClient.services()
                    .inNamespace(namespace)
                    .withName(serviceName)
                    .get();

        if (service == null) {
            throw new CitrusRuntimeException(String.format("Unable to resolve service instance %s/%s", namespace, serviceName));
        }

        String clusterIp = service.getSpec().getClusterIP();
        if (clusterIp != null) {
            return clusterIp;
        }

        if (!service.getSpec().getExternalIPs().isEmpty()) {
            return service.getSpec().getExternalIPs().get(0);
        }

        throw new CitrusRuntimeException(String.format("Unable to resolve cluster ip on service instance %s - no cluster ip set", service.getMetadata().getName()));
    }
}
