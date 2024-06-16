package org.paulmanjarres.gradle.timereporter;

import static org.paulmanjarres.gradle.timereporter.model.PluginConstants.EXTENSION_NAME;
import static org.paulmanjarres.gradle.timereporter.model.PluginConstants.PRINT_TEST_TIME_STATS_TASK_NAME;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.NotNull;
import org.paulmanjarres.gradle.timereporter.model.PluginConstants;
import org.paulmanjarres.gradle.timereporter.model.PluginExtensionDefaultValues;

/**
 * The Plugin main class.
 * @author <a href="mailto:paul.manjarres@gmail.com">Jean Paul Manjarres Correal</a>
 */
public class TestTimeReporterPlugin implements Plugin<Project> {

    private static final GradleVersion MINIMUM_SUPPORTED_VERSION = GradleVersion.version("6.9");

    public void apply(@NotNull Project project) {

        verifyGradleVersion(GradleVersion.current());

        final TestTimeReporterExtension extension = registerExtension(project);
        setExtensionDefaultValues(extension);

        final TimeReporterTestListener listener = new TimeReporterTestListener();
        registerTask(project, listener, extension);

        final TaskCollection<Test> testTasks = project.getTasks().withType(Test.class);
        project.getLogger().info("[{}] - Test tasks count: {}", EXTENSION_NAME, testTasks.size());

        testTasks.configureEach(it -> {
            it.addTestListener(listener);
            it.finalizedBy(PRINT_TEST_TIME_STATS_TASK_NAME);
            project.getLogger()
                    .info(
                            "[{}] - Adding finalizer [{}] on task: {}",
                            EXTENSION_NAME,
                            PRINT_TEST_TIME_STATS_TASK_NAME,
                            it.getName());
        });
    }

    void verifyGradleVersion(GradleVersion version) {
        if (version.compareTo(MINIMUM_SUPPORTED_VERSION) < 0) {
            String message = String.format(
                    "Gradle version %s is unsupported. Please use %s or later.", version, MINIMUM_SUPPORTED_VERSION);
            throw new IllegalArgumentException(message);
        }
    }

    TestTimeReporterExtension registerExtension(Project project) {
        project.getLogger().info("[{}] - Registering extension {}", EXTENSION_NAME, EXTENSION_NAME);
        return project.getExtensions().create(PluginConstants.EXTENSION_NAME, TestTimeReporterExtension.class);
    }

    void registerTask(Project project, TimeReporterTestListener listener, TestTimeReporterExtension extension) {
        project.getTasks().register(PRINT_TEST_TIME_STATS_TASK_NAME, PrintTestTimeStatsTask.class, task -> {
            task.getTestListener().set(listener);
            task.getLongestTestsCount().set(extension.getLongestTestsCount());
            task.getBinSize().set(extension.getBinSizeInMillis());
            task.getSlowThreshold().set(extension.getSlowThresholdInMillis());
            task.getShowGroupByClass().set(extension.getShowGroupByClass());
            task.getShowGroupByResult().set(extension.getShowGroupByResult());
            task.getShowSlowestTests().set(extension.getShowSlowestTests());
            task.getMaxResultsForGroupByClass().set(extension.getMaxResultsForGroupByClass());
            task.getExperimentalFeatures().set(extension.getExperimentalFeatures());
            task.getShowHistogram().set(extension.getShowHistogram());
            task.getColoredOutput().set(extension.getColoredOutput());
            task.getPluginEnabled().set(extension.getEnabled());
            task.getShowTreeView().set(extension.getShowTreeView());
            task.getShowSkipped().set(extension.getShowSkipped());
            task.getShowFailed().set(extension.getShowFailed());
        });
        project.getLogger().info("[{}] - Registered task {} ", EXTENSION_NAME, PRINT_TEST_TIME_STATS_TASK_NAME);
    }

    void setExtensionDefaultValues(TestTimeReporterExtension extension) {
        extension.getLongestTestsCount().convention(PluginExtensionDefaultValues.longestTestsCount);
        extension.getBinSizeInMillis().convention(PluginExtensionDefaultValues.binSizeInMillis);
        extension.getSlowThresholdInMillis().convention(PluginExtensionDefaultValues.slowThresholdInMillis);
        extension.getShowGroupByResult().convention(PluginExtensionDefaultValues.showGroupByResult);
        extension.getShowGroupByClass().convention(PluginExtensionDefaultValues.showGroupByClass);
        extension.getShowSlowestTests().convention(PluginExtensionDefaultValues.showSlowestTests);
        extension.getMaxResultsForGroupByClass().convention(PluginExtensionDefaultValues.maxResultsForGroupByClass);
        extension.getExperimentalFeatures().convention(PluginExtensionDefaultValues.experimentalFeatures);
        extension.getShowHistogram().convention(PluginExtensionDefaultValues.showHistogram);
        extension.getColoredOutput().convention(PluginExtensionDefaultValues.coloredOutput);
        extension.getEnabled().convention(PluginExtensionDefaultValues.ENABLED);
        extension.getShowTreeView().convention(PluginExtensionDefaultValues.SHOW_TREE_VIEW);
        extension.getShowSkipped().convention(PluginExtensionDefaultValues.SHOW_SKIPPED);
        extension.getShowFailed().convention(PluginExtensionDefaultValues.SHOW_FAILED);
    }
}
