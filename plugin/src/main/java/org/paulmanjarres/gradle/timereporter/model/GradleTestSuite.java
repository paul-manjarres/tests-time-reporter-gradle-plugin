package org.paulmanjarres.gradle.timereporter.model;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GradleTestSuite extends GradleTest {

    /** The class name used in the suite if any */
    private String className;

    /** The total number of tests in this suite */
    private int numberOfTests;

    /** The estimated time between the start of the suite and the start of the first test */
    private long initTimeMillis;

    @Override
    public int countTests() {
        return this.numberOfTests;
    }

    @Override
    public Set<GradleTestCase> getTestCases() {
        return this.getChildren().stream().map(GradleTestCase.class::cast).collect(Collectors.toSet());
    }

    @Override
    public Set<GradleTestSuite> getTestSuites() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        final String duration =
                this.getDuration() == null ? "null" : this.getDuration().toMillis() + "ms";
        return "GradleTestSuite(" + "name='" + this.getName()
                + "', duration=" + duration
                + ", initTime=" + initTimeMillis
                + ", numberOfTests=" + this.getNumberOfTests() + ", childrenSize="
                + (this.getChildren() == null ? 0 : this.getChildren().size())
                + ", className='" + this.getClassName()
                + "')";
    }
}
