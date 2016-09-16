package net.foragerr.jmeter.gradle.plugins

class JMPluginExtension {

    File jmLog = null          // maps to -j, --jmeterlogfile
    File testFileDir = null
    File jmPropertyFile = null //maps to -p, --propfile
    File jmAddProp = null      //maps to -q, --addprop
    File customReportXslt

    Boolean ignoreErrors = null
    Boolean ignoreFailures = null
    Boolean remote = false
    Boolean enableReports = null
    Boolean enableExtendedReports = null

    List<File> jmTestFiles = null             //maps to -t, --testfile
    List<File> jmSystemPropertiesFiles = null //maps to -S, --systemPropertyFile
    //TODO: Change to a map
    List<String> jmSystemProperties = null    //maps to -D, --systemproperty

    List<String> jmPluginJars = null
    //TODO: Change to a map
    List<String> jmUserProperties = null      //maps to -J, --jmeterproperty

    Map<String, ?> globalProperties = null

    List<String> includes = null
    List<String> excludes = null

    String resultFilenameTimestamp
    String reportPostfix
    String reportXslt = null
    String maxHeapSize
    String minHeapSize
    String reportTitle = null

    //For internal use, Not user settable:
    String jmVersion
    String jmPluginVersion

    File workDir = null
    File reportDir = null
    List<File> jmResultFiles = null

}
