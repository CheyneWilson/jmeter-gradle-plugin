package net.foragerr.jmeter.gradle.plugins

import net.foragerr.jmeter.gradle.plugins.utils.ErrorScanner
import net.foragerr.jmeter.gradle.plugins.utils.JMUtils
import net.foragerr.jmeter.gradle.plugins.worker.JMeterRunner
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public class TaskJMRun extends DefaultTask {

    protected final Logger log = Logging.getLogger(getClass());

    File testFile = null

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

    File jmLog = null

    String maxHeapSize
    String minHeapSize

    Boolean remote

    /**
     * Run a Jmeter Test
     *
     * Run a Jmeter Test using the configuration from the JMPluginExtension and inside this task.
     * The configuration inside the jmRum task can override partially or fully any JMPluginExtension configuration.
     *
     * @return
     */

    @TaskAction
    jmRun() {

        //Run Tests
        List<File> resultList = new ArrayList<File>();

        if (testFile == null) {
            //Get List of test files to run from the jmeter config element
            List<File> testFiles = JMUtils.getListOfTestFiles(project)
            for (File testFile : testFiles) {
                JMSpecs testConfig = setupTestConfig(testFile);
                resultList.add(executeJmeterTest(testConfig));
            }
        } else {
            // Run the test defined in the task instead of in the jmeter config
            JMSpecs testConfig = setupTestConfig(testFile);
            resultFile = executeJmeterTest(testConfig)
            resultList.add(resultFile);
        }
        //Scan for errors
        checkForErrors(resultList);
        project.jmeter.jmResultFiles = resultList;

    }

    private JMSpecs setupTestConfig(File testFile){
        JMSpecs testConfig = new JMSpecs();

        testConfig.jmTestFile = testFile
        testConfig.resultFile = resultFile ?: JMUtils.getResultFile(testFile, project)

        testConfig.workDir = workDir ?: project.jmeter.workDir

        testConfig.jmLog = jmLog ?: project.jmeter.jmLog

        testConfig.propFile  = propFile ?: JMUtils.getJmeterPropsFile(project)
        testConfig.addPropFiles = addPropFiles ?: project.jmeter.jmAddProp
        testConfig.jmeterProperties = jmeterProperties ?: project.jmeter.jmUserProperties

        testConfig.systemPropertiesFiles = systemPropertiesFiles ?: project.jmeter.jmSystemPropertiesFiles
        testConfig.systemProperties = systemProperties ?: project.jmeter.jmSystemProperties ?: new HashMap<String, ?>()

        testConfig.globalProperties = globalProperties ?: project.jmeter.globalProperties ?: new HashMap<String, ?>()

        // TODO: Decide when/where these get set
        testConfig.getSystemProperties().put("search_paths", System.getProperty("search_paths"));
        testConfig.getSystemProperties().put("jmeter.home", testConfig.workDir.getAbsolutePath());
        testConfig.getSystemProperties().put("saveservice_properties", System.getProperty("saveservice_properties"));
        testConfig.getSystemProperties().put("upgrade_properties", System.getProperty("upgrade_properties"));
        testConfig.getSystemProperties().put("log_file", testConfig.jmLog);
        testConfig.getSystemProperties().put("jmeter.save.saveservice.output_format", "xml");

        testConfig.maxHeapSize = maxHeapSize ?: project.jmeter.maxHeapSize
        testConfig.minHeapSize = minHeapSize ?: project.jmeter.minHeapSize


        testConfig.remote = remote != null ? remote : project.jmeter.remote

        return testConfig;
    }

    private void checkForErrors(List<File> results) {
        ErrorScanner scanner = new ErrorScanner(project.jmeter.ignoreErrors, project.jmeter.ignoreFailures);
        try {
            for (File file : results) {
                if (scanner.scanForProblems(file)) {
                    log.warn("There were test errors.  See the jmeter logs for details");
                }
            }
        } catch (IOException e) {
            throw new GradleException("Can't read log file", e);
        }
    }

    private File executeJmeterTest(JMSpecs testConfig) {
        try {
            log.info("Executing jMeter test : ${testConfig.jmTestFile.getCanonicalPath()}")

            testConfig.resultFile.delete();

//            log.info("JMeter is called with the following command line arguments: " + testConfig.getA.toString());


            new JMeterRunner().executeJmeterCommand(testConfig);
            return testConfig.resultFile;

        } catch (IOException e) {
            throw new GradleException("Can't execute test", e);
        }
    }
}
