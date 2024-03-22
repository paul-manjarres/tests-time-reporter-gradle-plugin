package org.paulmanjarres.gradle.timereporter;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;

public class TimeReporterTestListener implements TestListener {

  private Set<TestTimeStats> stats;

  public TimeReporterTestListener() {
    stats = new HashSet<>();
  }

  @Override
  public void beforeSuite(TestDescriptor suite) {}

  @Override
  public void afterSuite(TestDescriptor suite, TestResult result) {}

  @Override
  public void beforeTest(TestDescriptor testDescriptor) {}

  @Override
  public void afterTest(TestDescriptor testDescriptor, TestResult result) {
    //System.out.println("JEANNN!! " + testDescriptor.getName());
    this.stats.add(
        new TestTimeStats(
            testDescriptor.getClassName() + "." + testDescriptor.getName(),
            Duration.ofMillis(result.getEndTime() - result.getStartTime()),
            result.getResultType().name()));
  }

  public Set<TestTimeStats> getStats() {
    return stats;
  }
}
