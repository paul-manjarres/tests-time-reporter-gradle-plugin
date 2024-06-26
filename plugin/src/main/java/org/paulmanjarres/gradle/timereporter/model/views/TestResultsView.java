package org.paulmanjarres.gradle.timereporter.model.views;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.testing.TestResult;
import org.paulmanjarres.gradle.timereporter.model.GradleTestCase;
import org.paulmanjarres.gradle.timereporter.model.GradleTestRun;
import org.paulmanjarres.gradle.timereporter.utils.ConsoleUtils;

@AllArgsConstructor
public class TestResultsView {
    private ConsoleUtils console;
    private Logger log;
    private boolean showFailed;
    private boolean showSkipped;

    public void printView(List<GradleTestRun> runs) {
        log.lifecycle(console.yellow("Test results view"));

        for (GradleTestRun run : runs) {
            final Set<GradleTestCase> allTestCases = run.getTestCases();
            log.lifecycle(console.magenta("{}"), run.getSimplifiedName());

            final Map<TestResult.ResultType, List<GradleTestCase>> groupsByResult =
                    allTestCases.stream().collect(Collectors.groupingBy(GradleTestCase::getResult));

            for (Map.Entry<TestResult.ResultType, List<GradleTestCase>> entry : groupsByResult.entrySet()) {
                logResult(entry.getKey(), entry.getValue().size(), allTestCases.size());

                if (showFailed && entry.getKey().equals(TestResult.ResultType.FAILURE)) {
                    ConsoleUtils.Color color = console.getColorBy(TestResult.ResultType.FAILURE);
                    for (GradleTestCase failTc : entry.getValue()) {
                        log.lifecycle(
                                "   |--- {}.{}",
                                console.print(failTc.getClassName(), color),
                                console.print(failTc.getName(), color));
                    }
                }

                if (showSkipped && entry.getKey().equals(TestResult.ResultType.SKIPPED)) {
                    ConsoleUtils.Color color = console.getColorBy(TestResult.ResultType.SKIPPED);
                    for (GradleTestCase failTc : entry.getValue()) {
                        log.lifecycle(
                                "   |--- {}.{}",
                                console.print(failTc.getClassName(), color),
                                console.print(failTc.getName(), color));
                    }
                }
            }
        }
    }

    private void logResult(TestResult.ResultType result, int tests, int total) {
        ConsoleUtils.Color color = result == TestResult.ResultType.SUCCESS && tests < total
                ? ConsoleUtils.Color.YELLOW
                : ConsoleUtils.Color.CLEAR;
        log.lifecycle(
                "- {} : {} ({} / {})",
                console.print("" + result, console.getColorBy(result)),
                console.print(String.format("%6.2f%%", tests * 100 / (double) total), color),
                String.format("%,d", tests),
                String.format("%,d", total));
    }
}
