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

package org.citrusframework.yaks.maven.extension.configuration.properties;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.assertj.core.api.Assertions;
import org.citrusframework.yaks.maven.extension.configuration.LoggingConfigurationLoader;
import org.citrusframework.yaks.maven.extension.configuration.TestHelper;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class PropertyFileLoggingConfigurationLoaderTest {

    private final PropertyFileLoggingConfigurationLoader loader = new PropertyFileLoggingConfigurationLoader();

    private final ConsoleLogger logger = new ConsoleLogger();
    private final ConfigurationBuilder<BuiltConfiguration> builder = LoggingConfigurationLoader.newConfigurationBuilder();

    @Test
    public void shouldLoadFromPropertyFile() throws LifecycleExecutionException, URISyntaxException {
        Optional<Level> rootLevel = loader.load(TestHelper.getClasspathResource("citrus.properties"), builder, logger);
        Assert.assertTrue(rootLevel.isPresent());
        Assert.assertEquals(Level.INFO, rootLevel.get());

        TestHelper.verifyLoggingConfiguration(builder);
    }

    @Test
    public void shouldHandleNonExistingFile() {
        Assertions.assertThatExceptionOfType(LifecycleExecutionException.class)
                .isThrownBy(() -> loader.load(Paths.get("doesNotExist"), builder, logger));
    }

}
