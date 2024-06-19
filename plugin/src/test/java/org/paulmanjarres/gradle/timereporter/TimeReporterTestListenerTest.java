package org.paulmanjarres.gradle.timereporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;
import org.junit.jupiter.api.Test;
import org.paulmanjarres.gradle.timereporter.model.GradleTest;
import org.paulmanjarres.gradle.timereporter.model.GradleTestCase;
import org.paulmanjarres.gradle.timereporter.model.GradleTestRun;
import org.paulmanjarres.gradle.timereporter.model.GradleTestSuite;

class TimeReporterTestListenerTest {

    @Test
    void whenNewSuiteExecuted_then_isRegisteredByListener() {

        // GIVEN
        TimeReporterTestListener tl = new TimeReporterTestListener(new HashMap<>(), mock(Logger.class));
        TestDescriptor td = mock(TestDescriptor.class);
        TestResult result = mock(TestResult.class);

        // WHEN
        when(td.getParent()).thenReturn(null);
        when(td.getName()).thenReturn("suite1");
        when(td.getClassName()).thenReturn("org.paulmanjarres.tests");
        when(result.getStartTime()).thenReturn(0L);
        when(result.getEndTime()).thenReturn(1000L);

        tl.beforeSuite(td);
        tl.afterSuite(td, result);

        // THEN
        assertThat(tl.getSuiteStats()).isNotNull().isNotEmpty().hasSize(1);
        final GradleTest suite = tl.getSuiteStats().get("suite1");
        assertThat(suite).isNotNull();
        assertThat(suite.getDuration()).isEqualByComparingTo(Duration.ofMillis(1000));
    }

    @Test
    void whenListenerGetsData_then_SuiteDataShouldBeComplete() {

        // GIVEN
        TimeReporterTestListener tl = new TimeReporterTestListener(new HashMap<>(), mock(Logger.class));

        TestDescriptor run = mock(TestDescriptor.class);
        TestDescriptor executor = mock(TestDescriptor.class);

        TestDescriptor suite1 = mock(TestDescriptor.class);
        TestDescriptor innerSuite = mock(TestDescriptor.class);

        TestDescriptor test1 = mock(TestDescriptor.class);
        TestDescriptor test2 = mock(TestDescriptor.class);
        TestDescriptor test3 = mock(TestDescriptor.class);

        TestResult resultOK = mock(TestResult.class);
        TestResult resultSuite1 = mock(TestResult.class);
        TestResult resultInnerSuite = mock(TestResult.class);

        when(run.getName()).thenReturn("gradle test run");
        when(run.getParent()).thenReturn(null);
        when(executor.getName()).thenReturn("gradle test executor");
        when(executor.getParent()).thenReturn(run);

        when(suite1.getName()).thenReturn("suite1");
        when(suite1.getParent()).thenReturn(executor);
        when(suite1.getClassName()).thenReturn("class1");
        when(innerSuite.getName()).thenReturn("innerSuite");
        when(innerSuite.getParent()).thenReturn(suite1);
        when(innerSuite.getClassName()).thenReturn(null);

        when(resultOK.getResultType()).thenReturn(TestResult.ResultType.SUCCESS);
        when(resultSuite1.getResultType()).thenReturn(TestResult.ResultType.SUCCESS);
        when(resultSuite1.getTestCount()).thenReturn(1L);
        when(resultInnerSuite.getResultType()).thenReturn(TestResult.ResultType.SUCCESS);
        when(resultInnerSuite.getTestCount()).thenReturn(2L);

        when(test1.getName()).thenReturn("Test1");
        when(test1.getParent()).thenReturn(innerSuite);
        when(test2.getName()).thenReturn("Test2");
        when(test2.getParent()).thenReturn(innerSuite);
        when(test3.getName()).thenReturn("Test3");
        when(test3.getParent()).thenReturn(suite1);

        tl.beforeSuite(run);
        tl.beforeSuite(executor);
        tl.beforeSuite(suite1);
        tl.beforeSuite(innerSuite);
        tl.beforeTest(test1);
        tl.afterTest(test1, resultOK);
        tl.beforeTest(test2);
        tl.afterTest(test2, resultOK);
        tl.afterSuite(innerSuite, resultInnerSuite);
        tl.beforeTest(test3);
        tl.afterTest(test3, resultOK);
        tl.afterSuite(suite1, resultSuite1);
        tl.afterSuite(executor, resultOK);
        tl.afterSuite(run, resultOK);

        final Map<String, GradleTest> stats = tl.getSuiteStats();
        assertThat(stats).isNotNull().isNotEmpty();

        final GradleTest gtRun = stats.get("gradle test run");
        assertThat(gtRun).isNotNull().isInstanceOf(GradleTestRun.class);
        assertThat(gtRun.countTests()).isEqualTo(3);
        assertThat(gtRun.getChildren()).isNotNull().hasSize(1);

        Set<GradleTestCase> testCases = gtRun.getTestCases();
        assertThat(testCases).isNotNull().isNotEmpty().hasSize(3);

        Set<String> names = testCases.stream().map(GradleTest::getName).collect(Collectors.toSet());
        assertThat(names).contains("Test1").contains("Test2").contains("Test3");

        Set<GradleTestSuite> testSuites = gtRun.getTestSuites();
        assertThat(testSuites).isNotNull().isNotEmpty().hasSize(2);

        GradleTestSuite gtSuite1 = testSuites.stream()
                .filter(s -> s.getName().equals("suite1"))
                .findFirst()
                .orElse(null);

        assertThat(gtSuite1).isNotNull();
        assertThat(gtSuite1.getTestSuites()).isNotNull().isNotEmpty().hasSize(1);
    }
}
