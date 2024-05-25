package org.paulmanjarres.gradle.timereporter.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.gradle.api.tasks.testing.TestResult;
import org.junit.jupiter.api.Test;

class HistogramTest {

    final TestResult.ResultType SUCCESS = TestResult.ResultType.SUCCESS;
    final TestResult.ResultType FAILURE = TestResult.ResultType.FAILURE;

    @Test
    void test1() {

        final Set<TestTimeExecutionStats> set = new HashSet<>();
        set.add(new TestTimeExecutionStats("t1", "t1", Duration.of(101, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t2", "t2", Duration.of(150, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t3", "t3", Duration.of(450, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t4", "t4", Duration.of(690, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t5", "t4", Duration.of(800, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t6", "t6", Duration.of(50, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t6", "t6", Duration.of(45, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t6", "t6", Duration.of(387, ChronoUnit.MILLIS), SUCCESS));

        final Histogram.HistogramConfig conf = new Histogram.HistogramConfig();
        conf.setBucketSize(100);
        conf.setMaxValue(500);

        final Histogram h = Histogram.from(set, conf);

        assertThat(h).isNotNull();
        assertThat(h.getBuckets()).isEqualTo(5);
        assertThat(h.getSlowTestCount()).isEqualTo(2);
        assertThat(h.getMaxValue()).isEqualTo(500);
        assertThat(h.getValues()[0]).isEqualTo(2); // 0-100
        assertThat(h.getPercentages()[0]).isEqualTo(25); // 0-100
        assertThat(h.getValues()[1]).isEqualTo(2); // 100-200
        assertThat(h.getPercentages()[1]).isEqualTo(25); // 100-200
        assertThat(h.getValues()[2]).isZero(); // 200-300
        assertThat(h.getPercentages()[2]).isZero(); // 200 - 300
        assertThat(h.getValues()[3]).isEqualTo(1); // 300-4
        assertThat(h.getPercentages()[3]).isEqualTo(12.5d); // // 00
        assertThat(h.getValues()[4]).isEqualTo(1); // 400-500
        assertThat(Arrays.stream(h.getValues()).sum() + h.getSlowTestCount()).isEqualTo(set.size());
        assertThat(Arrays.stream(h.getPercentages()).sum() + h.getSlowTestPercentage())
                .isEqualTo(100);
    }

    @Test
    void test2() {
        final Set<TestTimeExecutionStats> set = new HashSet<>();
        set.add(new TestTimeExecutionStats("t4", "t4", Duration.of(690, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t5", "t4", Duration.of(800, ChronoUnit.MILLIS), SUCCESS));
        set.add(new TestTimeExecutionStats("t6", "t6", Duration.of(7000, ChronoUnit.MILLIS), SUCCESS));

        final Histogram.HistogramConfig conf = new Histogram.HistogramConfig();
        conf.setBucketSize(200);
        conf.setMaxValue(600);

        final Histogram h = Histogram.from(set, conf);

        assertThat(h).isNotNull();
        assertThat(h.getBuckets()).isEqualTo(3);
        assertThat(h.getSlowTestCount()).isEqualTo(3);
        assertThat(h.getMaxValue()).isEqualTo(600);
        assertThat(h.getValues()[0]).isZero(); // 0-200
        assertThat(h.getValues()[1]).isZero(); // 200-400
        assertThat(h.getValues()[2]).isZero(); // 400-600
        assertThat(Arrays.stream(h.getValues()).sum() + h.getSlowTestCount()).isEqualTo(set.size());
    }
}
