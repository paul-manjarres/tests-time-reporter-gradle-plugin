# Tests Time Stats Reporter Gradle Plugin

## Description
This is a simple Gradle plugin that reports the tests execution time statistics of your project. 

The plugin is inspired in the Karma Time Stats Reporter plugin (https://www.npmjs.com/package/karma-time-stats-reporter)

## Use In Project

In your build.gradle:

```kotlin
plugins {
    id("io.github.paul-manjarres.test-time-reporter") version "0.0.1"
}

testTimeReporter{
    longestTestsCount = 14
    slowThreshold = 0
}
```

