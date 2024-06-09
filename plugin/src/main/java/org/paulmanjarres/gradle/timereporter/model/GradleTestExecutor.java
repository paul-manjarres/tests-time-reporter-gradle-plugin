package org.paulmanjarres.gradle.timereporter.model;

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
        final StringBuilder sb = new StringBuilder("GradleTestExecutor(");
        sb.append("name='");
        sb.append(this.getName());
        sb.append("', childrenSize=");
        sb.append(this.getChildren() == null ? 0 : this.getChildren().size());
        sb.append(')');
        return sb.toString();
    }
}
