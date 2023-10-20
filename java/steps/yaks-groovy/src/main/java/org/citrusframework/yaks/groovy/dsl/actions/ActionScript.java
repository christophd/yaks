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

package org.citrusframework.yaks.groovy.dsl.actions;

import org.citrusframework.Citrus;
import org.citrusframework.TestActionRunner;
import org.citrusframework.context.TestContext;
import org.citrusframework.yaks.groovy.GroovyShellUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * @author Christoph Deppisch
 */
public class ActionScript {

    private final String script;
    private final Citrus citrus;
    private final TestContext context;

    public ActionScript(String script, Citrus citrus, TestContext context) {
        this.script = script;
        this.citrus = citrus;
        this.context = context;
    }

    public void execute(TestActionRunner runner) {
        ImportCustomizer ic = new ImportCustomizer();
        GroovyShellUtils.run(ic, new ActionsConfiguration(runner), normalize(script), citrus, context);
    }

    private String normalize(String script) {
        String normalized = GroovyShellUtils.removeComments(script);

        if (isActionScript(normalized)) {
            if (normalized.startsWith("$(")) {
                return String.format("$actions { %s }", normalized);
            }

            return normalized;
        } else {
            return String.format("$actions { $(%s) }", normalized);
        }
    }

    public static boolean isActionScript(String script) {
        return script.startsWith("$actions {") || script.startsWith("$actions{") ||
                script.startsWith("$finally {") || script.startsWith("$finally{") ||
                script.startsWith("$(");
    }
}
