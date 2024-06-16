package org.paulmanjarres.gradle.timereporter.model.views;

import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.gradle.api.logging.Logger;
import org.paulmanjarres.gradle.timereporter.model.GradleTest;
import org.paulmanjarres.gradle.timereporter.model.GradleTestCase;
import org.paulmanjarres.gradle.timereporter.model.GradleTestRun;
import org.paulmanjarres.gradle.timereporter.model.Histogram;
import org.paulmanjarres.gradle.timereporter.utils.ConsoleUtils;

@AllArgsConstructor
public class HistogramView {
    private ConsoleUtils console;
    private Logger log;
    private int slowThreshold;
    private int binSize;

    public void printView(List<GradleTestRun> runs) {
        log.lifecycle(console.yellow("Histogram view"));

        for (GradleTestRun run : runs) {

            for (GradleTest executor : run.getChildren()) {

                final Set<GradleTestCase> allTestCases = executor.getTestCases();
                log.lifecycle(
                        "{} - Total: [{}] - BinSize: [{}ms] - Slow Threshold: [{}ms] - TotalSuite Time: [{}ms]",
                        console.magenta(executor.getName()),
                        allTestCases.size(),
                        binSize,
                        slowThreshold,
                        String.format("%,6d", executor.getDuration().toMillis()));

                final Histogram.HistogramConfig conf = new Histogram.HistogramConfig();
                conf.setBucketSize(binSize);
                conf.setMaxValue(slowThreshold);

                final Histogram h = Histogram.from(allTestCases, conf);
                log.lifecycle("================================================");

                for (int i = 0; i < h.getBuckets(); i++) {
                    log.lifecycle(
                            " [{} - {}ms] : ({}/{}) - [{}]  - {}ms - {}%",
                            String.format("%4d", i * binSize),
                            String.format("%4d", (i + 1) * binSize),
                            String.format("%4d", h.getValues()[i]),
                            String.format("%4d", h.getCount()),
                            String.format("%5.2f%%", h.getPercentages()[i]),
                            String.format("%,4d", h.getDuration()[i]),
                            String.format(
                                    "%5.2f",
                                    h.getDuration()[i]
                                            * 100
                                            / (double) executor.getDuration().toMillis()));
                }

                log.lifecycle(
                        " [{}] : ({}/{}) - [{}]  - {}ms - {}%",
                        console.red(String.format("     > %4dms", h.getMaxValue())),
                        console.red(String.format("%4d", h.getSlowTestCount())),
                        console.red(String.format("%4d", h.getCount())),
                        console.red(String.format("%5.2f%%", h.getSlowTestPercentage())),
                        String.format("%,4d", h.getSlowTestDuration()),
                        String.format(
                                "%5.2f",
                                h.getSlowTestDuration()
                                        * 100
                                        / (double) executor.getDuration().toMillis()));

                logNewLine();
                log.lifecycle(
                        "{} of the tests ({}) were considered slow. Approximately {} of total time.",
                        console.yellow(String.format("%3.2f%%", h.getSlowTestPercentage())),
                        h.getSlowTestCount(),
                        console.yellow(String.format(
                                "%3.2f%%",
                                h.getSlowTestDuration()
                                        * 100
                                        / (double) executor.getDuration().toMillis())));
                logNewLine();
            }
        }
    }

    public void logNewLine() {
        log.lifecycle(" ");
    }
}
