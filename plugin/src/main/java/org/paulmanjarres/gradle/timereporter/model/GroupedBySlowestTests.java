package org.paulmanjarres.gradle.timereporter.model;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupedBySlowestTests {

    private GroupedBySlowestTests() {}

    public static List<GradleTestInstance> from(Set<GradleTestInstance> stats, int threshold) {
        return stats.stream()
                .sorted(Comparator.comparing(GradleTestInstance::getDuration).reversed())
                .filter(r -> r.getDuration().toMillis() >= threshold)
                .collect(Collectors.toList());
    }
}
