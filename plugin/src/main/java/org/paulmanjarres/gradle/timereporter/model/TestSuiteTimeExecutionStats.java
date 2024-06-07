package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@Builder
@ToString
public class TestSuiteTimeExecutionStats {

    /** The name of the test */
    String suiteName;

    private Optional<String> parentName;

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

    public static boolean isGradleSuite(@NotNull String name) {
        return name.equalsIgnoreCase("gradle test run");

        //        protected boolean isGradleSuite(String name) {
        //            name.startsWith('Gradle Test Executor') ||
        //                name.startsWith('Gradle suite') ||
        //                name.startsWith('Gradle test')
        //        }
    }
}
