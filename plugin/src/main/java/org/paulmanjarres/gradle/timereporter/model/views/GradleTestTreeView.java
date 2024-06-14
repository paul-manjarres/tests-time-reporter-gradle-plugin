package org.paulmanjarres.gradle.timereporter.model.views;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.gradle.api.logging.Logger;
import org.paulmanjarres.gradle.timereporter.model.GradleTest;
import org.paulmanjarres.gradle.timereporter.model.GradleTestRun;
import org.paulmanjarres.gradle.timereporter.model.GradleTestSuite;
import org.paulmanjarres.gradle.timereporter.utils.ConsoleUtils;

@AllArgsConstructor
public class GradleTestTreeView {

    private ConsoleUtils console;
    private Logger log;

    public void printTreeView(Set<GradleTest> set) {
        log.lifecycle(console.yellow("Test suites tree view"));

        final List<GradleTestRun> testRun = getGradleTestRun(set);

        for (GradleTest run : testRun) {
            log.lifecycle(
                    "+ {} - Tests: [{}] - Duration: [{}ms] - {}",
                    console.magenta(run.getName()),
                    run.countTests(),
                    String.format("%,6d", run.getDuration().toMillis()),
                    console.print(run.getResult().toString(), console.getColorBy(run.getResult())));

            for (GradleTest executor : run.getChildren()) {
                long executorTestCount = executor.countTests();
                log.lifecycle(
                        "|--- {} - {} - Duration: [{}ms] - Tests: [{}]",
                        console.blue(executor.getName()),
                        console.print(executor.getResult().toString(), console.getColorBy(executor.getResult())),
                        String.format("%,6d", executor.getDuration().toMillis()),
                        executorTestCount);

                for (GradleTest suite : executor.getChildren()) {
                    GradleTestSuite ts = (GradleTestSuite) suite;
                    log.lifecycle(
                            "|    |--- Tests: [{}] {} [{}ms] - Suite: {} - InitTime: [{}ms]",
                            ts.getNumberOfTests(),
                            console.print(ts.getResult().toString(), console.getColorBy(ts.getResult())),
                            String.format("%,6d", ts.getDuration().toMillis()),
                            console.cyan(ts.getName()),
                            String.format("%,d", ts.getInitTimeMillis()),
                            executorTestCount);
                }
            }
            log.lifecycle("| ");
        }
    }

    protected List<GradleTestRun> getGradleTestRun(Set<GradleTest> set) {
        return set.stream()
                .filter(s -> s.getParent() != null && s.getParent().getName().equals("root"))
                .map(GradleTestRun.class::cast)
                .collect(Collectors.toList());
    }
}