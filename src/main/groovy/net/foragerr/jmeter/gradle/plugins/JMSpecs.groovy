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

    String maxHeapSize
    String minHeapSize

    File propFile = null                        //maps to -p, --propfile
    List<File> addPropFiles = null              //maps to -q, --addprop
    Map<String, ?> jmeterProperties = null      //maps to -J, --jmeterproperty

    List<File> systemPropertiesFiles = null                    // maps to -S, --systemPropertyFile
    Map<String, ?> systemProperties = new HashMap<>()          // maps to -D, --systemproperty

    Map<String, ?> globalProperties = null          //maps to -G, --globalproperty
    File globalPropertiesFile                       //pass the properties in the files to remote injectors via -G

    Boolean remote = false                          //maps to -r, --runremote, Start remote servers (as defined in remote_hosts)
    List<String> remoteHosts                        //convenience field, maps to -Jremote_hosts=S1,S2,S3...
    List<String> remoteStart                        //maps to -R, --remotestart, Start these remote servers (overrides remote_hosts)
    Boolean remoteExit = false                      //maps to -X, --remoteexit, Exit the remote servers at end of test (non-GUI)
    Boolean nongui                                  //maps to -n, --nongui,    run JMeter in nongui mode

    Boolean ignoreErrors = null
    Boolean ignoreFailures = null

    Boolean enableReports = null
    Boolean enableExtendedReports = null

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

        if(remoteStart != null){
            args.add("-R${remoteStart.join(",")}");
        } else if (remoteHosts != null){
            args.add("-Jremote_hosts=${remoteHosts.join(",")}");
        }

        if(remote){
            args.add("-r")
        }

        if(remoteExit){
            args.add("-X")
        }

        if (addPropFiles) {
            for (File addPropFile : addPropFiles) {
                if (addPropFile.exists() && addPropFile.isFile()) {
                    args.addAll(Arrays.asList("-q", addPropFile.getCanonicalPath()));
                } else {
                    LOG.warn("Addtional Property File ${addPropFile} was not valid.")
                }
            }
        }

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

        if (globalPropertiesFile != null) {
            globalPropertiesFile.withInputStream {
                properties.load(it)
            }

            globalPropertiesFile.each {
                println("-G{$it.key}=${it.value}")
                args.add("-G{$it.key}=${it.value}")
            }
        }

        if (globalProperties != null) {
            globalProperties.each {k,v ->
                k.replaceAll(" ", "\\ ")
                args.add("-G$k=$v")
            }
        }

        if (jmeterProperties != null) {
            jmeterProperties.each {k,v ->
                k.replaceAll(" ", "\\ ")
                args.add("-J$k=${v}")
            }
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

        if (systemProperties != null) {
            systemProperties.each { k,v ->
                k.replaceAll(" ", "\\ ")
                args.add("-D$k=${v}")
            }
        }

        return args
    }
}


