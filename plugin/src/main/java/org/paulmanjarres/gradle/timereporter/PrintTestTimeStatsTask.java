package org.paulmanjarres.gradle.timereporter;

import java.util.HashSet;
import java.util.List;
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

        final Set<TestTimeExecutionStats> stats = this.getTestListener().get().getStats();
        final Map<String, TestSuiteTimeExecutionStats> sStats =
                this.getTestListener().get().getSuiteStats();
        final int totalTestCount = stats.size();

        java.util.Optional<String> globalSuiteKey = sStats.keySet().stream()
                .filter(s -> s.toLowerCase().contains("gradle test run"))
                .findFirst();

        final long totalSuiteTime =
                globalSuiteKey.map(s -> sStats.get(s).getDuration().toMillis()).orElse(1L);

        cUtils.setColorEnabled(coloredOutput);

        this.getLogger().lifecycle(cUtils.printInYellow("========== Tests Time Execution Statistics =========="));
        this.getLogger().lifecycle("Total Test Count: [{}]", totalTestCount);
        this.getLogger().lifecycle("Total Test time: [{}ms]", totalSuiteTime);
        logNewLine();

        if (showGroupByResult) {
            this.getLogger().lifecycle("Tests grouped by Result:");
            GroupedResultsByStatus.from(stats).forEach(r -> this.getLogger().lifecycle(formatGroupResultsByStatus(r)));
            logNewLine();
        }

        if (showGroupByClass) {
            this.getLogger().lifecycle("Tests grouped by Class - Max Results: [{}]", maxResultsForGroupByClass);
            GroupedResultsByClass.fromSuiteStats(new HashSet<>(sStats.values()), maxResultsForGroupByClass)
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
                            "Histogram - Total: [{}] - BinSize: [{}ms] - Slow Threshold: [{}ms] - TotalSuite Time: [{}ms]",
                            h.getCount(),
                            binSize,
                            slowThreshold,
                            totalSuiteTime);

            for (int i = 0; i < h.getBuckets(); i++) {
                this.getLogger()
                        .lifecycle(
                                " [{} - {}ms] : ({})  [ {}]  - {}ms - {}%",
                                String.format("%4d", i * binSize),
                                String.format("%4d", (i + 1) * binSize),
                                String.format("%4d", h.getValues()[i]),
                                String.format("%5.2f%%", h.getPercentages()[i]),
                                String.format("%4d", h.getDuration()[i]),
                                String.format("%5.2f", h.getDuration()[i] * 100 / (double) totalSuiteTime));
            }

            this.getLogger()
                    .lifecycle(
                            " [{}] : ({})  [ {}]  - {}ms - {}%",
                            cUtils.printInRed(String.format("     > %4dms", h.getMaxValue())),
                            cUtils.printInRed(String.format("%4d", h.getSlowTestCount())),
                            cUtils.printInRed(String.format("%5.2f%%", h.getSlowTestPercentage())),
                            String.format("%4d", h.getSlowTestDuration()),
                            String.format("%5.2f", h.getSlowTestDuration() * 100 / (double) totalSuiteTime));

            logNewLine();
            this.getLogger()
                    .lifecycle(
                            "{} of the tests ({}) were considered slow. Approximately {} of total time.",
                            cUtils.printInYellow(String.format("%3.2f%%", h.getSlowTestPercentage())),
                            h.getSlowTestCount(),
                            cUtils.printInYellow(
                                    String.format("%3.2f%%", h.getSlowTestDuration() * 100 / (double) totalSuiteTime)));
            logNewLine();
        }

        if (showSlowestTests) {

            final List<TestTimeExecutionStats> group = GroupedBySlowestTests.from(stats, slowThreshold);
            this.getLogger()
                    .lifecycle(
                            "Slowest tests ({}) - Threshold: [{}ms] - Max Results: [{}]",
                            group.size(),
                            slowThreshold,
                            longestTestCount);
            group.stream().limit(longestTestCount).forEach(r -> this.getLogger().lifecycle(formatSlowestTest(r)));
            logNewLine();
        }

        if (experimentalFeatures) {
            sStats.forEach((k, v) -> this.getLogger()
                    .lifecycle(
                            "Key:[{}] - Name: [{}] - #Tests: [{}] - Duration:[{}ms] - InitTime: [{}ms]",
                            k,
                            v.getClassName(),
                            v.getNumberOfTests(),
                            v.getDuration().toMillis(),
                            v.getInitTimeMillis()));
            logNewLine();

            sStats.values().forEach(t -> this.getLogger().lifecycle(t.toString()));
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
                " - %5.2f%% (%3d/%3d) Time: [%4dms] : %s",
                r.getPercentage() * 100,
                r.getTestCountPerSuite(),
                r.getTotalTestCount(),
                r.getSuiteExecutionTime(),
                r.getTestClassName());
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
