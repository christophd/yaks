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


import com.consol.citrus.endpoint.EndpointAdapter
import com.consol.citrus.endpoint.adapter.RequestDispatchingEndpointAdapter
import com.consol.citrus.endpoint.adapter.StaticEndpointAdapter
import com.consol.citrus.endpoint.adapter.mapping.HeaderMappingKeyExtractor
import com.consol.citrus.endpoint.adapter.mapping.SimpleMappingStrategy
import com.consol.citrus.http.message.HttpMessage
import com.consol.citrus.http.message.HttpMessageHeaders
import com.consol.citrus.message.Message
import com.consol.citrus.util.FileUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.core.io.ClassPathResource

EndpointAdapter templateResponseAdapter() {
    RequestDispatchingEndpointAdapter dispatchingEndpointAdapter = new RequestDispatchingEndpointAdapter()

    Map<String, EndpointAdapter> mappings = new HashMap<>()

    mappings.put("/", indexPageHandler())
    mappings.put("/favicon.ico", faviconHandler())

    SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy()
    mappingStrategy.setAdapterMappings(mappings)
    dispatchingEndpointAdapter.setMappingStrategy(mappingStrategy)

    dispatchingEndpointAdapter.setMappingKeyExtractor(new HeaderMappingKeyExtractor(HttpMessageHeaders.HTTP_REQUEST_URI))

    return dispatchingEndpointAdapter
}

static EndpointAdapter indexPageHandler() {
    return new StaticEndpointAdapter() {
        @Override
        protected Message handleMessageInternal(Message message) {
            try {
                return new HttpMessage(FileUtils.readToString(new ClassPathResource("index.html")))
                        .contentType(MediaType.TEXT_HTML_VALUE)
                        .status(HttpStatus.OK)
            } catch (IOException ignored) {
                return new HttpMessage().status(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }
}

static EndpointAdapter faviconHandler() {
    return new StaticEndpointAdapter() {
        @Override
        protected Message handleMessageInternal(Message message) {
            return new HttpMessage().status(HttpStatus.OK)
        }
    }
}

http()
    .server()
    .port(8080)
    .endpointAdapter(templateResponseAdapter())
    .autoStart(true)
