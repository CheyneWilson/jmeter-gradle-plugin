package net.foragerr.jmeter.gradle.plugins

import net.foragerr.jmeter.gradle.plugins.utils.JMUtils
import net.foragerr.jmeter.gradle.plugins.worker.JMeterRunner
import net.foragerr.jmeter.gradle.plugins.worker.JMeterRunnerType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
//import org.gradle.api.internal.AbstractTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

/**
 *  This class from which other JMeter tasks such as JMRun and JMGui can extend from providing common input parameters.
 */
class TaskJMExec extends DefaultTask {

    static final Logger LOG = Logging.getLogger(getClass());

    File testFile = null                        //maps to -t, --testfile

    @Optional
    @OutputFile
    File resultFile = null

    File propFile = null                        //maps to -p, --propfile
    List<File> addPropFiles = null              //maps to -q, --addprop
    Map<String, ?> jmeterProperties = null      //maps to -J, --jmeterproperty

    List<File> systemPropertiesFiles = null     //maps to -S, --systemPropertyFile
    Map<String, ?> systemProperties = null      //maps to -D, --systemproperty

    Map<String, ?> globalProperties = null      //maps to -G, --globalproperty
    File globalPropertiesFile    = null         //pass the properties in the file to remote injectors via -G

    boolean remote = false                      //maps to -r, --runremote, Start remote servers (as defined in remote_hosts)
    List<String> remoteHosts                    //convenience field, maps to -Jremote_hosts=S1,S2,S3...
    List<String> remoteStart                    //maps to -R, --remotestart, Start these remote servers (overrides remote_hosts)
    Boolean remoteExit = false                  //maps to -X, --remoteexit, Exit the remote servers at end of test (non-GUI)

    File workDir = null

    File jmeterLogFile = null

    String maxHeapSize
    String minHeapSize




    /**
     * Create a new JMSpecs file from the in
     *
     * @param testFile The testfile to configure JMSpecs with
     * @return
     */
    protected JMSpecs setupTestConfig(File testFile){
        JMSpecs testConfig = new JMSpecs();
        testConfig.testFile = testFile
        testConfig.resultFile = resultFile ?: JMUtils.getResultFile(testFile, project)

        testConfig.workDir = workDir ?: project.jmeter.workDir

        testConfig.jmeterLogFile = jmeterLogFile ?: project.jmeter.jmeterLogFile

        testConfig.propFile  = propFile ?: JMUtils.getJmeterPropsFile(project)

        testConfig.addPropFiles = addPropFiles ?: project.jmeter.addPropFiles

        testConfig.jmeterProperties = jmeterProperties ?: project.jmeter.jmeterProperties

        testConfig.systemPropertiesFiles = systemPropertiesFiles ?: project.jmeter.systemPropertiesFiles
        testConfig.systemProperties = systemProperties ?: project.jmeter.systemProperties ?: new HashMap<String, ?>()

        testConfig.globalProperties = globalProperties ?: project.jmeter.globalProperties ?: new HashMap<String, ?>()
        testConfig.globalPropertiesFile = globalPropertiesFile ?: project.jmeter.globalPropertiesFile

        testConfig.remoteHosts = remoteHosts ?: project.jmeter.remoteHosts
        testConfig.remoteStart = remoteStart ?: project.jmeter.remoteStart
        testConfig.remoteExit = remoteExit ?: project.jmeter.remoteExit

        // TODO: Decide when/where these get set
        testConfig.getSystemProperties().put("jmeter.home", testConfig.workDir.getAbsolutePath());
        testConfig.getSystemProperties().put("log_file", testConfig.jmeterLogFile);
        testConfig.getSystemProperties().put("jmeter.save.saveservice.output_format", "xml");

        testConfig.maxHeapSize = maxHeapSize ?: project.jmeter.maxHeapSize
        testConfig.minHeapSize = minHeapSize ?: project.jmeter.minHeapSize


        testConfig.remote = remote != null ? remote : project.jmeter.remote

        return testConfig;
    }

    /**
     * Start JMeter on the command line using the configuration provided
     *
     * @param testConfig The test context includes test script, java and jmeter properties to run with
     *
     * @return A results file from the JMeter execution
     */
    protected static File executeJMeterScript(JMSpecs testConfig, JMeterRunnerType runnerType) {
        try {
            LOG.info("Executing jMeter test : ${testConfig.testFile?.getCanonicalPath()}")
            testConfig.resultFile?.delete();

            new JMeterRunner().executeJmeterCommand(testConfig, runnerType);
            return testConfig.resultFile;

        } catch (IOException e) {
            throw new GradleException("Can't execute test", e);
        }
    }

}
