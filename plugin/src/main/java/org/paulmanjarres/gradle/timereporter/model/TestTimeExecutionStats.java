package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class TestTimeExecutionStats {
    /** The name of the test */
    String testName;

    /** The duration of the Test */
    Duration duration;

    /** Result of the test */
    String result;
}
