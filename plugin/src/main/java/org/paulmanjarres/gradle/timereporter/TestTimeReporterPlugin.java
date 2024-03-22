package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;

public class TestTimeReporterPlugin implements Plugin<Project> {

  private static final String PRINT_TEST_TIME_STATS_TASK_NAME = "printTestTimeStats";

  public void apply(Project project) {
    // Register a task
    project.getTasks().register("greeting", task -> {
      task.doLast(s -> System.out.println("Hello from plugin 'org.example.greeting'"));
    });

    final TimeReporterTestListener listener = new TimeReporterTestListener();
    registerPrinter(project, new PrintTestTimeStatsTask(listener));
//    project.getTasks().register(PRINT_TEST_TIME_STATS_TASK_NAME, task ->
//        task.doLast(new PrintTestTimeStatsTask(listener))
//    );

    final TaskCollection<Test> testTasks = project.getTasks().withType(Test.class);
    testTasks.forEach(it -> {
      it.addTestListener(listener);
      it.finalizedBy().finalizedBy(PRINT_TEST_TIME_STATS_TASK_NAME);
    });
  }

  public void registerPrinter(Project project, PrintTestTimeStatsTask printer){
    project.getTasks().register(PRINT_TEST_TIME_STATS_TASK_NAME, task ->
        task.doLast(printer)
    );
  }
}
