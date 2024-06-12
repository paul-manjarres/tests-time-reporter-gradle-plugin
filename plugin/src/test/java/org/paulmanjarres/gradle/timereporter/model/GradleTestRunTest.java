package org.paulmanjarres.gradle.timereporter.model;

import org.assertj.core.api.Assertions;
import org.gradle.api.tasks.testing.TestResult;
import org.junit.jupiter.api.Test;

class GradleTestRunTest {

    @Test
    void countTests_whenCalled_shouldReturnTheTotalNumberOfTests() {

        GradleTestExecutor executor1 =
                GradleTestExecutor.builder().name("executor1").build();
        GradleTestSuite s = createSuiteOf("s1", 20);

        executor1.addChildren(s);
        executor1.addChildren(createSuiteOf("s2", 10));
        executor1.addChildren(createSuiteOf("s3", 5));

        GradleTestExecutor executor2 =
                GradleTestExecutor.builder().name("executor2").build();
        executor2.addChildren(createSuiteOf("s4", 8));
        executor2.addChildren(createSuiteOf("s5", 12));
        executor2.addChildren(createSuiteOf("s6", 2));

        GradleTestRun tr = GradleTestRun.builder().name("run").build();
        tr.addChildren(executor1);

        GradleTestRun tr2 = GradleTestRun.builder().name("run").build();

        tr2.addChildren(executor1);
        tr2.addChildren(executor2);

        Assertions.assertThat(tr.countTests()).isEqualTo(35);
        Assertions.assertThat(tr2.countTests()).isEqualTo(57);
        Assertions.assertThat(executor1.countTests()).isEqualTo(35);
        Assertions.assertThat(executor2.countTests()).isEqualTo(22);
        Assertions.assertThat(s.countTests()).isEqualTo(20);
    }

    @Test
    void getTestCases_whenCalled_shouldReturnAllTheTestCases() {

        final GradleTestExecutor executor1 =
                GradleTestExecutor.builder().name("executor1").build();

        final GradleTestSuite s1 = createSuiteOf("s1", 4);
        s1.addChildren(createTestCase("test1"));
        s1.addChildren(createTestCase("test2"));
        s1.addChildren(createTestCase("test3"));
        s1.addChildren(createTestCase("test4"));

        executor1.addChildren(s1);

        final GradleTestSuite s2 = createSuiteOf("s2", 3);
        s2.addChildren(createTestCase("test5"));
        s2.addChildren(createTestCase("test6"));
        s2.addChildren(createTestCase("test7"));

        final GradleTestExecutor executor2 =
                GradleTestExecutor.builder().name("executor2").build();

        executor2.addChildren(s1);
        executor2.addChildren(s2);

        Assertions.assertThat(executor1.getTestCases()).isNotEmpty().hasSize(4);
        Assertions.assertThat(executor2.getTestCases()).isNotEmpty().hasSize(7);
    }

    @Test
    void getTestSuites_whenCalled_shouldReturnAllTheTestSuites() {

        final GradleTestExecutor executor1 =
                GradleTestExecutor.builder().name("executor1").build();

        final GradleTestSuite s1 = createSuiteOf("s1", 4);
        final GradleTestSuite s2 = createSuiteOf("s2", 3);
        final GradleTestSuite s3 = createSuiteOf("s3", 2);

        executor1.addChildren(s1);
        executor1.addChildren(s2);
        executor1.addChildren(s3);

        Assertions.assertThat(executor1.getTestSuites()).isNotEmpty().hasSize(3);

        final GradleTestExecutor executor2 =
                GradleTestExecutor.builder().name("executor2").build();

        final GradleTestSuite s4 = createSuiteOf("s4", 4);
        final GradleTestSuite s5 = createSuiteOf("s5", 3);

        executor2.addChildren(s4);
        executor2.addChildren(s5);

        Assertions.assertThat(executor2.getTestSuites()).isNotEmpty().hasSize(2);

        GradleTestRun tr = GradleTestRun.builder().name("run").build();
        tr.addChildren(executor1);
        tr.addChildren(executor2);

        Assertions.assertThat(tr.getTestSuites()).isNotEmpty().hasSize(5);
    }

    private GradleTestSuite createSuiteOf(String name, int n) {
        return GradleTestSuite.builder().name(name).numberOfTests(n).build();
    }

    private GradleTestCase createTestCase(String name) {
        return GradleTestCase.builder()
                .name(name)
                .result(TestResult.ResultType.SUCCESS)
                .build();
    }
}
