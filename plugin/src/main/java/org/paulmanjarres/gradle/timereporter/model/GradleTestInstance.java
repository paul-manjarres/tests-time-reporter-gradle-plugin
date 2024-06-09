package org.paulmanjarres.gradle.timereporter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gradle.api.tasks.testing.TestResult;

/**
 * Represent a single test run by gradle.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString
public class GradleTestInstance extends GradleTest {

    /** Test class name */
    String className;

    /** Result of the test */
    TestResult.ResultType result;
}
