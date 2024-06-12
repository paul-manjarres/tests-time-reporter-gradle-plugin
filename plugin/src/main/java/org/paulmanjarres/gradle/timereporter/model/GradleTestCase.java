package org.paulmanjarres.gradle.timereporter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represent a single test run by gradle.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@ToString
public class GradleTestCase extends GradleTest {
    /** Test class name */
    String className;
}
