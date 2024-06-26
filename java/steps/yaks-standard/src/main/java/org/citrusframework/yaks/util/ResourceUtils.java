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

package org.citrusframework.yaks.util;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.yaks.YaksVariableNames;

public final class ResourceUtils {

    private ResourceUtils() {
        // prevent instantiation of utility class.
    }

    public static Resource resolve(String path, TestContext context) {
        Resource resource = FileUtils.getFileResource(path, context);
        if (resource.exists()) {
            return resource;
        }

        if (context.getVariables().containsKey(YaksVariableNames.FEATURE_PACKAGE.value())) {
            String contextPath = context.getVariable(YaksVariableNames.FEATURE_PACKAGE.value()).replace(".", "/");
            Resource contextResource = FileUtils.getFileResource(contextPath + "/" + path, context);
            if (contextResource.exists()) {
                return contextResource;
            }
        }

        throw new CitrusRuntimeException(String.format("Failed to resolve resource for path: %s", path));
    }
}
