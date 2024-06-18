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

    public void printView(List<GradleTestRun> runs) {
        log.lifecycle(console.yellow("Slowest tests"));

        for (GradleTest run : runs) {
            final Set<GradleTestCase> allTestCases = run.getTestCases();
            final List<GradleTestCase> slowTestList = getSlowestTestCases(allTestCases, slowThreshold);

            log.lifecycle(
                    "{} SlowTests: [{} out of {}] - Threshold: [{}ms] - Max Results: [{}]",
                    console.magenta(run.getName()),
                    String.format("%,d", slowTestList.size()),
                    String.format("%,d", allTestCases.size()),
                    slowThreshold,
                    maxResults);

            slowTestList.stream().limit(maxResults).forEach(r -> log.lifecycle(formatSlowestTest(r)));
            log.lifecycle(" ");
        }
    }

    protected List<GradleTestCase> getSlowestTestCases(Set<GradleTestCase> set, long threshold) {
        return set.stream()
                .sorted(Comparator.comparing(GradleTestCase::getDuration).reversed())
                .filter(r -> r.getDuration().toMillis() >= threshold)
                .collect(Collectors.toList());
    }

    protected String formatSlowestTest(GradleTestCase r) {
        return String.format(
                "[%,6dms] - %s - %s.%s ",
                r.getDuration().toMillis(),
                console.print(r.getResult().toString(), console.getColorBy(r.getResult())),
                console.cyan(r.getClassName()),
                console.cyan(r.getName()));
    }
}
