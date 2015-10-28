package net.foragerr.jmeter.gradle.plugins

/**
 * Created by @author foragerr@gmail.com on 7/17/2015.
 */
class JMPluginExtension {

    File jmLog = null
    File testFileDir = null
    File jmPropertyFile = null //maps to -p, --propfile
    File customReportXslt

    Boolean ignoreErrors = null
    Boolean ignoreFailures = null
    Boolean remote = false
    Boolean enableReports = null
    Boolean enableExtendedReports = null

	List<File> jmTestFiles = null
    List<File> jmUserPropertiesFiles = null //maps to -S, --systemPropertyFile
    List<String> jmPluginJars = null
    List<String> jmUserProperties = null //maps to -J, --jmeterproperty
    List<String> includes = null
    List<String> excludes = null 

    String jmVersion
    String jmPluginVersion
    String resultFilenameTimestamp
    String reportPostfix
    String reportXslt = null
    String maxHeapSize
	String reportTitle = null
	
	//For internal use, Not user settable:
	File workDir = null 
	File reportDir = null
	List<File> jmResultFiles = null

}
