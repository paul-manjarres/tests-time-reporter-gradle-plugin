package org.paulmanjarres.gradle.timereporter;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

public abstract class PrintTestTimeStatsTask extends DefaultTask {

    @Input
    public abstract Property<TimeReporterTestListener> getTestListener();

    @Input
    @Optional
    public abstract Property<Integer> getLongestTestsCount();

    @TaskAction
    public void print() {
        this.getLogger().lifecycle("Tests time execution results");
        System.out.println("LongestTestCount: "
                + (getLongestTestsCount().isPresent() ? getLongestTestsCount().get() : "empty"));
        System.out.println(
                " Total executed tests: " + getTestListener().get().getStats().size());
        getTestListener().get().getStats().forEach(t -> {
            System.out.println(t.getDuration().toMillis() + "ms - " + t.getTestName() + " : " + t.getResult());
        });
        System.out.println("================");
    }
}
