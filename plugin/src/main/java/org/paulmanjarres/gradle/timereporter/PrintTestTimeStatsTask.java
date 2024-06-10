package org.paulmanjarres.gradle.timereporter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
        try {
            process();
        } catch (Exception e) {
            getProject().getLogger().error("PrintTestTimeStatsTask - Exception: {}", e.getMessage(), e);
        }
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

        final Set<GradleTestCase> stats = this.getTestListener().get().getStats();
        final Map<String, GradleTest> sStats = this.getTestListener().get().getSuiteStats();
        final int totalTestCount = stats.size();

        // TODO Revisar
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
            Set<GradleTest> suites = sStats.values().stream()
                    .filter(GradleTestSuite.class::isInstance)
                    .collect(Collectors.toSet());

            GroupedResultsByClass.fromSuiteStats(suites, maxResultsForGroupByClass)
                    .forEach(r -> this.getLogger().lifecycle(formatGroupResultsByClass(r)));
            logNewLine();
        }

        if (showHistogram) {
            final Histogram.HistogramConfig conf = new Histogram.HistogramConfig();
            conf.setBucketSize(binSize);
            conf.setMaxValue(slowThreshold);
            // TODO histograma per run
            final Histogram h = Histogram.from(stats, conf);
            this.getLogger()
                    .lifecycle(
                            "Histogram - Total: [{}] - BinSize: [{}ms] - Slow Threshold: [{}ms] - TotalSuite Time: [{}ms]",
                            h.getCount(),
                            binSize,
                            slowThreshold,
                            totalSuiteTime);

            this.getLogger().lifecycle("================================================");

            for (int i = 0; i < h.getBuckets(); i++) {
                this.getLogger()
                        .lifecycle(
                                " [{} - {}ms] : ({}/{}) - [{}]  - {}ms - {}%",
                                String.format("%4d", i * binSize),
                                String.format("%4d", (i + 1) * binSize),
                                String.format("%4d", h.getValues()[i]),
                                String.format("%4d", h.getCount()),
                                String.format("%5.2f%%", h.getPercentages()[i]),
                                String.format("%4d", h.getDuration()[i]),
                                String.format("%5.2f", h.getDuration()[i] * 100 / (double) totalSuiteTime));
            }

            this.getLogger()
                    .lifecycle(
                            " [{}] : ({}/{}) - [{}]  - {}ms - {}%",
                            cUtils.printInRed(String.format("     > %4dms", h.getMaxValue())),
                            cUtils.printInRed(String.format("%4d", h.getSlowTestCount())),
                            cUtils.printInRed(String.format("%4d", h.getCount())),
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

            final List<GradleTestCase> group = GroupedBySlowestTests.from(stats, slowThreshold);
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

            try {
                this.getLogger().lifecycle("================================================");
                this.getLogger().lifecycle("====== EXPERIMENTAL =====");
                this.getLogger().lifecycle("================================================");

                this.getLogger().lifecycle("Suites values: ");
                sStats.values().forEach(t -> this.getLogger().lifecycle(" - " + t.toString()));

                this.logNewLine();
                this.getLogger().lifecycle("Test suites grouped by parent:");

                Map<String, List<GradleTest>> suitesGroupedByParentName = sStats.values().stream()
                        .collect(Collectors.groupingBy(s ->
                                s.getParent() == null ? "root" : s.getParent().getName()));

                suitesGroupedByParentName.forEach((key, value) ->
                        this.getLogger().lifecycle(" - Parent: [{}] - Children ({}): {}", key, value.size(), value));

                logNewLine();

                this.getLogger().lifecycle("Root children suites");
                List<GradleTest> rootSuites = suitesGroupedByParentName.get("root");

                for (GradleTest s : rootSuites) {
                    this.getLogger()
                            .lifecycle(
                                    "Name: {} - Duration: {}ms",
                                    s.getName(),
                                    s.getDuration().toMillis());

                    for (GradleTest g : s.getChildren()) {
                        this.getLogger()
                                .lifecycle(
                                        "   - Name: {} - Result:{} - Duration: {}ms",
                                        g.getName(),
                                        g.getResult(),
                                        g.getDuration().toMillis());

                        for (GradleTest h : g.getChildren()) {
                            this.getLogger()
                                    .lifecycle(
                                            "     - Name: {} - Result:{} - Duration: {}ms",
                                            h.getName(),
                                            h.getResult(),
                                            h.getDuration().toMillis());
                        }
                    }
                }

                //                rootSuites.forEach(s -> {
                //                    this.getLogger()
                //                            .lifecycle(
                //                                    "Name: {} - Duration: {}ms",
                //                                    s.getName(),
                //                                    s.getDuration().toMillis());
                //
                //                    if (s instanceof GradleTestExecutor) {
                //                        s.getChildren().forEach(t -> this.getLogger()
                //                                .lifecycle(
                //                                        "   - Name: {} - ChildrenSuiteCount: {} - Duration: {}ms",
                //                                        t.getName(),
                //                                        t.getChildren() == null
                //                                                ? 0
                //                                                : t.getChildren().size(),
                //                                        t.getDuration().toMillis()));
                //                    }
                //
                //                    suitesGroupedByParentName.get(s.getName()).forEach(t -> this.getLogger()
                //                            .lifecycle(
                //                                    "   - Name: {} - Result:{} - Duration: {}ms",
                //                                    t.getName(),
                //                                    t.getResult(),
                //                                    //
                // t.getNumberOfTests(),
                //                                    t.getDuration().toMillis()));
                //                });

                this.getLogger().lifecycle("================================================");
            } catch (Exception e) {
                this.getLogger().error("ERROR: {}", e.getMessage(), e);
            }
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

    public String formatSlowestTest(GradleTestCase r) {

        return String.format(
                "[%4d ms] - %s - %s.%s ",
                r.getDuration().toMillis(),
                cUtils.print(r.getResult().toString(), getConsoleTextColorBy(r.getResult())),
                r.getClassName(),
                r.getName());
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
