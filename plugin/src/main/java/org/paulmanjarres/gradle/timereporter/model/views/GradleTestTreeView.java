package org.paulmanjarres.gradle.timereporter.model.views;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.logging.Logger;
import org.paulmanjarres.gradle.timereporter.model.GradleTest;
import org.paulmanjarres.gradle.timereporter.model.GradleTestRun;
import org.paulmanjarres.gradle.timereporter.model.GradleTestSuite;
import org.paulmanjarres.gradle.timereporter.utils.ConsoleUtils;

public class GradleTestTreeView {

    public void printTreeView(Set<GradleTest> set, ConsoleUtils console, Logger log) {
        log.lifecycle(console.yellow("Test suites tree view"));
        log.lifecycle(" ");

        final List<GradleTestRun> testRun = set.stream()
                .filter(s -> s.getParent() != null && s.getParent().getName().equals("root"))
                .map(GradleTestRun.class::cast)
                .collect(Collectors.toList());

        for (GradleTest run : testRun) {
            log.lifecycle(
                    "+ {} - Tests: [{}] - Duration: {}ms - {}",
                    console.magenta(run.getName()),
                    run.countTests(),
                    run.getDuration().toMillis(),
                    run.getResult());

            for (GradleTest executor : run.getChildren()) {
                long executorTestCount = executor.countTests();
                log.lifecycle(
                        "|--- {} - {} - Duration: {}ms - Tests: [{}]",
                        console.blue(executor.getName()),
                        executor.getResult(),
                        executor.getDuration().toMillis(),
                        executorTestCount);

                for (GradleTest suite : executor.getChildren()) {
                    GradleTestSuite ts = (GradleTestSuite) suite;
                    log.lifecycle(
                            "|    |--- Suite: {} - {} - Duration: {}ms - Tests: [{}] - InitTime: {}ms",
                            console.cyan(ts.getName()),
                            ts.getResult(),
                            ts.getDuration().toMillis(),
                            //                            String.format("%5.2f", ts.getNumberOfTests() * 100 / (double)
                            // executorTestCount),
                            ts.getNumberOfTests(),
                            ts.getInitTimeMillis(),
                            executorTestCount);
                }
            }
            log.lifecycle("| ");
        }
    }
}
