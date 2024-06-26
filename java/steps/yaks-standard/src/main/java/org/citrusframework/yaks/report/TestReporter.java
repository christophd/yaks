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

package org.citrusframework.yaks.report;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citrusframework.cucumber.CitrusReporter;
import io.cucumber.java.PendingException;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import org.citrusframework.yaks.YaksSettings;
import org.citrusframework.yaks.util.CucumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reporter writing test results summary to termination log. This information will be accessible via
 * pod container status details.
 *
 * @author Christoph Deppisch
 */
public class TestReporter extends CitrusReporter {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(TestReporter.class);

    private final Pattern featureNamePattern = Pattern.compile("^Feature:(.+)$", Pattern.MULTILINE);

    private final TestResults testResults = new TestResults();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, this::saveTestResult);
        publisher.registerHandlerFor(TestCaseStarted.class, this::addTestDetail);
        publisher.registerHandlerFor(TestStepFinished.class, this::checkStepErrors);
        publisher.registerHandlerFor(TestSourceRead.class, this::setSuiteName);
        publisher.registerHandlerFor(TestRunFinished.class, this::printReports);
        super.setEventPublisher(publisher);
    }

    private void addTestDetail(TestCaseStarted event) {
        testResults.addTestResult(new TestResult(event.getTestCase().getId(), event.getTestCase().getName(),
                CucumberUtils.extractFeatureFileName(event.getTestCase().getUri()) + ":" + event.getTestCase().getLine()));
    }

    /**
     * Adds step error to test results.
     * @param event
     */
    private void checkStepErrors(TestStepFinished event) {
        if (event.getTestStep() instanceof HookTestStep) {
            return;
        }

        Optional<TestResult> testDetail = testResults.getTests().stream()
                .filter(detail -> detail.getId().equals(event.getTestCase().getId()))
                .findFirst();

        if (event.getResult().getError() != null) {
            if (testDetail.isPresent()) {
                testDetail.get().setCause(event.getResult().getError());
            } else {
                testResults.addTestResult(new TestResult(event.getTestCase().getId(), event.getTestCase().getName(),
                        CucumberUtils.extractFeatureFileName(event.getTestCase().getUri()) + ":" + event.getTestCase().getLine(), event.getResult().getError()));
            }
        }

        if (event.getResult().getStatus().is(Status.PENDING) || event.getResult().getStatus().is(Status.UNDEFINED)) {
            Exception cause = new PendingException("The scenario has pending or undefined step(s)");
            if (testDetail.isPresent()) {
                testDetail.get().setCause(cause);
            } else {
                testResults.addTestResult(new TestResult(event.getTestCase().getId(), event.getTestCase().getName(),
                        CucumberUtils.extractFeatureFileName(event.getTestCase().getUri()) + ":" + event.getTestCase().getLine(), cause));
            }
        }
    }

    /**
     * Sets the suite name either from feature name extracted from source or from source file path.
     * @param event
     */
    private void setSuiteName(TestSourceRead event) {
        Matcher featureNameMatcher = featureNamePattern.matcher(event.getSource());
        if (featureNameMatcher.find()) {
            testResults.setSuiteName(featureNameMatcher.group(1).trim());
        } else if (event.getUri().getSchemeSpecificPart() != null) {
            testResults.setSuiteName(event.getUri().getSchemeSpecificPart());
        }
    }

    /**
     * Prints test results to termination log.
     * @param event
     */
    private void printReports(TestRunFinished event) {
        try (Writer terminationLogWriter = Files.newBufferedWriter(YaksSettings.getTerminationLog(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            terminationLogWriter.write(testResults.toJson());
            terminationLogWriter.flush();
        } catch (IOException e) {
            LOG.warn(String.format("Failed to write termination logs to file '%s'", YaksSettings.getTerminationLog()), e);
        }
    }

    /**
     * Save test result for later reporting.
     * @param event
     */
    private void saveTestResult(TestCaseFinished event) {
        switch (event.getResult().getStatus()) {
            case FAILED:
                testResults.getSummary().failed++;
                break;
            case PASSED:
                testResults.getSummary().passed++;
                break;
            case PENDING:
                testResults.getSummary().pending++;
                break;
            case UNDEFINED:
                testResults.getSummary().undefined++;
                break;
            case SKIPPED:
                testResults.getSummary().skipped++;
                break;
            default:
        }
    }
}
