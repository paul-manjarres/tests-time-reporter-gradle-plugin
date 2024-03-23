package org.paulmanjarres.gradle.timereporter;

import static org.paulmanjarres.gradle.timereporter.model.PluginConstants.PRINT_TEST_TIME_STATS_TASK_NAME;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;
import org.gradle.util.GradleVersion;
import org.paulmanjarres.gradle.timereporter.model.PluginConstants;

public class TestTimeReporterPlugin implements Plugin<Project> {

    private static final GradleVersion MINIMUM_SUPPORTED_VERSION = GradleVersion.version("7.0");

    public void apply(Project project) {

        //boolean isPluginApplied = project.getPluginManager().hasPlugin(PluginConstants.PLUGIN_ID);
        verifyGradleVersion(GradleVersion.current());

        final TestTimeReporterExtension extension = registerExtension(project);
        setExtensionDefaultValues(extension);

        final TimeReporterTestListener listener = new TimeReporterTestListener();
        registerTask(project, listener, extension);

        final TaskCollection<Test> testTasks = project.getTasks().withType(Test.class);
        testTasks.forEach(it -> {
            it.addTestListener(listener);
            it.finalizedBy().finalizedBy(PRINT_TEST_TIME_STATS_TASK_NAME);
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
        return project.getExtensions().create(PluginConstants.EXTENSION_NAME, TestTimeReporterExtension.class);
    }

    void registerTask(Project project, TimeReporterTestListener listener, TestTimeReporterExtension extension) {
        project.getTasks().register(PRINT_TEST_TIME_STATS_TASK_NAME, PrintTestTimeStatsTask.class, task -> {
            task.getTestListener().set(listener);
            task.getLongestTestsCount().set(extension.getLongestTestsCount());
            task.getBinSize().set(extension.getBinSize());
            task.getSlowThreshold().set(extension.getSlowThreshold());
            // task.dependsOn("test");
        });
    }

    void setExtensionDefaultValues(TestTimeReporterExtension extension) {
        extension.getLongestTestsCount().convention(100);
        extension.getBinSize().convention(100);
        extension.getSlowThreshold().convention(500);
    }
}
