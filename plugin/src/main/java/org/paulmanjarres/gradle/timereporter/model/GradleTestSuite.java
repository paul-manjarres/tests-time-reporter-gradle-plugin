package org.paulmanjarres.gradle.timereporter.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GradleTestSuite extends GradleTest {

    /** The class name used in the suite if any */
    private String className;

    /** The total number of tests in this suite */
    @Setter(AccessLevel.NONE)
    private int numberOfTests;

    /** The estimated time between the start of the suite and the start of the first test */
    private long initTimeMillis;

    @Override
    public String toString() {

        final String duration =
                this.getDuration() == null ? "null" : "" + this.getDuration().toMillis();
        return "GradleTestSuite(" + "name='" + this.getName()
                + "', duration=" + duration
                + "ms, numberOfTests=" + this.getNumberOfTests() + ", childrenSize="
                + (this.getChildren() == null ? 0 : this.getChildren().size())
                + ", className='" + this.getClassName()
                + "')";
    }

    public void increaseNumberOfTestsBy(int increment) {
        this.numberOfTests = this.numberOfTests + increment;
    }
}
