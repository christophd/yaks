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

package org.citrusframework.yaks.maven.extension.configuration;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.logging.Logger;

/**
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface DependencyLoader {

    Pattern COORDINATE_PATTERN = Pattern.compile("^(?<groupId>[^:]+?):(?<artifactId>[^:]+?):(?<version>[@.0-9][^:]+?)$");
    Pattern VERSION_PROPERTY_PATTERN = Pattern.compile("^@(?<propertyName>[^@]+?)@$");

    /**
     * Load dependencies from configuration source.
     *
     * @param properties
     * @param logger
     * @return
     */
    List<Dependency> load(Properties properties, Logger logger) throws LifecycleExecutionException;

    /**
     * Construct dependency form coordinate string that follows the format "groupId:artifactId:version". Coordinates must
     * have a version set.
     * @param coordinates
     * @param properties
     * @param logger
     * @return
     */
    default Dependency build(String coordinates, Properties properties, Logger logger) throws LifecycleExecutionException {
        Dependency dependency = new Dependency();
        Matcher matcher = COORDINATE_PATTERN.matcher(coordinates);
        if (!matcher.matches()) {
            throw new LifecycleExecutionException("Unsupported dependency coordinate. Must be of format groupId:artifactId:version");
        }

        String groupId = matcher.group("groupId");
        String artifactId = matcher.group("artifactId");
        String version = resolveVersionProperty(matcher.group("version"), properties);
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);

        logger.info(String.format("Add %s", dependency));
        return dependency;
    }

    /**
     * Resolve version supporting Maven property expressions with '@version.property.name@' syntax.
     * @param version
     * @param properties
     * @return resolved version if any property expression of version itself.
     * @throws LifecycleExecutionException
     */
    default String resolveVersionProperty(String version, Properties properties) throws LifecycleExecutionException {
        Matcher matcher = VERSION_PROPERTY_PATTERN.matcher(version);
        if (matcher.matches()) {
            String propertyName = matcher.group("propertyName");
            if (!properties.containsKey(propertyName)) {
                throw new LifecycleExecutionException(String.format("Unable to resolve version property '%s' - property must be set as Maven property", propertyName));
            }
            return properties.get(propertyName).toString();
        }

        return version;
    }

}
