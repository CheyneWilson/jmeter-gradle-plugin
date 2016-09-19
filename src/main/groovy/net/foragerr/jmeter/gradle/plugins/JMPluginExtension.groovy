package net.foragerr.jmeter.gradle.plugins

class JMPluginExtension {

    List<File> testFiles = null                      //maps to -t, --testfile
    File testFileDir = null
    List<String> includes = null
    List<String> excludes = null

    File jmeterLogFile = null                       //maps to -j, --jmeterlogfile

    File propFile = null                            //maps to -p, --propfile
    List<File> addPropFiles = null                  //maps to -q, --addprop
    Map<String, ?> jmeterProperties = null          //maps to -J, --jmeterproperty

    List<File> systemPropertiesFiles = null         //maps to -S, --systemPropertyFile
    Map<String, ?> systemProperties = null          //maps to -D, --systemproperty

    Map<String, ?> globalProperties = null

    File customReportXslt

    Boolean ignoreErrors = null
    Boolean ignoreFailures = null
    Boolean remote = false
    Boolean enableReports = null
    Boolean enableExtendedReports = null

    List<String> jmPluginJars = null

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
