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
                log.lifecycle("{}", console.magenta(run.getName()));
                log.lifecycle(
                        "{} - Total: [{}] - BinSize: [{}ms] - SlowThreshold: [{}ms] - Total Time: [{}ms]",
                        console.magenta(executor.getName()),
                        String.format("%,d", allTestCases.size()),
                        binSize,
                        slowThreshold,
                        String.format("%,6d", executor.getDuration().toMillis()));

                final Histogram.HistogramConfig conf = new Histogram.HistogramConfig();
                conf.setBucketSize(binSize);
                conf.setMaxValue(slowThreshold);

                final Histogram h = Histogram.from(allTestCases, conf);
                log.lifecycle("================================================");
                log.lifecycle("       Bin          Tests       %          Time      %Time");

                for (int i = 0; i < h.getBuckets(); i++) {
                    double durationPercentage = h.getDuration()[i]
                            * 100
                            / (double) executor.getDuration().toMillis();
                    log.lifecycle(
                            " [{} - {}ms] : {} - [{}]  - {}ms - {}%",
                            String.format("%4d", i * binSize),
                            String.format("%4d", (i + 1) * binSize),
                            String.format("%,6d", h.getValues()[i]),
                            String.format("%6.2f%%", h.getPercentages()[i]),
                            String.format("%,6d", h.getDuration()[i]),
                            String.format("%5.2f", durationPercentage));
                }

                final double stDurationPercentage = h.getSlowTestDuration()
                        * 100
                        / (double) executor.getDuration().toMillis();
                log.lifecycle(
                        " [{}] : {} - [{}]  - {}ms - {}%",
                        console.red(String.format("     > %4dms", h.getMaxValue())),
                        console.red(String.format("%,6d", h.getSlowTestCount())),
                        console.red(String.format("%6.2f%%", h.getSlowTestPercentage())),
                        String.format("%,6d", h.getSlowTestDuration()),
                        String.format("%5.2f", stDurationPercentage));

                logNewLine();
                if (h.getSlowTestCount() == 0) {
                    log.lifecycle(
                            "There are no tests considered slow. All of them are below the slow threshold of {}ms",
                            slowThreshold);
                } else {
                    log.lifecycle(
                            "{} of the tests ({}/{}) were considered slow. Approximately {} of total time.",
                            console.yellow(String.format("%3.2f%%", h.getSlowTestPercentage())),
                            h.getSlowTestCount(),
                            String.format("%,d", h.getCount()),
                            console.yellow(String.format(
                                    "%3.2f%%",
                                    h.getSlowTestDuration()
                                            * 100
                                            / (double) executor.getDuration().toMillis())));
                }
                logNewLine();
            }
        }
    }

    public void logNewLine() {
        log.lifecycle(" ");
    }
}
