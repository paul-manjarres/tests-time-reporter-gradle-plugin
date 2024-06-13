package org.paulmanjarres.gradle.timereporter;

import java.util.HashSet;
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
import org.paulmanjarres.gradle.timereporter.model.views.GradleTestTreeView;
import org.paulmanjarres.gradle.timereporter.model.views.SlowestTestsView;
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

        cUtils.setColorEnabled(coloredOutput);

        this.getLogger().lifecycle(cUtils.yellow("========== Tests Time Execution Statistics =========="));
        logNewLine();

        final Map<String, List<GradleTest>> suitesByParentName = sStats.values().stream()
                .collect(Collectors.groupingBy(s -> s.getParent().getName()));

        final List<GradleTest> runSuites = suitesByParentName.get("root");

        runSuites.forEach(s -> {
            String name = (s instanceof GradleTestRun) ? ((GradleTestRun) s).getSimplifiedName() : s.getName();
            this.getLogger().lifecycle(cUtils.magenta("{}"), name);
            this.getLogger()
                    .lifecycle(
                            "  - Test Count: [{}] - Test time: [{}ms]",
                            s.countTests(),
                            s.getDuration().toMillis());
        });

        logNewLine();

        if (showGroupByResult) {
            this.getLogger().lifecycle("Tests grouped by Result:");
            runSuites.forEach(s -> {
                String name = (s instanceof GradleTestRun) ? ((GradleTestRun) s).getSimplifiedName() : s.getName();
                this.getLogger().lifecycle(cUtils.magenta("{}"), name);
                GroupedResultsByStatus.from(s.getTestCases())
                        .forEach(r -> this.getLogger().lifecycle(formatGroupResultsByStatus(r)));
                logNewLine();
            });
        }

        // TODO Flag
        logNewLine();
        final GradleTestTreeView treeView = new GradleTestTreeView();
        treeView.printTreeView(new HashSet<>(sStats.values()), cUtils, this.getLogger());
        logNewLine();

        if (showSlowestTests) {
            final SlowestTestsView slowestTestsView =
                    new SlowestTestsView(cUtils, getLogger(), slowThreshold, longestTestCount);
            slowestTestsView.printView(new HashSet<>(sStats.values()));
            logNewLine();
        }

        if (showHistogram) {

            runSuites.forEach(s -> {
                String name = (s instanceof GradleTestRun) ? ((GradleTestRun) s).getSimplifiedName() : s.getName();
                this.getLogger().lifecycle(cUtils.magenta("{}"), name);

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
                                s.getDuration().toMillis());

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
                                    String.format(
                                            "%5.2f",
                                            h.getDuration()[i]
                                                    * 100
                                                    / (double) s.getDuration().toMillis()));
                }

                this.getLogger()
                        .lifecycle(
                                " [{}] : ({}/{}) - [{}]  - {}ms - {}%",
                                cUtils.printInRed(String.format("     > %4dms", h.getMaxValue())),
                                cUtils.printInRed(String.format("%4d", h.getSlowTestCount())),
                                cUtils.printInRed(String.format("%4d", h.getCount())),
                                cUtils.printInRed(String.format("%5.2f%%", h.getSlowTestPercentage())),
                                String.format("%4d", h.getSlowTestDuration()),
                                String.format(
                                        "%5.2f",
                                        h.getSlowTestDuration()
                                                * 100
                                                / (double) s.getDuration().toMillis()));

                logNewLine();
                this.getLogger()
                        .lifecycle(
                                "{} of the tests ({}) were considered slow. Approximately {} of total time.",
                                cUtils.yellow(String.format("%3.2f%%", h.getSlowTestPercentage())),
                                h.getSlowTestCount(),
                                cUtils.yellow(String.format(
                                        "%3.2f%%",
                                        h.getSlowTestDuration()
                                                * 100
                                                / (double) s.getDuration().toMillis())));
                logNewLine();
            });
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
