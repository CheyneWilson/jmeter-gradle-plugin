package net.foragerr.jmeter.gradle.plugins

import net.foragerr.jmeter.gradle.plugins.utils.JMUtils
import net.foragerr.jmeter.gradle.plugins.worker.JMeterRunner
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

    Map<String, ?> globalProperties = null

    File workDir = null

    File jmeterLogFile = null

    String maxHeapSize
    String minHeapSize

    Boolean remote

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

        // TODO: Decide when/where these get set
        testConfig.getSystemProperties().put("search_paths", System.getProperty("search_paths"));
        testConfig.getSystemProperties().put("jmeter.home", testConfig.workDir.getAbsolutePath());
        testConfig.getSystemProperties().put("saveservice_properties", System.getProperty("saveservice_properties"));
        testConfig.getSystemProperties().put("upgrade_properties", System.getProperty("upgrade_properties"));
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
    protected static File executeJMeterScript(JMSpecs testConfig) {
        try {
            LOG.info("Executing jMeter test : ${testConfig.testFile?.getCanonicalPath()}")
            testConfig.resultFile?.delete();

            new JMeterRunner().executeJmeterCommand(testConfig);
            return testConfig.resultFile;

        } catch (IOException e) {
            throw new GradleException("Can't execute test", e);
        }
    }

}
