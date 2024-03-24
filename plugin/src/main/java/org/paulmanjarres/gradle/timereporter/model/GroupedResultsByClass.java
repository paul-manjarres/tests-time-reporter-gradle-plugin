package org.paulmanjarres.gradle.timereporter.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@AllArgsConstructor
@ToString
public class GroupedResultsByClass {
    String testClassName;
    double percentage;
    int count;
    int total;

    public static List<GroupedResultsByClass> from(Set<TestTimeExecutionStats> stats) {
        return stats.stream()
                .collect(Collectors.groupingBy(TestTimeExecutionStats::getTestClassName))
                .entrySet()
                .stream()
                .map(e -> new GroupedResultsByClass(
                        e.getKey(),
                        e.getValue().size() / (double) stats.size(),
                        e.getValue().size(),
                        stats.size()))
                .collect(Collectors.toList());
    }
}
