package org.paulmanjarres.gradle.timereporter.model.views;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.gradle.api.logging.Logger;
import org.paulmanjarres.gradle.timereporter.model.GradleTest;
import org.paulmanjarres.gradle.timereporter.model.GradleTestCase;
import org.paulmanjarres.gradle.timereporter.model.GradleTestRun;
import org.paulmanjarres.gradle.timereporter.utils.ConsoleUtils;

@AllArgsConstructor
public class SlowestTestsView {

    private ConsoleUtils console;
    private Logger log;
    private long slowThreshold;
    private int maxResults;

    public void printView(Set<GradleTest> set) {
        log.lifecycle(console.yellow("Slowest tests"));
        log.lifecycle(" ");

        final List<GradleTestRun> testRun = set.stream()
                .filter(s -> s.getParent() != null && s.getParent().getName().equals("root"))
                .map(GradleTestRun.class::cast)
                .collect(Collectors.toList());

        for (GradleTest run : testRun) {

            log.lifecycle(
                    "{} Slowest tests ({}) - Threshold: [{}ms] - Max Results: [{}]",
                    console.magenta(run.getName()),
                    "XXXX",
                    slowThreshold,
                    maxResults);

            run.getTestCases().stream()
                    .sorted(Comparator.comparing(GradleTestCase::getDuration).reversed())
                    .filter(r -> r.getDuration().toMillis() >= slowThreshold)
                    .limit(maxResults)
                    .forEach(r -> log.lifecycle(formatSlowestTest(r)));

            log.lifecycle(" ");
        }
    }

    public String formatSlowestTest(GradleTestCase r) {

        return String.format(
                "[%4d ms] - %s - %s.%s ",
                r.getDuration().toMillis(),
                //                console.print(r.getResult().toString(), getConsoleTextColorBy(r.getResult())),
                console.yellow(r.getResult().toString()),
                r.getClassName(),
                r.getName());
    }
}
