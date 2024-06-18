package org.paulmanjarres.gradle.timereporter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.paulmanjarres.gradle.timereporter.model.GradleTest;
import org.paulmanjarres.gradle.timereporter.model.GradleTestRun;
import org.paulmanjarres.gradle.timereporter.model.PluginConstants;
import org.paulmanjarres.gradle.timereporter.model.views.GradleTestTreeView;
import org.paulmanjarres.gradle.timereporter.model.views.HistogramView;
import org.paulmanjarres.gradle.timereporter.model.views.SlowestTestsView;
import org.paulmanjarres.gradle.timereporter.model.views.TestResultsView;
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
    public abstract Property<Integer> getMaxResultsForTreeViewSuites();

    @Input
    @Optional
    public abstract Property<Integer> getBinSize();

    @Input
    @Optional
    public abstract Property<Integer> getSlowThreshold();

    @Input
    @Optional
    public abstract Property<Boolean> getPluginEnabled();

    @Input
    @Optional
    public abstract Property<Boolean> getShowGroupByResult();

    @Input
    @Optional
    public abstract Property<Boolean> getShowGroupByClass();

    @Input
    @Optional
    public abstract Property<Boolean> getShowTreeView();

    @Input
    @Optional
    public abstract Property<Boolean> getShowSkipped();

    @Input
    @Optional
    public abstract Property<Boolean> getShowFailed();

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

    private static final String SEPARATOR = "================================================";

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

        boolean pluginEnabled = this.getPluginEnabled().get();
        if (!pluginEnabled) {
            getLogger().info("Plugin is disabled. Finishing PrintTestTimeStatsTask task.");
            return;
        }

        int binSize = this.getBinSize().get();
        int slowThreshold = this.getSlowThreshold().get();
        int longestTestCount = this.getLongestTestsCount().get();
        boolean showGroupByResult = this.getShowGroupByResult().get();
        boolean showGroupByClass = this.getShowGroupByClass().get();
        boolean showSlowestTests = this.getShowSlowestTests().get();
        boolean showHistogram = this.getShowHistogram().get();
        boolean showTreeView = this.getShowTreeView().get();
        boolean showSkipped = this.getShowSkipped().get();
        boolean showFailed = this.getShowFailed().get();
        boolean coloredOutput = this.getColoredOutput().get();
        boolean experimentalFeatures = this.getExperimentalFeatures().get();
        int maxResultsForGroupByClass = this.getMaxResultsForGroupByClass().get();
        int maxResultsForTreeViewSuites = this.getMaxResultsForTreeViewSuites().get();

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

        final Map<String, GradleTest> sStats = this.getTestListener().get().getSuiteStats();
        cUtils.setColorEnabled(coloredOutput);

        this.getLogger().lifecycle(cUtils.cyan("========== Tests Time Execution Statistics (BETA) =========="));
        logNewLine();

        final List<GradleTestRun> runSuites = getGradleTestRun(sStats);

        runSuites.forEach(s -> {
            String name = s.getSimplifiedName();
            this.getLogger()
                    .lifecycle(
                            "{} - Test Count: [{}] - Test time: [{}ms]",
                            cUtils.magenta(name),
                            String.format("%,d", s.countTests()),
                            String.format("%,d", s.getDuration().toMillis()));
        });
        logNewLine();

        if (showGroupByResult) {
            final TestResultsView view = new TestResultsView(cUtils, getLogger(), showFailed, showSkipped);
            view.printView(runSuites);
            logNewLine();
        }

        if (showTreeView) {
            final GradleTestTreeView treeView =
                    new GradleTestTreeView(cUtils, getLogger(), maxResultsForTreeViewSuites);
            treeView.printTreeView(runSuites);
            logNewLine();
        }

        if (showSlowestTests) {
            final SlowestTestsView slowestTestsView =
                    new SlowestTestsView(cUtils, getLogger(), slowThreshold, longestTestCount);
            slowestTestsView.printView(runSuites);
            logNewLine();
        }

        if (showHistogram) {
            final HistogramView histogramViewView = new HistogramView(cUtils, getLogger(), slowThreshold, binSize);
            histogramViewView.printView(runSuites);
            logNewLine();
        }

        if (experimentalFeatures) {

            this.getLogger().lifecycle(SEPARATOR);
            this.getLogger().lifecycle("====== EXPERIMENTAL =====");
            this.getLogger().lifecycle(SEPARATOR);

            this.getLogger().lifecycle("Suites values: ");
            sStats.values().forEach(t -> this.getLogger().lifecycle(" - " + t.toString()));

            this.logNewLine();
            this.getLogger().lifecycle("Test suites grouped by parent:");

            Map<String, List<GradleTest>> suitesGroupedByParentName = sStats.values().stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getParent() == null ? "root" : s.getParent().getName()));

            suitesGroupedByParentName.forEach((key, value) ->
                    this.getLogger().lifecycle(" - Parent: [{}] - Children ({}): {}", key, value.size(), value));

            logNewLine();

            this.getLogger().lifecycle(SEPARATOR);
        }

        this.getLogger().lifecycle(cUtils.cyan("End of PrintTestTimeStatsTask"));
        this.getLogger().lifecycle(cUtils.cyan(SEPARATOR));
    }

    public void logNewLine() {
        this.getLogger().lifecycle(" ");
    }

    protected List<GradleTestRun> getGradleTestRun(Map<String, GradleTest> suitesByName) {
        return suitesByName.values().stream()
                .collect(Collectors.groupingBy(s -> s.getParent().getName()))
                .get(PluginConstants.ROOT_NODE_NAME)
                .stream()
                .map(GradleTestRun.class::cast)
                .collect(Collectors.toList());
    }
}
