package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.gradle.api.tasks.testing.TestResult;

@Data
@EqualsAndHashCode
@SuperBuilder
public abstract class GradleTest {

    /** The name of the test */
    private String name;

    /** The duration of the Suite */
    private Duration duration;

    /** The approximate start time */
    private long startTime;

    /** The approximate end time */
    private long endTime;

    /** Result of the test */
    TestResult.ResultType result;

    @EqualsAndHashCode.Exclude
    private GradleTest parent;

    @EqualsAndHashCode.Exclude
    private Set<GradleTest> children;

    public void addChildren(GradleTest child) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        this.children.add(child);
    }
}
