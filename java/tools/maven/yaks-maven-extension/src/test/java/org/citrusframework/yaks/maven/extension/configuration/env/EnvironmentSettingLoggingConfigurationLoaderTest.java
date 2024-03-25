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

package org.citrusframework.yaks.maven.extension.configuration.env;

import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.citrusframework.yaks.maven.extension.configuration.LoggingConfigurationLoader;
import org.citrusframework.yaks.maven.extension.configuration.TestHelper;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class EnvironmentSettingLoggingConfigurationLoaderTest {

    private final ConsoleLogger logger = new ConsoleLogger();
    private final ConfigurationBuilder<BuiltConfiguration> builder = LoggingConfigurationLoader.newConfigurationBuilder();

    @Test
    public void shouldLoadFromEnv() throws LifecycleExecutionException {
        EnvironmentSettingLoggingConfigurationLoader loader = new EnvironmentSettingLoggingConfigurationLoader() {
            @Override
            public String getEnvSetting(String name) {
                return "root=info,org.foo=debug,org.bar=warn";
            }
        };

        Optional<Level> rootLevel = loader.load(builder, logger);
        Assert.assertTrue(rootLevel.isPresent());
        Assert.assertEquals(Level.INFO, rootLevel.get());
        Configurator.initialize(builder.build());

        TestHelper.verifyLoggingConfiguration(builder);
    }

    @Test
    public void shouldHandleNonExistingSystemProperty() throws LifecycleExecutionException {
        EnvironmentSettingLoggingConfigurationLoader loader = new EnvironmentSettingLoggingConfigurationLoader();
        Optional<Level> rootLevel = loader.load(builder, logger);
        Assert.assertFalse(rootLevel.isPresent());
        TestHelper.verifyDefaultLoggingConfiguration(builder);
    }
}
