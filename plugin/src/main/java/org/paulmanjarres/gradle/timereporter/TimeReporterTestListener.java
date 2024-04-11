package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.paulmanjarres.gradle.timereporter.model.TestSuiteTimeExecutionStats;
import org.paulmanjarres.gradle.timereporter.model.TestTimeExecutionStats;

public class TimeReporterTestListener implements TestListener {

    private final Set<TestTimeExecutionStats> stats;
    private final Map<String, TestSuiteTimeExecutionStats> suiteStats;

    public TimeReporterTestListener() {
        stats = new HashSet<>();
        suiteStats = new HashMap<>();
    }

    @Override
    public void beforeSuite(TestDescriptor suite) {
        this.suiteStats.put(
                suite.getClassName(),
                new TestSuiteTimeExecutionStats(
                        suite.getName(), suite.getDisplayName(), null, 0, 0, LocalDateTime.now()));
    }

    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {
        Duration duration = Duration.ofMillis(result.getEndTime() - result.getStartTime());
        TestSuiteTimeExecutionStats sStats = this.suiteStats.get(suite.getClassName());
        sStats.setDuration(duration);
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {
        TestSuiteTimeExecutionStats sStats = this.suiteStats.get(testDescriptor.getClassName());
        sStats.setNumberOfTests(sStats.getNumberOfTests() + 1);
        if (sStats.getInitTimeMillis() == 0) {
            sStats.setInitTimeMillis(
                    Duration.between(sStats.getStartTime(), LocalDateTime.now()).toMillis());
        }
    }

    @Override
    public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        this.stats.add(new TestTimeExecutionStats(
                testDescriptor.getClassName(),
                testDescriptor.getName(),
                Duration.ofMillis(result.getEndTime() - result.getStartTime()),
                result.getResultType()));
    }

    public Set<TestTimeExecutionStats> getStats() {
        return new HashSet<>(stats);
    }

    public Map<String, TestSuiteTimeExecutionStats> getSuiteStats() {
        return new HashMap<>(suiteStats);
    }
}
