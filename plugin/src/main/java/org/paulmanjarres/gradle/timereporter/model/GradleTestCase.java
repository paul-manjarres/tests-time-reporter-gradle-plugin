package org.paulmanjarres.gradle.timereporter.model;

import java.util.Collections;
import java.util.Set;
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

    @Override
    public Set<GradleTestCase> getTestCases() {
        return Collections.emptySet();
    }
}
