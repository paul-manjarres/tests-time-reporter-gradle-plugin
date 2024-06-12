package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import java.util.Collections;
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

    /**
     * Counts the total amount of tests that exists in all the hierarchy of elements.
     * @return int
     */
    public int countTests() {
        int count = 0;
        if (this.children == null) {
            return count;
        }
        for (GradleTest t : this.children) {
            count += t.countTests();
        }
        return count;
    }

    /**
     * Returns the list of all tests cases related to this object.
     * Collects all the test cases down in the hierarchy.
     * @return Set of {@link GradleTestCase}
     */
    public Set<GradleTestCase> getTestCases() {
        if (this.children == null) {
            return Collections.emptySet();
        }
        final Set<GradleTestCase> testCases = new HashSet<>();
        for (GradleTest t : this.children) {
            testCases.addAll(t.getTestCases());
        }
        return testCases;
    }

    public Set<GradleTestSuite> getTestSuites() {
        if (this.children == null) {
            return Collections.emptySet();
        }
        final Set<GradleTestSuite> testSuites = new HashSet<>();
        for (GradleTest t : this.children) {
            testSuites.addAll(t.getTestSuites());
        }
        return testSuites;
    }
}
