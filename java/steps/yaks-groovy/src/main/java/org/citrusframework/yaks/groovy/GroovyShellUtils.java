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

package org.citrusframework.yaks.groovy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citrusframework.Citrus;
import org.citrusframework.context.TestContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * @author Christoph Deppisch
 */
public final class GroovyShellUtils {

    private static final Pattern COMMENTS = Pattern.compile("^(?:\\s*//|/\\*|\\s+\\*).*$", Pattern.MULTILINE);

    private GroovyShellUtils() {
        // prevent instantiation of utility class
    }

    /**
     * Run given scriptCode with GroovyShell.
     * @param ic import customizer
     * @param scriptCode code to evaluate in shell
     * @param context the current test context
     * @param <T> return type
     * @return script result
     */
    public static <T> T run(ImportCustomizer ic, String scriptCode, Citrus citrus, TestContext context) {
        return run(ic, null, scriptCode, citrus, context);
    }

    /**
     * Run given scriptCode with GroovyShell and delegate execution to given instance.
     * @param ic import customizer
     * @param delegate instance providing methods and properties
     * @param scriptCode code to evaluate in shell
     * @param context the current test context
     * @param <T> return type
     * @return script result
     */
    public static <T> T run(ImportCustomizer ic, Object delegate, String scriptCode, Citrus citrus, TestContext context) {
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.addCompilationCustomizers(ic);
        cc.setScriptBaseClass(GroovyScript.class.getName());

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        GroovyShell sh = new GroovyShell(cl, new Binding(), cc);

        Script script = sh.parse(scriptCode);

        if (script instanceof GroovyScript) {
            if (delegate != null) {
                // set the delegate target
                ((GroovyScript) script).setDelegate(delegate);
            }

            ((GroovyScript) script).setCitrusFramework(citrus);
            ((GroovyScript) script).setContext(context);
        }

        return (T) script.run();
    }

    /**
     * Remove leading comments such as license header.
     * @param script
     * @return
     */
    public static String removeComments(String script) {
        Matcher matcher = COMMENTS.matcher(script);
        if (matcher.find()) {
            return matcher.replaceAll("").trim();
        } else {
            return script.trim();
        }
    }

    /**
     * Remove package declaration.
     * @param script
     * @return
     */
    public static String removePackageDeclaration(String script) {
        if (script.startsWith("package ")) {
            return script.substring(script.indexOf("\n")).trim();
        } else {
            return script.trim();
        }
    }
}
