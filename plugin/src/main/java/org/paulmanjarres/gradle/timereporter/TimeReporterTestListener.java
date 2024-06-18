package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.paulmanjarres.gradle.timereporter.model.*;

/**
 * A test listener to check the results on each test.
 *
 * @author <a href="mailto:paul.manjarres@gmail.com">Jean Paul Manjarres Correal</a>
 * @since 0.1.0
 */
@AllArgsConstructor
public class TimeReporterTestListener implements TestListener {

    private final Map<String, GradleTest> suiteStats;

    private Logger log;

    @Override
    public void beforeSuite(TestDescriptor suite) {

        final String parentName =
                suite.getParent() != null ? suite.getParent().getName() : PluginConstants.ROOT_NODE_NAME;
        final GradleTest parent = suiteStats.getOrDefault(parentName, GradleTestRun.ROOT);
        log.info("beforeSuite Name:{} - Parent: {}", suite.getName(), parent);
        final String suiteName = suite.getName();

        final GradleTest gtSuite = createGradleTest(suite);
        gtSuite.setParent(parent);
        gtSuite.setStartTime(System.currentTimeMillis()); // TODO esta medida es confiable?

        this.suiteStats.put(suiteName, gtSuite);
        if (suiteStats.containsKey(parentName) && !(parent instanceof GradleTestCase)) {
            parent.addChildren(gtSuite);
        }
    }

    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {
        final Duration duration = Duration.ofMillis(result.getEndTime() - result.getStartTime());
        final GradleTest sStats = this.suiteStats.get(suite.getName());
        sStats.setDuration(duration);
        sStats.setEndTime(result.getEndTime());
        sStats.setResult(result.getResultType());
        log.info("AfterSuite Name:{} - Result: {} - Class: {}", suite.getName(), result, suite.getClassName());

        if (sStats instanceof GradleTestSuite) {
            final GradleTestSuite gtSuite = (GradleTestSuite) sStats;
            gtSuite.setNumberOfTests((int) result.getTestCount());
        }
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {
        log.info("beforeTest Test:{} - Parent: {}", testDescriptor.getName(), testDescriptor.getParent());
        if (testDescriptor.getParent() == null) {
            log.lifecycle("WARN beforeTest() -> Parent for Test: {} is NULL", testDescriptor.getName());
            return;
        }

        final GradleTestSuite suite =
                (GradleTestSuite) this.suiteStats.get(testDescriptor.getParent().getName());
        if (suite.getInitTimeMillis() == 0) {
            suite.setInitTimeMillis(System.currentTimeMillis() - suite.getStartTime());
        }
    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        log.info(
                "AfterTest Name:{} - Result: {} - Parent:{} - Class:{}",
                testDescriptor.getName(),
                result,
                testDescriptor.getParent(),
                testDescriptor.getParent().getClassName());
        final GradleTestCase testInstance = GradleTestCase.builder()
                .className(testDescriptor.getClassName())
                .name(testDescriptor.getName())
                .duration(Duration.ofMillis(result.getEndTime() - result.getStartTime()))
                .result(result.getResultType())
                .build();

        if (testDescriptor.getParent() == null) {
            log.lifecycle("WARN Parent for Test: {} is NULL", testDescriptor.getName());
            return;
        }

        final String parentName = testDescriptor.getParent().getName();
        final GradleTest parent = suiteStats.getOrDefault(parentName, GradleTestRun.ROOT);
        if (!(parent instanceof GradleTestSuite)) {
            log.lifecycle(
                    "WARN Parent for Test: {} is not of type GradleSuite, type:", testDescriptor.getName(), parent);
            return;
        }
        parent.addChildren(testInstance);
        testInstance.setParent(parent);
    }

    protected GradleTest createGradleTest(TestDescriptor suite) {
        if (isGradleTestExecutor(suite.getName())) {
            return createGradleTestExecutor(suite);
        } else if (isGradleTestRun(suite.getName())) {
            return createGradleTestRun(suite);
        } else {
            return createGradleTestSuite(suite);
        }
    }

    public GradleTestSuite createGradleTestSuite(TestDescriptor suite) {
        return GradleTestSuite.builder()
                .name(suite.getName())
                .className(suite.getClassName())
                .build();
    }

    protected GradleTestRun createGradleTestRun(TestDescriptor suite) {
        return GradleTestRun.builder().name(suite.getName()).build();
    }

    protected GradleTestExecutor createGradleTestExecutor(TestDescriptor suite) {
        return GradleTestExecutor.builder().name(suite.getName()).build();
    }

    protected boolean isGradleTestExecutor(String name) {
        return name != null && name.toLowerCase().startsWith("gradle test executor");
    }

    protected boolean isGradleTestRun(String name) {
        return name != null && name.toLowerCase().startsWith("gradle test run");
    }

    public Map<String, GradleTest> getSuiteStats() {
        return new HashMap<>(suiteStats);
    }
}
