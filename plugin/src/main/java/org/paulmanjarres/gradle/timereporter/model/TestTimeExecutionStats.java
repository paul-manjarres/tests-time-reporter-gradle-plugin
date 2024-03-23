package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.gradle.api.tasks.testing.TestResult;

@Value
@AllArgsConstructor
@ToString
public class TestTimeExecutionStats {

    /** Test class name */
    String testClassName;

    /** The name of the test */
    String testName;

    /** The duration of the Test */
    Duration duration;

    /** Result of the test */
    TestResult.ResultType result;
}
