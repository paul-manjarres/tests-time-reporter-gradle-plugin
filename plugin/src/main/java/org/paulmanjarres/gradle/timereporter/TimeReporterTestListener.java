package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
public class TimeReporterTestListener implements TestListener {

    private final Set<GradleTestCase> stats;
    private final Map<String, GradleTest> suiteStats;

    public TimeReporterTestListener() {
        stats = new HashSet<>();
        suiteStats = new HashMap<>();
    }

    @Override
    public void beforeSuite(TestDescriptor suite) {
        final String parentName = suite.getParent() != null ? suite.getParent().getName() : "root";
        final GradleTest parent = suiteStats.getOrDefault(parentName, GradleTestRun.ROOT);

        GradleTest gSuite = null;
        if (isGradleTestExecutor(suite.getName())) {
            gSuite = createGradleTestExecutor(suite);
        } else if (isGradleTestRun(suite.getName())) {
            gSuite = createGradleTestRun(suite);
        } else {
            gSuite = createGradleTestSuite(suite);
        }

        gSuite.setParent(parent);

        this.suiteStats.put(suite.getName(), gSuite);
        if (suiteStats.containsKey(parentName) && !(parent instanceof GradleTestCase)) {
            parent.addChildren(gSuite);
        }
    }

    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {
        final Duration duration = Duration.ofMillis(result.getEndTime() - result.getStartTime());
        final GradleTest sStats = this.suiteStats.get(suite.getName());
        sStats.setDuration(duration);
        sStats.setStartTime(result.getStartTime());
        sStats.setEndTime(result.getEndTime());
        sStats.setResult(result.getResultType());
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {
        final GradleTestSuite suite = (GradleTestSuite) this.suiteStats.get(testDescriptor.getClassName());
        suite.increaseNumberOfTestsBy(1);
        if (suite.getInitTimeMillis() == 0) {
            suite.setInitTimeMillis(System.currentTimeMillis() - suite.getStartTime());
        }
    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        final GradleTestCase testInstance = GradleTestCase.builder()
                .className(testDescriptor.getClassName())
                .name(testDescriptor.getName())
                .duration(Duration.ofMillis(result.getEndTime() - result.getStartTime()))
                .result(result.getResultType())
                .build();
        this.stats.add(testInstance);
    }

    public GradleTestSuite createGradleTestSuite(TestDescriptor suite) {
        return GradleTestSuite.builder()
                .name(suite.getName())
                .className(suite.getClassName())
                .duration(null)
                //                .numberOfTests(0)
                //                .initTimeMillis(0L)
                .build();
    }

    public GradleTestRun createGradleTestRun(TestDescriptor suite) {
        return GradleTestRun.builder()
                .name(suite.getName())
                //                .duration(null)
                .build();
    }

    public GradleTestExecutor createGradleTestExecutor(TestDescriptor suite) {
        return GradleTestExecutor.builder()
                .name(suite.getName())
                //                .duration(null)
                .build();
    }

    public boolean isGradleTestExecutor(String name) {
        return name != null && name.toLowerCase().startsWith("gradle test executor");
    }

    public boolean isGradleTestRun(String name) {
        return name != null && name.toLowerCase().startsWith("gradle test run");
    }

    public Set<GradleTestCase> getStats() {
        return new HashSet<>(stats);
    }

    public Map<String, GradleTest> getSuiteStats() {
        return new HashMap<>(suiteStats);
    }
}
