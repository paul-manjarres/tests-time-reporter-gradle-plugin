package org.paulmanjarres.gradle.timereporter.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class GradleTestRun extends GradleTest {
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GradleTestRun(");
        sb.append("name='");
        sb.append(this.getName());
        sb.append("', childrenSize=");
        sb.append(this.getChildren() == null ? 0 : this.getChildren().size());
        sb.append(')');
        return sb.toString();
    }
}
