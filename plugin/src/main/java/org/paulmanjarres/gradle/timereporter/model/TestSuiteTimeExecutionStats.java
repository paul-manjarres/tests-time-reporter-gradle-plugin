package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
@ToString
public class TestSuiteTimeExecutionStats {

    /** The name of the test */
    String suiteName;

    /** The class name used in the suite if any */
    String className;

    /** The duration of the Suite */
    Duration duration;

    /** The total number of tests in this suite */
    int numberOfTests;

    /** The estimated time between the start of the suite and the start of the first test */
    long initTimeMillis;

    /** The approximate start time of the suite */
    LocalDateTime startTime;
}
