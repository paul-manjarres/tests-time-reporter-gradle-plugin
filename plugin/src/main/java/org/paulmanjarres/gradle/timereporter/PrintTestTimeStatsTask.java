package org.paulmanjarres.gradle.timereporter;

import lombok.AllArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class PrintTestTimeStatsTask implements Action<Task> {

  private TimeReporterTestListener listener;

  @Override
  public void execute(@NotNull Task task) {
    System.out.println("FINALIZED JEAN PLUGIN EXECUTION");
    System.out.println(" Total executed tests: " + listener.getStats().size());
    listener.getStats().forEach(t -> {
      System.out.println(t.getDuration().toMillis() + "ms - " +t.getTestName() + " : " + t.getResult());
    });
    System.out.println("================");

  }
}
