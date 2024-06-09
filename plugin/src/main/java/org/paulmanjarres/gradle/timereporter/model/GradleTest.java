package org.paulmanjarres.gradle.timereporter.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode
@SuperBuilder
public abstract class GradleTest {

    /** The name of the test */
    private String name;

    /** The duration of the Suite */
    private Duration duration;

    /** The approximate start time */
    private LocalDateTime startTime;

    @EqualsAndHashCode.Exclude
    private GradleTest parent;

    @EqualsAndHashCode.Exclude
    private Set<GradleTest> children;

    public void addChildren(GradleTest child) {
        if (this.children == null) {
            this.children = new HashSet<>();
        }
        this.children.add(child);
    }
}
