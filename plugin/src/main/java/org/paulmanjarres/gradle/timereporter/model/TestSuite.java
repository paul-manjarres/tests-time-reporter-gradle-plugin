package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
public class TestSuite {

    /** The name of the test */
    private String name;

    /** The name of the parent suite */
    private String parentName;

    /** The class name used in the suite if any */
    private String className;

    /** The duration of the Suite */
    private Duration duration;

    /** The total number of tests in this suite */
    private int numberOfTests;

    /** The estimated time between the start of the suite and the start of the first test */
    private long initTimeMillis;

    /** The approximate start time of the suite */
    private LocalDateTime startTime;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<TestSuite> suiteChildren;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<TestExecution> testChildren;

    public boolean isGradleTestRun() {
        return this.getName() != null && this.getName().toLowerCase().startsWith("gradle test run");
    }

    public boolean isGradleTestExecutor() {
        return this.getName() != null && this.getName().toLowerCase().startsWith("gradle test executor");
    }

    public void addChildSuite(TestSuite suite) {
        if (this.suiteChildren == null) {
            this.suiteChildren = new HashSet<>();
        }
        this.suiteChildren.add(suite);
    }

    public void addChildTest(TestExecution test) {
        if (this.testChildren == null) {
            this.testChildren = new HashSet<>();
        }
        this.testChildren.add(test);
    }
}
