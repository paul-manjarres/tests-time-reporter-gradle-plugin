package org.paulmanjarres.gradle.timereporter;


import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;
import org.junit.jupiter.api.Test;
import org.paulmanjarres.gradle.timereporter.model.TestSuite;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeReporterTestListenerTest {

    @Test
    void whenNewSuiteExecuted_then_isRegisteredByListener(){

        // GIVEN
        TimeReporterTestListener tl = new TimeReporterTestListener();
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
        final TestSuite suite = tl.getSuiteStats().get("suite1");
        assertThat(suite).isNotNull();
        assertThat(suite.getDuration()).isEqualByComparingTo(Duration.ofMillis(1000));
    }

}
