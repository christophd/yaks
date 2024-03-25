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

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Christoph Deppisch
 */
public class CucumberUtilsTest {

    @Test
    public void extractFeatureFileName() {
        Assert.assertEquals("", CucumberUtils.extractFeatureFileName((String) null));
        Assert.assertEquals("", CucumberUtils.extractFeatureFileName(""));
        Assert.assertEquals("foo.feature", CucumberUtils.extractFeatureFileName("foo.feature"));
        Assert.assertEquals("foo.feature", CucumberUtils.extractFeatureFileName("/foo.feature"));
        Assert.assertEquals("foo.feature", CucumberUtils.extractFeatureFileName("classpath:org/citrusframework/yaks/foo/foo.feature"));
    }
}
