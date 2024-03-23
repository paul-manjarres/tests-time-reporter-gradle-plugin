package org.paulmanjarres.gradle.timereporter;

import java.util.Set;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.paulmanjarres.gradle.timereporter.model.GroupedBySlowestTests;
import org.paulmanjarres.gradle.timereporter.model.GroupedResultsByClass;
import org.paulmanjarres.gradle.timereporter.model.GroupedResultsByStatus;
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
        // System.out.println("Tests time execution results");
        process();
        this.getLogger().lifecycle("================");
    }

    public void process() {

        Set<TestTimeExecutionStats> stats = this.getTestListener().get().getStats();
        Integer binSize = this.getBinSize().get();
        Integer slowThreshold = this.getSlowThreshold().get();
        Integer longestTestCount = this.getLongestTestsCount().get();
        System.out.println("Bin size: " + binSize);
        System.out.println("slowThreshold: " + slowThreshold);
        System.out.println("longestTestCount: " + longestTestCount);

        // Slowest tests - toggle para mostrar detalles

        int totalTestCount = stats.size();
        System.out.println("\nTotal Test Count: " + totalTestCount);

        // Print Tests distribution by status
        System.out.println("\nGroup By Result:");
        GroupedResultsByStatus.from(stats).forEach(r -> System.out.println(formatGroupResultsByStatus(r)));

        this.getLogger().lifecycle("\nGroup By Class:");
        GroupedResultsByClass.from(stats).forEach(r -> System.out.println(formatGroupResultsByClass(r)));

        this.getLogger()
                .lifecycle("\nSlowest tests - Threshold: " + slowThreshold + " - Max Results: " + longestTestCount);
        GroupedBySlowestTests.from(stats, longestTestCount, slowThreshold)
                .forEach(r -> System.out.println(formatSlowestTest(r)));

        // Histogram  toggle
        //

    }

    public String formatGroupResultsByStatus(GroupedResultsByStatus r) {
        return String.format(" - %s : %3.2f%% (%d)", r.getType(), r.getPercentage() * 100, r.getCount());
    }

    public String formatGroupResultsByClass(GroupedResultsByClass r) {
        return String.format(" - %s : %3.2f%% (%d)", r.getTestClassName(), r.getPercentage() * 100, r.getCount());
    }

    public String formatSlowestTest(TestTimeExecutionStats r) {
        return String.format(
                " %4d ms - %s - %s.%s ",
                r.getDuration().toMillis(), r.getResult(), r.getTestClassName(), r.getTestName());
    }
}
