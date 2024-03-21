package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TestTimeReporterPlugin implements Plugin<Project> {
  public void apply(Project project) {
    // Register a task
    project.getTasks().register("greeting", task -> {
      task.doLast(s -> System.out.println("Hello from plugin 'org.example.greeting'"));
    });
  }
}
