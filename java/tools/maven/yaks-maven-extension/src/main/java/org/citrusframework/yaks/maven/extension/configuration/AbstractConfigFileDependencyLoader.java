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

package org.citrusframework.yaks.maven.extension.configuration;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.logging.Logger;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractConfigFileDependencyLoader implements DependencyLoader  {

    @Override
    public List<Dependency> load(Properties properties, Logger logger) {
        return Collections.emptyList();
    }

    /**
     * Load dependencies from given file.
     * @param filePath
     * @param properties
     * @param logger
     * @return
     * @throws LifecycleExecutionException
     */
    protected abstract List<Dependency> load(Path filePath, Properties properties, Logger logger) throws LifecycleExecutionException;
}
