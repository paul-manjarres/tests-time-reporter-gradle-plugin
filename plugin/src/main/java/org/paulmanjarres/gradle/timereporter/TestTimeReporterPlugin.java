package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.testing.Test;

public class TestTimeReporterPlugin implements Plugin<Project> {
  public void apply(Project project) {
    // Register a task
    project.getTasks().register("greeting", task -> {
      task.doLast(s -> System.out.println("Hello from plugin 'org.example.greeting'"));
    });

    TimeReporterTestListener listener = new TimeReporterTestListener();
    project.getTasks().register("printTestTimeStats", task -> {
      task.doLast(s ->{
        System.out.println("FINALIZED JEAN PLUGIN EXECUTION");
        System.out.println(" Total executed tests: "+listener.getStats().size());
        listener.getStats().forEach(t ->{
          System.out.println(t.getTestName()+ " : "+t.getDuration().toMillis()+"ms - "+t.getResult());
        });
      });
    });


    TaskCollection<Test> testTasks = project.getTasks().withType(Test.class);
    testTasks.forEach(it -> {
      it.addTestListener(listener);
      it.finalizedBy("printTestTimeStats");
    });


  }
}
