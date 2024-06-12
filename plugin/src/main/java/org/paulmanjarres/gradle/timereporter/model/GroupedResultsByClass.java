package org.paulmanjarres.gradle.timereporter.model;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
@ToString
public class GroupedResultsByClass {
    String testClassName;
    double percentage;
    int testCountPerSuite;
    int totalTestCount;
    long totalExecutionTime;
    long suiteExecutionTime;
    double suiteExecutionTimePercentage;
    long initTime;

    public static List<GroupedResultsByClass> fromSuiteStats(Set<GradleTestSuite> stats, int limit) {

        int totalTestCount = stats.stream()
                .map(GradleTestSuite.class::cast)
                .mapToInt(GradleTestSuite::getNumberOfTests)
                .sum();

        final long totalSuiteTime = stats.stream()
                .map(GradleTestSuite.class::cast)
                .filter(s -> s.getClassName() == null)
                .filter(s -> s.getName().toLowerCase().contains("gradle test run"))
                .findFirst()
                .map(s -> s.getDuration().toMillis())
                .orElse(0L);

        return stats.stream()
                .map(GradleTestSuite.class::cast)
                .filter(s -> s.getClassName() != null)
                .map(t -> GroupedResultsByClass.builder()
                        .testClassName(t.getClassName())
                        .percentage(t.getNumberOfTests() / (double) totalTestCount)
                        .testCountPerSuite(t.getNumberOfTests())
                        .totalTestCount(totalTestCount)
                        .suiteExecutionTime(t.getDuration().toMillis())
                        .totalExecutionTime(totalSuiteTime)
                        .initTime(t.getInitTimeMillis())
                        .build())
                .sorted(Comparator.comparing(GroupedResultsByClass::getPercentage)
                        .reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
