package net.foragerr.jmeter.gradle.plugins

import net.foragerr.jmeter.gradle.plugins.utils.ErrorScanner
import net.foragerr.jmeter.gradle.plugins.utils.JMUtils
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

class TaskJMRun extends TaskJMExec {

    static final Logger LOG = Logging.getLogger(getClass());

    Boolean  nongui = false                      //maps to -n, --nongui,    run JMeter in nongui mode

    @TaskAction
    jmRun() {
        //Run Tests
        List<File> resultList = new ArrayList<File>()

        if (testFile == null) {
            //Get List of test files to run from the jmeter config element
            List<File> testFiles = JMUtils.getListOfTestFiles(project)

            if (testFiles.size() == 0){
                LOG.error("No test file found.")
                return
            }

            else {
                for (File testFile : testFiles) {
                    JMSpecs testConfig = setupTestConfig(testFile)
                    resultList.add(executeJMeterScript(testConfig, project.jmeter.runnerType))
                }
            }
        } else {
            // Run the test defined in the task instead of in the jmeter config
            JMSpecs testConfig = setupTestConfig(testFile)
            resultFile = executeJMeterScript(testConfig, project.jmeter.runnerType)
            resultList.add(resultFile)
        }
        //Scan for errors
        checkForErrors(resultList)
        project.jmeter.jmResultFiles = resultList
    }

    @Override
    protected  JMSpecs  setupTestConfig(File testFile){
        JMSpecs testConfig = super.setupTestConfig(testFile)
        testConfig.nongui = nongui ?: project.jmeter.nongui
        return testConfig
    }

    private void checkForErrors(List<File> results) {
        ErrorScanner scanner = new ErrorScanner(project.jmeter.ignoreErrors, project.jmeter.ignoreFailures)
        try {
            for (File file : results) {
                if (scanner.scanForProblems(file)) {
                    LOG.warn("There were test errors. See the jmeter logs for details")
                }
            }
        } catch (IOException e) {
            throw new GradleException("Can't read log file", e)
        }
    }
}
