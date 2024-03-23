package org.paulmanjarres.gradle.timereporter.model;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupedBySlowestTests {

    public static List<TestTimeExecutionStats> from(Set<TestTimeExecutionStats> stats, int limit, int threshold) {
        return stats.stream()
                .sorted(Comparator.comparing(TestTimeExecutionStats::getDuration)
                        .reversed())
                .filter(r -> r.getDuration().toMillis() > threshold)
                .limit(limit)
                .collect(Collectors.toList());
    }
}
