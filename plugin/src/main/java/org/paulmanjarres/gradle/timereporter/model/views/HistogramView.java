package org.paulmanjarres.gradle.timereporter.model.views;

import lombok.AllArgsConstructor;
import org.gradle.api.logging.Logger;
import org.paulmanjarres.gradle.timereporter.model.GradleTest;
import org.paulmanjarres.gradle.timereporter.utils.ConsoleUtils;

import java.util.Set;

@AllArgsConstructor
public class HistogramView {
    private ConsoleUtils console;
    private Logger log;

    public void printView(Set<GradleTest> set) {
        log.lifecycle(console.yellow("Histogram view"));
        log.lifecycle(" ");

        }

}
