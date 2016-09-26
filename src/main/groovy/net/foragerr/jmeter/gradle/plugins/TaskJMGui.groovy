package net.foragerr.jmeter.gradle.plugins

import net.foragerr.jmeter.gradle.plugins.utils.JMUtils
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

class TaskJMGui extends TaskJMExec {

    static final Logger LOG = Logging.getLogger(getClass());

    @TaskAction
    jmGui() {
        JMSpecs testConfig

        if (testFile == null) {
            //Get List of test files to run from the jmeter config element
            List<File> testFiles = JMUtils.getListOfTestFiles(project)

            if (testFiles.size() >= 0){
                LOG.info("No test file found.")
                // Start the gui using without any test file
                testConfig = setupTestConfig(testFiles[0]);
            } else {
                // Start the gui using the first testfile defined in the jmeter config
                testConfig = setupTestConfig(null);
            }

        } else {
            // Start the gui using the testfile defined in the task instead of in the jmeter config
            testConfig = setupTestConfig(testFile);
        }

        executeJMeterScript(testConfig, project.jmeter.runnerType);
    }


    protected  JMSpecs  setupTestConfig(File testFile){
        JMSpecs testConfig = super.setupTestConfig(testFile)
        testConfig.nongui = false
        return testConfig
    }


}
