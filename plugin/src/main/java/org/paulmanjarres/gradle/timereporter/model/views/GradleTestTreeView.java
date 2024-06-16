package org.paulmanjarres.gradle.timereporter.model.views;

import java.util.Comparator;
import java.util.List;
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
    private int suitesMaxResults;

    public void printTreeView(List<GradleTestRun> runs) {
        log.lifecycle(console.yellow("Test suites tree view"));

        for (GradleTest run : runs) {
            log.lifecycle(
                    "+ {} - {} Tests: [{}] - Duration: [{}ms]",
                    console.magenta(run.getName()),
                    console.print(run.getResult().toString(), console.getColorBy(run.getResult())),
                    run.countTests(),
                    String.format("%,6d", run.getDuration().toMillis()));

            for (GradleTest executor : run.getChildren()) {
                long executorTestCount = executor.countTests();
                long suitesCount = executor.getChildren().size();
                log.lifecycle(
                        "|--- {} - {} - Duration: [{}ms] - Tests: [{}] - Suites: [{}] - MaxResults: [{}]",
                        console.blue(executor.getName()),
                        console.print(executor.getResult().toString(), console.getColorBy(executor.getResult())),
                        String.format("%,6d", executor.getDuration().toMillis()),
                        executorTestCount,
                        suitesCount,
                        suitesMaxResults);

                int hiddenSuites = executor.getChildren().size() - suitesMaxResults;
                final List<GradleTestSuite> suites = executor.getChildren().stream()
                        .map(GradleTestSuite.class::cast)
                        .sorted(Comparator.comparing(GradleTestSuite::getDuration)
                                .reversed())
                        .limit(suitesMaxResults)
                        .collect(Collectors.toList());

                for (GradleTestSuite suite : suites) {
                    log.lifecycle(
                            "|    |--- Tests: [{}] {} [{}ms] - Init: [{}ms] - Suite: {}",
                            String.format("%3d", suite.getNumberOfTests()),
                            console.print(suite.getResult().toString(), console.getColorBy(suite.getResult())),
                            String.format("%,6d", suite.getDuration().toMillis()),
                            String.format("%3d", suite.getInitTimeMillis()),
                            console.cyan(suite.getName()));
                }
                if (hiddenSuites > 0) {
                    log.lifecycle("|    |--- ({}) suites hidden ...", hiddenSuites);
                }
            }
            log.lifecycle("| ");
        }
    }
}
