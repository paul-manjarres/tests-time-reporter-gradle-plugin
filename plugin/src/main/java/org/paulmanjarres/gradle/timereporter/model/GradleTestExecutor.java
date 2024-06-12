package org.paulmanjarres.gradle.timereporter.model;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Represent an instance of a Gradle test executor.
 * Cannot contain tests, only child test suites.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GradleTestExecutor extends GradleTest {
    @Override
    public String toString() {

        final String duration =
                this.getDuration() == null ? "null" : "" + this.getDuration().toMillis();
        return "GradleTestExecutor(name='" + this.getName() + "', duration=" + duration + "ms, childrenSize="
                + (this.getChildren() == null ? 0 : this.getChildren().size())
                + ')';
    }

    @Override
    public Set<GradleTestSuite> getTestSuites() {
        return this.getChildren().stream().map(GradleTestSuite.class::cast).collect(Collectors.toSet());
    }
}
