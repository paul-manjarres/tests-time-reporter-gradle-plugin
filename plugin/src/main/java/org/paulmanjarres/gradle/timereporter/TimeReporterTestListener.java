package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.paulmanjarres.gradle.timereporter.model.TestExecution;
import org.paulmanjarres.gradle.timereporter.model.TestSuite;

/**
 * A test listener to check the results on each test.
 *
 * @author <a href="mailto:paul.manjarres@gmail.com">Jean Paul Manjarres Correal</a>
 * @since 0.1.0
 */
public class TimeReporterTestListener implements TestListener {

    private final Set<TestExecution> stats;
    private final Map<String, TestSuite> suiteStats;

    public TimeReporterTestListener() {
        stats = new HashSet<>();
        suiteStats = new HashMap<>();
    }

    @Override
    public void beforeSuite(TestDescriptor suite) {
        final String parentName = suite.getParent() != null ? suite.getParent().getName() : "root";

        final TestSuite st = TestSuite.builder()
                .name(suite.getName())
                .className(suite.getClassName())
                .parentName(parentName)
                .duration(null)
                .numberOfTests(0)
                .initTimeMillis(0L)
                .startTime(LocalDateTime.now())
                .build();

        this.suiteStats.put(suite.getName(), st);

        //        if (suiteStats.containsKey(parentName)) {
        //            TestSuite parent = suiteStats.get(parentName);
        //            if (st.isGradleTestRun() || st.isGradleTestExecutor()) {
        //                parent.addChildSuite(st);
        //            }
        //        }
    }

    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {
        final Duration duration = Duration.ofMillis(result.getEndTime() - result.getStartTime());
        final TestSuite sStats = this.suiteStats.get(suite.getName());
        sStats.setDuration(duration);
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {
        final TestSuite sStats = this.suiteStats.get(testDescriptor.getClassName());
        sStats.setNumberOfTests(sStats.getNumberOfTests() + 1);
        if (sStats.getInitTimeMillis() == 0) {
            sStats.setInitTimeMillis(
                    Duration.between(sStats.getStartTime(), LocalDateTime.now()).toMillis());
        }
    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        this.stats.add(new TestExecution(
                testDescriptor.getClassName(),
                testDescriptor.getName(),
                Duration.ofMillis(result.getEndTime() - result.getStartTime()),
                result.getResultType(),
                null));
    }

    public Set<TestExecution> getStats() {
        return new HashSet<>(stats);
    }

    public Map<String, TestSuite> getSuiteStats() {
        return new HashMap<>(suiteStats);
    }
}
