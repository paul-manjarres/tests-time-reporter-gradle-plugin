package org.paulmanjarres.gradle.timereporter;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.paulmanjarres.gradle.timereporter.model.TestTimeExecutionStats;

public abstract class PrintTestTimeStatsTask extends DefaultTask {

    @Input
    public abstract Property<TimeReporterTestListener> getTestListener();

    @Input
    @Optional
    public abstract Property<Integer> getLongestTestsCount();

    @Input
    @Optional
    public abstract Property<Integer> getBinSize();

    @Input
    @Optional
    public abstract Property<Integer> getSlowThreshold();

    @TaskAction
    public void print() {
        this.getLogger().lifecycle("Tests time execution results");
        System.out.println("Tests time execution results");
        process();
        System.out.println("================");
    }

    public void process() {

        Set<TestTimeExecutionStats> stats = this.getTestListener().get().getStats();
        Integer binSize = this.getBinSize().get();
        Integer slowThreshold = this.getSlowThreshold().get();
        System.out.println("Bin size: " + binSize);
        System.out.println("slowThreshold: " + slowThreshold);

        stats.stream()
                .sorted(Comparator.comparing(TestTimeExecutionStats::getDuration)
                        .reversed())
                .limit(10)
                .collect(Collectors.toList())
                .forEach(t -> {
                    System.out.println(
                            t.getDuration().toMillis() + "ms - \t " + t.getTestName() + " : " + t.getResult());
                });

        // Slower tests
        // buckets

    }
}
