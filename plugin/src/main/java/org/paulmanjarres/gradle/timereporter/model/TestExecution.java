package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.gradle.api.tasks.testing.TestResult;

@Value
@AllArgsConstructor
@ToString
public class TestExecution {

    /** Test class name */
    String className;

    /** The name of the test */
    String name;

    /** The duration of the Test */
    Duration duration;

    /** Result of the test */
    TestResult.ResultType result;

    TestSuite parentSuite;
}
