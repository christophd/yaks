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

package org.citrusframework.yaks.groovy.dsl.endpoints;

/**
 * @author Christoph Deppisch
 */
public class EndpointConfigurationScript {

    private String endpointType;

    public Object methodMissing(String name, Object argLine) {
        if (endpointType == null) {
            endpointType = name;
            return this;
        } else {
            return EndpointBuilderHelper.find(endpointType + "." + EndpointBuilderHelper.sanitizeEndpointBuilderName(name));
        }
    }
}
