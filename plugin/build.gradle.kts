import com.diffplug.spotless.LineEnding

plugins {
    `idea`
    `java-gradle-plugin`
    `maven-publish`
    jacoco
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.diffplug.spotless") version "6.25.0"
    id("io.github.paul-manjarres.test-time-reporter") version "0.5.0"
}

version = "0.7-SNAPSHOT"
group = "io.github.paul-manjarres"
description = "A gradle plugin to display test execution statistics"

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

repositories {
    mavenCentral()
}

gradlePlugin {
    website.set("https://github.com/paul-manjarres/tests-time-reporter-gradle-plugin")
    vcsUrl.set("https://github.com/paul-manjarres/tests-time-reporter-gradle-plugin.git")

    val testTimeReporter by plugins.creating {
        id = "io.github.paul-manjarres.test-time-reporter"
        implementationClass = "org.paulmanjarres.gradle.timereporter.TestTimeReporterPlugin"
        displayName = "Test Time Statistics Reporter"
        description = project.description
        tags.set(listOf("tests", "time", "stats", "reporter"))
    }
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertJ)
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
    dependsOn(functionalTest)
    //dependsOn(tasks.jacocoTestReport)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    failFast = false
    finalizedBy(tasks.jacocoTestReport)
}

spotless {
    java {
        encoding("UTF-8")
        importOrder()
        removeUnusedImports()
        palantirJavaFormat()
        formatAnnotations()
        trimTrailingWhitespace()
    }
    lineEndings = LineEnding.GIT_ATTRIBUTES
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

afterEvaluate {
    tasks.spotlessCheck {
        dependsOn(tasks.spotlessApply)
    }
}

idea {
    module {
        testSourceDirs.addAll(functionalTestSourceSet.java.srcDirs)
        testResourceDirs.addAll(functionalTestSourceSet.resources.srcDirs)
    }
}

testTimeReporter {
    experimentalFeatures = true
}