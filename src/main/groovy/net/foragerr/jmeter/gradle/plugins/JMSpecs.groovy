package net.foragerr.jmeter.gradle.plugins

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * This class contains the configuration for an individual JMeter test run.
 *
 * It also contains methods to output the command line arguments used when invoking a JMeter test script.
 */
class JMSpecs implements Serializable{

    private final static Logger LOG = Logging.getLogger(getClass());

    File testFile = null                      //maps to -t, --testfile
    File jmeterLogFile = null

    boolean nongui = true

    String maxHeapSize
    String minHeapSize

    File propFile = null                        //maps to -p, --propfile
    List<File> addPropFiles = null              //maps to -q, --addprop
    Map<String, ?> jmeterProperties = null      //maps to -J, --jmeterproperty

    Boolean ignoreErrors = null
    Boolean ignoreFailures = null
    Boolean remote = false

    Boolean enableReports = null
    Boolean enableExtendedReports = null

    List<File> systemPropertiesFiles = null                    // maps to -S, --systemPropertyFile
    Map<String, ?> systemProperties = new HashMap<>()          // maps to -D, --systemproperty

    Map<String, ?> globalProperties                            // maps to -G, --globalproperty

    List<String> jmPluginJars = null

    String resultFilenameTimestamp
    String reportPostfix
    String reportXslt = null
    String reportTitle = null
    File customReportXslt

    File workDir = null
    File reportDir = null
    File resultFile = null

    /**
     * Returns the JMeter command line arguments used when invoking jmeter to run the test configured in this JMSpec
     *
     * @return the JMeter command line arguments used when invoking jmeter to run the test configured in this JMSpec
     */
    List<String> getJmeterCommandLineArguments() {
        List<String> args = new ArrayList<String>();

        if(nongui){
            args.add("-n")
        }

        if(testFile != null){
            args.addAll(Arrays.asList("-t", testFile.getCanonicalPath()))
        }

        if(resultFile != null){
            args.addAll(Arrays.asList("-l", resultFile.getCanonicalPath()))
        }

        args.addAll(Arrays.asList(
                "-p", propFile.getCanonicalPath()
        ));

        if (addPropFiles) {
            boolean hasPrefix = false
            for (File addPropFile : addPropFiles) {
                if (addPropFile.exists() && addPropFile.isFile()) {
                    if(!hasPrefix){
                        args.add("-q");
                        hasPrefix = true
                    }
                    args.add(addPropFile.getCanonicalPath());
                } else {
                    LOG.warn("Addtional Property File ${addPropFile} was not valid.")
                }
            }
        }

        if (jmeterProperties != null) {
            jmeterProperties.each {k,v ->
                args.add("-J$k=$v")
            }
        }

        if (remote) {
            args.add("-r");
        }

        return args
    }

    /**
     * Returns the java command line arguments used when invoking jmeter to run the test configured in this JMSpec
     *
     * @return the java command line arguments used when invoking jmeter to run the test configured in this JMSpec
     */
    List<String> getJavaCommandLineArguments () {
        List<String> args = new ArrayList<String>();

        args.add("-Xms${minHeapSize}")
        args.add("-Xmx${maxHeapSize}")

        if (systemPropertiesFiles != null) {
            for (File systemPropertyFile : systemPropertiesFiles) {
                if (systemPropertyFile.exists() && systemPropertyFile.isFile()) {
                    args.addAll(Arrays.asList("-S", systemPropertyFile.getCanonicalPath()));
                }
                else {
                    LOG.warn("System property file ${systemPropertyFile} was not valid.")
                }
            }
        }

        if (systemProperties != null) {
            //TODO: may not work if $v has a space in it .. change how args are added.
            systemProperties.each { k,v ->
                args.add("-D$k=$v")
            }
        }
        return args
    }
}


