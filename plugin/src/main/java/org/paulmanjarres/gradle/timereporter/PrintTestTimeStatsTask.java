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

        this.getLogger().debug("longestTestCount = {}", longestTestCount);
        this.getLogger().debug("maxResultsForGroupByClass = {}", maxResultsForGroupByClass);
        this.getLogger().debug("slowThreshold = {}", slowThreshold);
        this.getLogger().debug("showGroupByResult = {}", showGroupByResult);
        this.getLogger().debug("showGroupByClass = {}", showGroupByClass);
        this.getLogger().debug("showSlowestTests = {}", showSlowestTests);
        this.getLogger().debug("experimentalFeatures = {}", experimentalFeatures);
        this.getLogger().debug("coloredOutput = {}", coloredOutput);
        this.getLogger().debug("showHistogram = {}", showHistogram);
        this.getLogger().debug("binSize = {}", binSize);

        final int totalTestCount = stats.size();

        cUtils.setColorEnabled(coloredOutput);

        this.getLogger().lifecycle(cUtils.printInYellow("========== Tests Time Execution Statistics =========="));
        this.getLogger().lifecycle("Total Test Count: [{}]", totalTestCount);
        // TODO: Total Time,
        logNewLine();

        if (showGroupByResult) {
            this.getLogger().lifecycle("Tests grouped by Result:");
            GroupedResultsByStatus.from(stats).forEach(r -> this.getLogger().lifecycle(formatGroupResultsByStatus(r)));
            logNewLine();
        }

        if (showGroupByClass) {
            this.getLogger().lifecycle("Tests grouped by Class - Max Results: [{}]", maxResultsForGroupByClass);
            GroupedResultsByClass.from(stats, maxResultsForGroupByClass)
                    .forEach(r -> this.getLogger().lifecycle(formatGroupResultsByClass(r)));
            logNewLine();
        }

        if (showHistogram) {
            final Histogram.HistogramConfig conf = new Histogram.HistogramConfig();
            conf.setBucketSize(binSize);
            conf.setMaxValue(slowThreshold);
            final Histogram h = Histogram.from(stats, conf);
            this.getLogger()
                    .lifecycle(
                            "Histogram - Total: [{}] - BinSize: [{}ms] - Slow Threshold: [{}ms]",
                            h.getCount(),
                            binSize,
                            slowThreshold);

            for (int i = 0; i < h.getBuckets(); i++) {
                this.getLogger()
                        .lifecycle(
                                " [{} - {}ms] : {} [ {}]",
                                String.format("%4d", i * binSize),
                                String.format("%4d", (i + 1) * binSize),
                                String.format("%4d", h.getValues()[i]),
                                String.format("%5.2f%%", h.getPercentages()[i]));
            }

            this.getLogger()
                    .lifecycle(
                            " [{}] : {} [ {}]",
                            cUtils.printInRed(String.format("     > %4dms", h.getMaxValue())),
                            String.format("%4d", h.getSlowTestCount()),
                            cUtils.printInRed(String.format("%5.2f%%", h.getSlowTestPercentage())));
            logNewLine();
            this.getLogger()
                    .lifecycle(
                            "{} of the tests ({}) were considered slow. ",
                            cUtils.printInYellow(String.format("%3.2f%%", h.getSlowTestPercentage())),
                            h.getSlowTestCount());

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
                "- %s : %6.2f%% (%3d/%3d)", r.getType(), r.getPercentage() * 100, r.getCount(), r.getTotal());
        return cUtils.print(text, color);
    }

    public String formatGroupResultsByClass(GroupedResultsByClass r) {
        return String.format(
                " - %5.2f%% (%3d/%3d) : %s ",
                r.getPercentage() * 100, r.getCount(), r.getTotal(), r.getTestClassName());
    }

    public String formatSlowestTest(TestTimeExecutionStats r) {
        return String.format(
                "[%4d ms] - %s - %s.%s ",
                r.getDuration().toMillis(),
                cUtils.print(r.getResult().toString(), getConsoleTextColorBy(r.getResult())),
                r.getTestClassName(),
                r.getTestName());
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
