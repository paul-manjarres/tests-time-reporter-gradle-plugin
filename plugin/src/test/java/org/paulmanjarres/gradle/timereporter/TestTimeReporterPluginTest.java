package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestTimeReporterPluginTest {

  @Test
  void pluginRegistersATask() {
    // Create a test project and apply the plugin
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply("io.github.paul-manjarres.test-time-reporter");

    // Verify the result
    assertNotNull(project.getTasks().findByName("greeting"));
  }

}