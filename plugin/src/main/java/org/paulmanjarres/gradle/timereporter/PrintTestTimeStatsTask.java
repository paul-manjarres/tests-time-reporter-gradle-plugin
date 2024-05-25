package org.paulmanjarres.gradle.timereporter;

import java.util.Map;
import java.util.Set;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.testing.TestResult;
import org.paulmanjarres.gradle.timereporter.model.*;
import org.paulmanjarres.gradle.timereporter.utils.ConsoleUtils;

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

    @Input
    @Optional
    public abstract Property<Boolean> getColoredOutput();

    @Input
    @Optional
    public abstract Property<Boolean> getShowHistogram();

    @TaskAction
    public void print() {
        process();
    }

    final ConsoleUtils cUtils = new ConsoleUtils(true);

    public void process() {

        final Set<TestTimeExecutionStats> stats = this.getTestListener().get().getStats();
        int binSize = this.getBinSize().get();
        int slowThreshold = this.getSlowThreshold().get();
        int longestTestCount = this.getLongestTestsCount().get();
        boolean showGroupByResult = this.getShowGroupByResult().get();
        boolean showGroupByClass = this.getShowGroupByClass().get();
        boolean showSlowestTests = this.getShowSlowestTests().get();
        boolean showHistogram = this.getShowHistogram().get();
        boolean coloredOutput = this.getColoredOutput().get();
        boolean experimentalFeatures = this.getExperimentalFeatures().get();
        int maxResultsForGroupByClass = this.getMaxResultsForGroupByClass().get();

        this.getLogger().info("longestTestCount = {}", longestTestCount);
        this.getLogger().info("maxResultsForGroupByClass = {}", maxResultsForGroupByClass);
        this.getLogger().info("slowThreshold = {}", slowThreshold);
        this.getLogger().info("showGroupByResult = {}", showGroupByResult);
        this.getLogger().info("showGroupByClass = {}", showGroupByClass);
        this.getLogger().info("showSlowestTests = {}", showSlowestTests);
        this.getLogger().info("experimentalFeatures = {}", experimentalFeatures);
        this.getLogger().info("coloredOutput = {}", coloredOutput);
        this.getLogger().info("showHistogram = {}", showHistogram);
        this.getLogger().info("binSize = {}", binSize);

        final int totalTestCount = stats.size();

        cUtils.setColorEnabled(coloredOutput);

        this.getLogger().lifecycle(cUtils.printInYellow("========= Tests Time Execution Statistics =========="));
        this.getLogger().lifecycle("Total Test Count: [{}]", totalTestCount);
        // TODO: Total Time,
        logNewLine();

        if (showGroupByResult) {
            this.getLogger().lifecycle("Group By Result:");
            GroupedResultsByStatus.from(stats).forEach(r -> this.getLogger().lifecycle(formatGroupResultsByStatus(r)));
            logNewLine();
        }

        if (showGroupByClass) {
            this.getLogger().lifecycle("Group By Class - Max Results: [{}]", maxResultsForGroupByClass);
            GroupedResultsByClass.from(stats, maxResultsForGroupByClass)
                    .forEach(r -> this.getLogger().lifecycle(formatGroupResultsByClass(r)));
            logNewLine();
        }

        if (showHistogram) {
            final Histogram.HistogramConfig conf = new Histogram.HistogramConfig();
            conf.setBucketSize(binSize);
            conf.setMaxValue(slowThreshold);

            final Histogram h = Histogram.from(stats, conf);

            this.getLogger().lifecycle("Histogram - Total: [{}] ", h.getCount());

            for (int i = 0; i < h.getBuckets(); i++) {
                this.getLogger()
                        .lifecycle(
                                "[{}-{}ms] \t: {} \t [ {}]",
                                i * binSize,
                                (i + 1) * binSize,
                                h.getValues()[i],
                                String.format("%3.2f%%", h.getPercentages()[i]));
            }
            this.getLogger()
                    .lifecycle(
                            cUtils.printInYellow("[>{}ms] \t: {} \t [ {}]"),
                            h.getMaxValue(),
                            h.getSlowTestCount(),
                            String.format("%3.2f%%", h.getSlowTestPercentage()));
            logNewLine();
            this.getLogger()
                    .lifecycle(
                            cUtils.printInYellow("({}) {}  of tests were considered slow"),
                            h.getSlowTestCount(),
                            String.format("%3.2f%%", h.getSlowTestPercentage()));

            logNewLine();
        }

        if (showSlowestTests) {
            this.getLogger()
                    .lifecycle(
                            "Slowest tests - Threshold: [{}ms] - Max Results: [{}]", slowThreshold, longestTestCount);
            GroupedBySlowestTests.from(stats, longestTestCount, slowThreshold)
                    .forEach(r -> this.getLogger().lifecycle(formatSlowestTest(r)));
            logNewLine();
        }

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
        final ConsoleUtils.Color color = getConsoleTextColorBy(r.getType());
        final String text = String.format(
                "- %s : %3.2f%% (%d/%d)", r.getType(), r.getPercentage() * 100, r.getCount(), r.getTotal());
        return cUtils.print(text, color);
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

    private ConsoleUtils.Color getConsoleTextColorBy(TestResult.ResultType type) {
        if (type == TestResult.ResultType.FAILURE) {
            return ConsoleUtils.Color.RED;
        } else if (type == TestResult.ResultType.SUCCESS) {
            return ConsoleUtils.Color.GREEN;
        } else if (type == TestResult.ResultType.SKIPPED) {
            return ConsoleUtils.Color.CYAN;
        } else {
            return ConsoleUtils.Color.BLACK;
        }
    }
}
