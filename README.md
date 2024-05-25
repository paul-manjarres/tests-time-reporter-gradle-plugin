# Tests Time Stats Reporter Gradle Plugin

## Description
This is a simple Gradle plugin that reports the tests execution time statistics of your project. 

The plugin is inspired in the Karma Time Stats Reporter plugin (https://www.npmjs.com/package/karma-time-stats-reporter)

![Build](https://github.com/paul-manjarres/tests-time-reporter-gradle-plugin/actions/workflows/gradle.yml/badge.svg)

## Use In Project

In your build.gradle:

```kotlin
plugins {
    id("io.github.paul-manjarres.test-time-reporter") version "0.3.0"
}

// These are the default values, can be omitted if they work for you
testTimeReporter{
    longestTestsCount = 5
    binSizeInMillis = 100
    slowThresholdInMillis = 200
    showGroupByClass = true
    showGroupByResult = true
    showSlowestTests = true
    showHistogram = true
    coloredOutput = true
}
```
