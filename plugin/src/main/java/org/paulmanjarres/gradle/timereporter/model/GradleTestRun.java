package org.paulmanjarres.gradle.timereporter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GradleTestRun extends GradleTest {

    public static final GradleTestRun ROOT =
            GradleTestRun.builder().name("root").build();

    @Override
    public String toString() {
        final String duration =
                this.getDuration() == null ? "null" : "" + this.getDuration().toMillis();
        return "GradleTestRun(" + "name='" + this.getName()
                + "', duration=" + duration + "ms, childrenSize="
                + (this.getChildren() == null ? 0 : this.getChildren().size())
                + ')';
    }

    public String getSimplifiedName() {
        if (this.getName() == null || !this.getName().contains("Gradle Test Run")) {
            return this.getName();
        }
        return this.getName().substring("Gradle Test Run".length()).trim();
    }
}
