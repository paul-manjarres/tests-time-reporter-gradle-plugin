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

        final List<GradleTestRun> testRun = getGradleTestRun(set);

        for (GradleTest run : testRun) {
            final Set<GradleTestCase> allTestCases = run.getTestCases();
            final List<GradleTestCase> slowTestList = getSlowestTestCases(allTestCases, slowThreshold);

            log.lifecycle(
                    "{} SlowTests: [{} out of {}] - Threshold: [{}ms] - Max Results: [{}]",
                    console.magenta(run.getName()),
                    slowTestList.size(),
                    allTestCases.size(),
                    slowThreshold,
                    maxResults);

            slowTestList.stream().limit(maxResults).forEach(r -> log.lifecycle(formatSlowestTest(r)));
            log.lifecycle(" ");
        }
    }

    protected List<GradleTestRun> getGradleTestRun(Set<GradleTest> set) {
        return set.stream()
                .filter(s -> s.getParent() != null && s.getParent().getName().equals("root"))
                .map(GradleTestRun.class::cast)
                .collect(Collectors.toList());
    }

    protected List<GradleTestCase> getSlowestTestCases(Set<GradleTestCase> set, long threshold) {
        return set.stream()
                .sorted(Comparator.comparing(GradleTestCase::getDuration).reversed())
                .filter(r -> r.getDuration().toMillis() >= threshold)
                .collect(Collectors.toList());
    }

    protected String formatSlowestTest(GradleTestCase r) {
        return String.format(
                "[%,6d ms] - %s - %s.%s ",
                r.getDuration().toMillis(),
                console.print(r.getResult().toString(), console.getColorBy(r.getResult())),
                r.getClassName(),
                r.getName());
    }
}
