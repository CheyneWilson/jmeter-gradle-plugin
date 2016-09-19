##Gradle plugin to execute JMeter tests.

This branch was created from the foragerr one so I could add support for multiple tests in a single Gradle config.
The original plugin 'almost' met my requirements, and looks like the most promising starting point.
I wanted to migrate my existing test cases from Jenkins+Ant with config stored in multiple places to a coherent config that is easy to run remotely and locally.

## Short term goals
 * Support multiple test cases with different config properties in a single build script
 * Remote execution on multiple injectors
 * ctrl^c cancelling local build. Remote injectors stopping if parent stopped
 * More documentation
 * Some tests for the plugin

## Example usage (from dev)
```groovy

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath "net.foragerr.jmeter:jmeter-gradle-plugin:1.0.8-3.0-BETA"
    }
}

apply plugin: 'net.foragerr.jmeter'

/**
 * Run a basic dummy test
 */
task aTestTask(type: net.foragerr.jmeter.gradle.plugins.TaskJMRun, dependsOn: jmInit) {
    testFile = file("Test Plan 1.jmx")

    // Any other properties, properties files, etc that you want to invoke JMeter with
 }

/**
 * Run a basic dummy test with a result file
 */
task aTestTaskWithResultFile(type: net.foragerr.jmeter.gradle.plugins.TaskJMRun, dependsOn: jmInit) {
    testFile = file("Test Plan 1.jmx")
    resultFile = file("A result.jtl")
    // This will only run once at the inputs / outputs are specified.
    // If we wanted to have it execute every time we could uncomment below
    // outputs.upToDateWhen {return false}
}


/**
 * Consume the output of 'aTestTask' and generate a report
 */
task aTestAndReportTask(type: net.foragerr.jmeter.gradle.plugins.TaskJMReports, dependsOn:  aTestTask) {
    // Because the resultFile is dynamically generated, we don't know it at configuration time.
    doFirst {
        resultFile = aTestTask.resultFile
    }
}

/**
 * Consume the output of 'aTestTask' and generate a report
 */
task aReportTask(type: net.foragerr.jmeter.gradle.plugins.TaskJMReports) {
    dependsOn  aTestTaskWithResultFile
    // This works because the results file is known at configuration time
    resultFile = aTestTaskWithResultFile.resultFile
}
```



For usage see: http://jmeter.foragerr.net/  
or [wiki](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/wiki/Getting-Started)

## News
**8/21/2016**
* [#79](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/79) fixed

**6/26/2016**
* Version 1.0.6 released
* [#56](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/56) and [#77](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/77) fixed

**4/21/2016**
* Version 1.0.5 released
* added support for minHeapSize [#56](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/56)
* added additional jmeter-plugin and webdriver jars to classpath [#57](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/57)
* Fixed [#55](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/55) issue with jmSystemPropertiesFiles
* Reformatted a few code files

**4/2/2016**
* Version 1.0.4 released
* [#47](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/47) and [#49](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/49) Fixed. These are related issues that cause a test failure when using xpath extractor
* Gradle wrapper upgraded to 2.11
* [#41](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/41) Fixed
* [#42](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/issues/42) Fixed

[See older here..](https://github.com/jmeter-gradle-plugin/jmeter-gradle-plugin/wiki/Release-Notes)

##Attribution
This project started as a hard fork of [kulya/jmeter-gradle-plugin](https://github.com/kulya/jmeter-gradle-plugin). Besides defect fixes and feature enhancements, most of the original codebase has been re-written since. 

If you are a user of the older plugin see [here]() for easy migration to this version of the plugin. If you're a developer familiar with the older plugin, see [here]() for notes about major changes.
