package org.paulmanjarres.gradle.timereporter;

import static org.junit.jupiter.api.Assertions.*;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.paulmanjarres.gradle.timereporter.model.PluginConstants;

class TestTimeReporterPluginTest {

    @Test
    void pluginRegistersATask() {

        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(PluginConstants.PLUGIN_ID);

        // Verify the result
        assertNotNull(project.getTasks().findByName(PluginConstants.PRINT_TEST_TIME_STATS_TASK_NAME));
    }
}
