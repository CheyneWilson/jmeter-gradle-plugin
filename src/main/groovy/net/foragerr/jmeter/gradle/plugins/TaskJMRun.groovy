package net.foragerr.jmeter.gradle.plugins

import net.foragerr.jmeter.gradle.plugins.utils.ErrorScanner
import net.foragerr.jmeter.gradle.plugins.utils.JMUtils
import net.foragerr.jmeter.gradle.plugins.worker.JMeterRunner
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

public class TaskJMRun extends DefaultTask {

    protected final Logger log = Logging.getLogger(getClass());

    File testFile = null

    File resultFile = null

    List<String> userProperties = null      //maps to -J, --jmeterproperty

    List<File> jmSystemPropertiesFiles = null //maps to -S, --systemPropertyFile
    List<String> jmSystemProperties = null    //maps to -D, --systemproperty

    File jmPropertyFile = null //maps to -p, --propfile
    File jmAddProp = null      //maps to -q, --addprop

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
                JMTestConfiguration testConfig = setupTestConfig(testFile);
                resultList.add(executeJmeterTest(testConfig));
            }
        } else {
            // Run the test defined in the task instead of in the jmeter config
            JMTestConfiguration testConfig = setupTestConfig(testFile);
            resultList.add(executeJmeterTest(testConfig));
        }
        //Scan for errors
        checkForErrors(resultList);
        project.jmeter.jmResultFiles = resultList;

    }

    private JMTestConfiguration setupTestConfig(File testFile){
        JMTestConfiguration testConfig = new JMTestConfiguration();

        testConfig.jmTestFile = testFile
        testConfig.resultFile = resultFile ?: JMUtils.getResultFile(testFile, project)

        testConfig.userProperties = userProperties ?: project.jmeter.jmUserProperties

        testConfig.jmSystemPropertiesFiles = jmSystemPropertiesFiles ?:project.jmeter.jmSystemPropertiesFiles
        testConfig.jmSystemProperties = jmSystemProperties ?: project.jmeter.jmSystemProperties

        testConfig.jmPropertyFile  = jmPropertyFile ?: JMUtils.getJmeterPropsFile(project)
        testConfig.jmAddProp = jmAddProp ?: project.jmeter.jmAddProp

        testConfig.workDir = workDir ?: project.jmeter.workDir
        testConfig.jmLog = jmLog ?: project.jmeter.jmLog

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

    private File executeJmeterTest(JMTestConfiguration testConfig) {
        try {
            log.info("Executing jMeter test : ${testConfig.jmTestFile.getCanonicalPath()}")

            testConfig.resultFile.delete();

            //Build Jmeter command args
            List<String> args = new ArrayList<String>();
            args.addAll(Arrays.asList("-n",
                    "-t", testConfig.jmTestFile.getCanonicalPath(),
                    "-l", testConfig.resultFile.getCanonicalPath(),
                    "-p", testConfig.jmPropertyFile.getCanonicalPath()
            ));

            if (testConfig.jmAddProp)
                args.addAll(Arrays.asList("-q", testConfig.jmAddProp.getCanonicalPath()))

            //User provided sysprops
            if (testConfig.jmSystemPropertiesFiles != null) {
                for (File systemPropertyFile : testConfig.jmSystemPropertiesFiles) {
                    if (systemPropertyFile.exists() && systemPropertyFile.isFile()) {
                        args.addAll(Arrays.asList("-S", systemPropertyFile.getCanonicalPath()));
                    }
                }
            }

            List<String> userSysProps = new ArrayList<String>()
            if (testConfig.jmSystemProperties != null) {
                for (String systemProperty : testConfig.jmSystemProperties) {
                    userSysProps.addAll(Arrays.asList(systemProperty));
                    log.info(systemProperty);
                }
            }

            if (testConfig.userProperties != null) {
                testConfig.userProperties.each { property -> args.add("-J ${property}") }
            }

            if (testConfig.remote) {
                args.add("-r");
            }

            log.info("JMeter is called with the following command line arguments: " + args.toString());

            JMSpecs specs = new JMSpecs();
            specs.getUserSystemProperties().addAll(userSysProps);
            specs.getSystemProperties().put("search_paths", System.getProperty("search_paths"));
            specs.getSystemProperties().put("jmeter.home", testConfig.workDir.getAbsolutePath());
            specs.getSystemProperties().put("saveservice_properties", System.getProperty("saveservice_properties"));
            specs.getSystemProperties().put("upgrade_properties", System.getProperty("upgrade_properties"));
            specs.getSystemProperties().put("log_file", testConfig.jmLog);
            specs.getSystemProperties().put("jmeter.save.saveservice.output_format", "xml");
            specs.getJmeterProperties().addAll(args);
            specs.setMaxHeapSize(testConfig.maxHeapSize.toString());
            specs.setMinHeapSize(testConfig.minHeapSize.toString());

            new JMeterRunner().executeJmeterCommand(specs, testConfig.workDir.getAbsolutePath());
            return testConfig.resultFile;

        } catch (IOException e) {
            throw new GradleException("Can't execute test", e);
        }
    }




}
