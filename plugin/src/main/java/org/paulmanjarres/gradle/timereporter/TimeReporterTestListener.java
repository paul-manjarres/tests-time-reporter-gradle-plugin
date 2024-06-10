package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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

    private final Set<GradleTestInstance> stats;
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
        if (suiteStats.containsKey(parentName) && !(parent instanceof GradleTestInstance)) {
            parent.addChildren(gSuite);
        }
    }

    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {
        final Duration duration = Duration.ofMillis(result.getEndTime() - result.getStartTime());
        final GradleTest sStats = this.suiteStats.get(suite.getName());
        // TODO: Do we need the result?
        sStats.setDuration(duration);
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {
        final GradleTestSuite sStats = (GradleTestSuite) this.suiteStats.get(testDescriptor.getClassName());

        sStats.setNumberOfTests(sStats.getNumberOfTests() + 1);
        if (sStats.getInitTimeMillis() == 0) {
            sStats.setInitTimeMillis(
                    Duration.between(sStats.getStartTime(), LocalDateTime.now()).toMillis());
        }
    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        final GradleTestInstance testInstance = GradleTestInstance.builder()
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
                .numberOfTests(0)
                .initTimeMillis(0L)
                .startTime(LocalDateTime.now())
                .build();
    }

    public GradleTestRun createGradleTestRun(TestDescriptor suite) {
        return GradleTestRun.builder()
                .name(suite.getName())
                .duration(null)
                .startTime(LocalDateTime.now())
                .build();
    }

    public GradleTestExecutor createGradleTestExecutor(TestDescriptor suite) {
        return GradleTestExecutor.builder()
                .name(suite.getName())
                .duration(null)
                .startTime(LocalDateTime.now())
                .build();
    }

    public boolean isGradleTestExecutor(String name) {
        return name != null && name.toLowerCase().startsWith("gradle test executor");
    }

    public boolean isGradleTestRun(String name) {
        return name != null && name.toLowerCase().startsWith("gradle test run");
    }

    public Set<GradleTestInstance> getStats() {
        return new HashSet<>(stats);
    }

    public Map<String, GradleTest> getSuiteStats() {
        return new HashMap<>(suiteStats);
    }
}
