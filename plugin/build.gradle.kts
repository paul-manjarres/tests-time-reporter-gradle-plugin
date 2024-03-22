//import com.diffplug.spotless.LineEnding
//import com.github.spotbugs.snom.SpotBugsTask

plugins {
    `java-gradle-plugin`
    `maven-publish`
    jacoco
   // id("com.diffplug.spotless") version "6.25.0"
   // id("com.github.spotbugs") version "6.0.9"
}

version = "0.0.1-SNAPSHOT"
description = ""

java{
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    val greeting by plugins.creating {
        id = "io.github.paul-manjarres.test-time-reporter"
        implementationClass = "org.paulmanjarres.gradle.timereporter.TestTimeReporterPlugin"
        displayName = ""
        description = ""
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

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


//spotless {
//    java{
//        encoding("UTF-8")
//        importOrder()
//        removeUnusedImports()
//        googleJavaFormat()
//    }
//    lineEndings = LineEnding.GIT_ATTRIBUTES
//}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

//tasks.spotbugsMain{
//    ignoreFailures = true
//    showProgress = false
//    showStackTraces = false
//}
//
//tasks.spotbugsTest{
//    ignoreFailures = true
//    showProgress = false
//    showStackTraces = false
//}
//
//tasks.named<SpotBugsTask>("spotbugsFunctionalTest") {
//    ignoreFailures = true
//    showProgress = false
//    showStackTraces = false
//}
