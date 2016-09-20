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

    Map<String, ?> globalProperties = null          //maps to -G, --globalproperty
    File globalPropertiesFile                       //pass the properties in the files to remote injectors via -G
    Boolean remote = false                          //maps to -r, --runremote, Start remote servers (as defined in remote_hosts)
    List<String> remoteHosts                        //convenience field, maps to -Jremote_hosts=S1,S2,S3...
    List<String> remoteStart                        //maps to -R, --remotestart, Start these remote servers (overrides remote_hosts)
    Boolean remoteExit = false                      //maps to -X, --remoteexit, Exit the remote servers at end of test (non-GUI)

    File customReportXslt

    Boolean ignoreErrors = null
    Boolean ignoreFailures = null
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
