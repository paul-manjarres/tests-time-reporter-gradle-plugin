package org.paulmanjarres.gradle.timereporter.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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

        final Set<GradleTestCase> set = new HashSet<>();

        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(101))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t2")
                .duration(Duration.ofMillis(150))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t3")
                .duration(Duration.ofMillis(450))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t4")
                .duration(Duration.ofMillis(690))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(800))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(50))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(45))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(387))
                .result(SUCCESS)
                .build());

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
        final Set<GradleTestCase> set = new HashSet<>();
        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(690))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(800))
                .result(SUCCESS)
                .build());
        set.add(GradleTestCase.builder()
                .name("t1")
                .duration(Duration.ofMillis(7000))
                .result(SUCCESS)
                .build());

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
