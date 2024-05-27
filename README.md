# Tests Time Stats Reporter Gradle Plugin

![Build](https://github.com/paul-manjarres/tests-time-reporter-gradle-plugin/actions/workflows/gradle.yml/badge.svg)

## Description
This is a simple Gradle plugin that reports tests execution time statistics of your project.

* Works with gradle 6.9+ and Java8+. 
* Total test count.
* Distribution of tests by result (success, failure, skipped)
* Distribution of tests by class (how many tests are in each class)
* Histogram with test count distribution per bins.
* Top N of slowest tests (configurable).

The plugin is inspired in the Karma Time Stats Reporter plugin (https://www.npmjs.com/package/karma-time-stats-reporter)

See the Gradle Plugins page: https://plugins.gradle.org/plugin/io.github.paul-manjarres.test-time-reporter

## Use in your Project

In your build.gradle:

```kotlin
plugins {
    id("io.github.paul-manjarres.test-time-reporter") version "0.4.0"
}

// These are the default values, can be omitted if they work for you
testTimeReporter{
    longestTestsCount = 5         // The max amount of results to show in the slowest test section. 
    maxResultsForGroupByClass = 5 // The max amount of results to show in the group-by-class test section.
    binSizeInMillis = 100         // Size of each bin in the histogram
    slowThresholdInMillis = 200   // The threshold to consider a test as 'slow'
    showGroupByClass = true       // Enables/disables test grouped by class section
    showGroupByResult = true      // Enables/disables test grouped by result section
    showSlowestTests = true       // Enables/disables slowest test section
    showHistogram = true          // Enables/disables histogram section
    coloredOutput = true          // If true, output will use colors.
}
```
