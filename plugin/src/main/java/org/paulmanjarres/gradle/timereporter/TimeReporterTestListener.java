package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.paulmanjarres.gradle.timereporter.model.TestTimeExecutionStats;

public class TimeReporterTestListener implements TestListener {

    private final Set<TestTimeExecutionStats> stats;

    public TimeReporterTestListener() {
        stats = new HashSet<>();
    }

    @Override
    public void beforeSuite(TestDescriptor suite) {}

    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {

        Duration duration = Duration.ofMillis(result.getEndTime() - result.getStartTime());
        System.out.println("Test Suite: " + suite.getName() + " Total Time: " + duration.toMillis() + " ms");
    }

    @Override
    public void beforeTest(TestDescriptor testDescriptor) {}

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
}
