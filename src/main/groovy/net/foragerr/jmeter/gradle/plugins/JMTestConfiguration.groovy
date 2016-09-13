package net.foragerr.jmeter.gradle.plugins


/*
  This POJO contains the context of a JMeter test run, such as test script, properties, properties files, whether to run remotely.
*/
class JMTestConfiguration {
    File jmLog = null
    File jmPropertyFile = null //maps to -p, --propfile
    File jmAddProp = null      //maps to -q, --addprop
    File customReportXslt

    Boolean ignoreErrors = null
    Boolean ignoreFailures = null
    Boolean remote = false
    Boolean enableReports = null
    Boolean enableExtendedReports = null

    File jmTestFile = null             //maps to -t, --testfile
    List<File> jmSystemPropertiesFiles = null //maps to -S, --systemPropertyFile

    List<String> jmSystemProperties = null    //maps to -D, --systemproperty
    List<String> jmPluginJars = null
    List<String> userProperties = null      //maps to -J, --jmeterproperty
    List<String> includes = null
    List<String> excludes = null

    String resultFilenameTimestamp
    String reportPostfix
    String reportXslt = null
    String maxHeapSize
    String minHeapSize
    String reportTitle = null

    File workDir = null
    File reportDir = null
    File resultFile = null


}