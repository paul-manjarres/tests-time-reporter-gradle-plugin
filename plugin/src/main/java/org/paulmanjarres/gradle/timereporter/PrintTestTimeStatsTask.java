package org.paulmanjarres.gradle.timereporter;

import java.util.Map;
import java.util.Set;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.paulmanjarres.gradle.timereporter.model.*;

public abstract class PrintTestTimeStatsTask extends DefaultTask {

    @Input
    public abstract Property<TimeReporterTestListener> getTestListener();

    @Input
    @Optional
    public abstract Property<Integer> getLongestTestsCount();

    @Input
    @Optional
    public abstract Property<Integer> getMaxResultsForGroupByClass();

    @Input
    @Optional
    public abstract Property<Integer> getBinSize();

    @Input
    @Optional
    public abstract Property<Integer> getSlowThreshold();

    @Input
    @Optional
    public abstract Property<Boolean> getShowGroupByResult();

    @Input
    @Optional
    public abstract Property<Boolean> getShowGroupByClass();

    @Input
    @Optional
    public abstract Property<Boolean> getShowSlowestTests();

    @Input
    @Optional
    public abstract Property<Boolean> getExperimentalFeatures();

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
        Boolean showGroupByResult = this.getShowGroupByResult().get();
        Boolean showGroupByClass = this.getShowGroupByClass().get();
        Boolean showSlowestTests = this.getShowSlowestTests().get();
        Boolean experimentalFeatures = this.getExperimentalFeatures().get();
        int maxResultsForGroupByClass = this.getMaxResultsForGroupByClass().get();

        this.getLogger().info("longestTestCount = {}", longestTestCount);
        this.getLogger().info("maxResultsForGroupByClass = {}", maxResultsForGroupByClass);
        this.getLogger().info("slowThreshold = {}", slowThreshold);
        this.getLogger().info("showGroupByResult = {}", showGroupByResult);
        this.getLogger().info("showGroupByClass = {}", showGroupByClass);
        this.getLogger().info("showSlowestTests = {}", showSlowestTests);
        this.getLogger().info("experimentalFeatures = {}", experimentalFeatures);
        this.getLogger().info("binSize = {}", binSize);

        int totalTestCount = stats.size();
        this.getLogger().lifecycle("Total Test Count: {}", totalTestCount);
        // TODO: Total Time,
        logNewLine();

        if (showGroupByResult) {
            this.getLogger().lifecycle("Group By Result:");
            GroupedResultsByStatus.from(stats).forEach(r -> this.getLogger().lifecycle(formatGroupResultsByStatus(r)));
            logNewLine();
        }

        if (showGroupByClass) {
            this.getLogger().lifecycle("Group By Class - Max Results: {}", maxResultsForGroupByClass);
            GroupedResultsByClass.from(stats, maxResultsForGroupByClass)
                    .forEach(r -> this.getLogger().lifecycle(formatGroupResultsByClass(r)));
            logNewLine();
        }

        if (showSlowestTests) {
            this.getLogger()
                    .lifecycle("Slowest tests - Threshold: {}ms - Max Results: {}", slowThreshold, longestTestCount);
            GroupedBySlowestTests.from(stats, longestTestCount, slowThreshold)
                    .forEach(r -> this.getLogger().lifecycle(formatSlowestTest(r)));
            logNewLine();
        }

        // Histogram  toggle

        // Suite stats
        if (experimentalFeatures) {
            final Map<String, TestSuiteTimeExecutionStats> sStats =
                    this.getTestListener().get().getSuiteStats();

            sStats.forEach((k, v) -> this.getLogger()
                    .lifecycle(
                            "{} - # Tests: {} -  Duration: {}ms Init Time: {}ms",
                            v.getClassName(),
                            v.getNumberOfTests(),
                            v.getDuration().toMillis(),
                            v.getInitTimeMillis()));

            logNewLine();
        }
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
                "[%4d ms] - %s - %s.%s ",
                r.getDuration().toMillis(), r.getResult(), r.getTestClassName(), r.getTestName());
    }

    public void logNewLine() {
        this.getLogger().lifecycle(" ");
    }
}
