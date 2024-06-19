package org.paulmanjarres.gradle.timereporter.model;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
        return this.numberOfTests + super.countTests();
    }

    @Override
    public Set<GradleTestCase> getTestCases() {
        final Set<GradleTestCase> directTestCases = this.getChildren().stream()
                .filter(GradleTestCase.class::isInstance)
                .map(GradleTestCase.class::cast)
                .collect(Collectors.toSet());
        directTestCases.addAll(super.getTestCases());
        return directTestCases;
    }

    @Override
    public String toString() {
        final String duration =
                this.getDuration() == null ? "null" : this.getDuration().toMillis() + "ms";
        final String parentName =
                this.getParent() == null ? "null" : this.getParent().getName();
        final int childrenSize =
                (this.getChildren() == null ? 0 : this.getChildren().size());
        return "GradleTestSuite(" + "name='" + this.getName()
                + "', duration=" + duration
                + ", initTime=" + initTimeMillis
                + ", numberOfTests=" + this.getNumberOfTests() + ", childrenSize="
                + childrenSize
                + ", parentName=" + parentName + ", className='" + this.getClassName() + "')";
    }
}
