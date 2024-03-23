package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;
import org.paulmanjarres.gradle.timereporter.model.PluginConstants;

public class TestTimeReporterPlugin implements Plugin<Project> {

    public void apply(Project project) {
        final TestTimeReporterExtension extension =
                project.getExtensions().create(PluginConstants.EXTENSION_NAME, TestTimeReporterExtension.class);

        final TimeReporterTestListener listener = new TimeReporterTestListener();

        project.getTasks()
                .register(PluginConstants.PRINT_TEST_TIME_STATS_TASK_NAME, PrintTestTimeStatsTask.class, task -> {
                    task.getTestListener().set(listener);
                    task.getLongestTestsCount().set(extension.getLongestTestsCount());
                });

        final TaskCollection<Test> testTasks = project.getTasks().withType(Test.class);
        testTasks.forEach(it -> {
            it.addTestListener(listener);
            it.finalizedBy().finalizedBy(PluginConstants.PRINT_TEST_TIME_STATS_TASK_NAME);
        });
    }
}
