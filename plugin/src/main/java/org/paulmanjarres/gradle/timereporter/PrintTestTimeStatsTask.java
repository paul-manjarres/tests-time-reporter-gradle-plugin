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
        this.getLogger().lifecycle("Tests Time Execution Statistics");
        logNewLine();
        process();
    }

    public void process() {

        final Set<TestTimeExecutionStats> stats = this.getTestListener().get().getStats();
        Integer binSize = this.getBinSize().get();
        Integer slowThreshold = this.getSlowThreshold().get();
        Integer longestTestCount = this.getLongestTestsCount().get();

        int totalTestCount = stats.size();
        this.getLogger().lifecycle("Total Test Count: {}", totalTestCount);
        logNewLine();

        this.getLogger().lifecycle("Group By Result:");
        GroupedResultsByStatus.from(stats).forEach(r -> this.getLogger().lifecycle(formatGroupResultsByStatus(r)));

        logNewLine();
        this.getLogger().lifecycle("Group By Class:");
        GroupedResultsByClass.from(stats).forEach(r -> this.getLogger().lifecycle(formatGroupResultsByClass(r)));
        logNewLine();

        this.getLogger()
                .lifecycle("Slowest tests - Threshold: {}ms - Max Results: {}", slowThreshold, longestTestCount);
        GroupedBySlowestTests.from(stats, longestTestCount, slowThreshold)
                .forEach(r -> this.getLogger().lifecycle(formatSlowestTest(r)));

        logNewLine();

        // Histogram  toggle

    }

    public String formatGroupResultsByStatus(GroupedResultsByStatus r) {
        return String.format(
                " - %s : %3.2f%% (%d/%d)", r.getType(), r.getPercentage() * 100, r.getCount(), r.getTotal());
    }

    public String formatGroupResultsByClass(GroupedResultsByClass r) {
        return String.format(
                " - %3.2f%% (%d/%d) : %s ", r.getPercentage() * 100, r.getCount(), r.getTotal(), r.getTestClassName());
    }

    public String formatSlowestTest(TestTimeExecutionStats r) {
        return String.format(
                " %4d ms - %s - %s.%s ",
                r.getDuration().toMillis(), r.getResult(), r.getTestClassName(), r.getTestName());
    }

    public void logNewLine() {
        this.getLogger().lifecycle(" ");
    }
}
