package org.paulmanjarres.gradle.timereporter.model;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;
import org.gradle.api.tasks.testing.TestResult;

@AllArgsConstructor
@Value
@ToString
public class GroupedResultsByStatus {
    TestResult.ResultType type;
    double percentage;
    int count;
    int total;

    public static List<GroupedResultsByStatus> from(Set<TestTimeExecutionStats> stats) {
        return stats.stream().collect(Collectors.groupingBy(TestTimeExecutionStats::getResult)).entrySet().stream()
                .map(e -> new GroupedResultsByStatus(
                        e.getKey(),
                        e.getValue().size() / (double) stats.size(),
                        e.getValue().size(),
                        stats.size()))
                .sorted(Comparator.comparing(GroupedResultsByStatus::getPercentage)
                        .reversed())
                .collect(Collectors.toList());
    }
}
