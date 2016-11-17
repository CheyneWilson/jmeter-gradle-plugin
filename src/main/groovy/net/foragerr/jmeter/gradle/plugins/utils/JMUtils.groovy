package net.foragerr.jmeter.gradle.plugins.utils

import groovy.util.logging.Slf4j
import net.foragerr.jmeter.gradle.plugins.JMSpecs
import org.apache.tools.ant.DirectoryScanner
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logging

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Created by foragerr@gmail.com on 7/19/2015.
 */
@Slf4j
class JMUtils {

    static final LOG = Logging.getLogger(getClass())

    static List<File> getListOfTestFiles(Project project){
        List<File> testFiles = new ArrayList<File>()
        if (project.jmeter.testFiles != null) {
            project.jmeter.testFiles.each { File file ->
                if (file.exists() && file.isFile()) {
                    testFiles.add(file)
                } else {
                    throw new GradleException("Test file ${file.getCanonicalPath()} does not exists")
                }
            }
        } else {
            String[] excludes = project.jmeter.excludes == null ?  [] as String[] : project.jmeter.excludes as String[]
            String[] includes = project.jmeter.includes == null ? ["**/*.jmx"] as String[] : project.jmeter.includes as String[]

            log.info("includes: ${includes}")
            log.info("excludes: ${excludes}")
            testFiles.addAll(scanDir(project, includes, excludes, project.jmeter.testFileDir))
            log.info(testFiles.size() + " test files found in folder scan")
        }

        return testFiles
    }

    static File getJmeterPropsFile(Project project) {
        File propsInSrcDir = new File(project.jmeter.testFileDir, "jmeter.properties")

        //1. Is jmeterPropertyFile defined?
        if (project.jmeter.propFile != null) {
            log.info("Using property file defined in jmeter block (from build.gradle), ${project.jmeter.propFile}")
            return project.jmeter.propFile
        }

        //2. Does jmeter.properties exist in $srcDir/test/jmeter
        else if (propsInSrcDir.exists()) {
            log.info("Using property file ${propsInSrcDir}")
            return propsInSrcDir
        }

        //3. If neither, use the default jmeter.properties, usually in build/jmeter
        else{
            File defPropsFile = new File(project.jmeter.workDir, "jmeter.properties")
            log.info("Using property file ${defPropsFile}")
            return defPropsFile
        }
    }

    /**
     * Generate a file for the test results
     *
     * @param testConfig  The test config to get the resultFile for
     * @param project
     * @return A file to be used for test results
     */
	static File getResultFile(JMSpecs testConfig, Project project) {
        if (testConfig.testFile == null){
            return null
        }
        DateFormat defaultFmt = new SimpleDateFormat("yyyyMMdd-HHmm")

        // TODO: Doesn't respect the overrides in user.properties .. I guess coz we're not loading them, need to fix elsewhere
        // Eventually we may drop support for XML output, if that is the direction JMeter is heading.
        String suffix = System.properties.'jmeter.save.saveservice.output_format'
        if (suffix in ["csv", "xml"]){
            // use the suffix
        } else {
            suffix = "csv" // default
        }

		if (project.jmeter.resultFilenameTimestamp == "useSaveServiceFormat"){
			String saveServiceFormat =  System.getProperty("jmeter.save.saveservice.timestamp_format")
			if (saveServiceFormat == "none") {
                return new File(testConfig.reportDir, "${testConfig.testFile.getName()}.$suffix")
            }
			try
			{
                DateFormat saveFormat = new SimpleDateFormat(saveServiceFormat)
				return new File(testConfig.reportDir, "${testConfig.testFile.getName()}-${saveFormat.format(new Date())}.$suffix")
			}
			catch (Exception e)
			{
				// jmeter.save.saveservice.timestamp_format does not contain a valid format
				log.warn("jmeter.save.saveservice.timestamp_format Not defined, using default timestamp format")
			}
		}

        //if resultFilenameTimestamp is "none" do not use a timestamp in filename
        else if (project.jmeter.resultFilenameTimestamp == "none") {
            return new File(testConfig.reportDir, "${testConfig.testFile.getName()}.$suffix")
        }

        else if (project.jmeter.resultFilenameTimestamp == null) {
            return new File(testConfig.reportDir, "${testConfig.testFile.getName()}-${defaultFmt.format(new Date())}.$suffix")
        }

        return new File(testConfig.reportDir, "${testConfig.testFile.getName()}-${defaultFmt.format(new Date())}.$suffix")
    }

	
    static  List<File> scanDir(String[] includes, String[] excludes, File baseDir) {
        List<File> scanResults = new ArrayList<File>()
        if (baseDir.exists()) {
            DirectoryScanner scanner = new DirectoryScanner()
            scanner.setBasedir(baseDir)
            scanner.setIncludes(includes)
            scanner.setExcludes(excludes)
            scanner.scan()
            for (String result : scanner.getIncludedFiles()) {
                scanResults.add(new File(scanner.getBasedir(), result))
            }
        } else {
            LOG.warn("Attempted to load Jmeter files from {}, but this directory does not exist.", baseDir)
        }
        return scanResults
    }
}
