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
    private int numberOfTests;

    /** The estimated time between the start of the suite and the start of the first test */
    private long initTimeMillis;
}
