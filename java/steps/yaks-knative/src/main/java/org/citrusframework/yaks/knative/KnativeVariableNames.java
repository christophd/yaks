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

package org.citrusframework.yaks.knative;

/**
 * @author Christoph Deppisch
 */
public enum KnativeVariableNames {

    CLUSTER_TYPE("CITRUS_CLUSTER_TYPE"),
    BROKER_NAME("KNATIVE_BROKER"),
    BROKER_PORT("KNATIVE_BROKER_PORT"),
    NAMESPACE("KNATIVE_NAMESPACE");

    private final String variableName;

    KnativeVariableNames(String variableName) {
        this.variableName = variableName;
    }

    public String value() {
        return variableName;
    }

    @Override
    public String toString() {
        return variableName;
    }
}
