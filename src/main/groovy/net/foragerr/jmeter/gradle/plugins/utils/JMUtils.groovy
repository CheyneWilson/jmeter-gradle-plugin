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
        List<File> testFiles = new ArrayList<File>();
        if (project.jmeter.testFiles != null) {
            project.jmeter.testFiles.each { File file ->
                if (file.exists() && file.isFile()) {
                    testFiles.add(file);
                } else {
                    throw new GradleException("Test file ${file.getCanonicalPath()} does not exists");
                }
            }
        } else {
            String[] excludes = project.jmeter.excludes == null ?  [] as String[] : project.jmeter.excludes as String[];
            String[] includes = project.jmeter.includes == null ? ["**/*.jmx"] as String[] : project.jmeter.includes as String[];

            log.info("includes: ${includes}")
            log.info("excludes: ${excludes}")
            testFiles.addAll(JMUtils.scanDir(project, includes, excludes, project.jmeter.testFileDir));
            log.info(testFiles.size() + " test files found in folder scan")
        }

        return testFiles;
    }

    static File getJmeterPropsFile(Project project) {
        File propsInSrcDir = new File(project.jmeter.testFileDir,"jmeter.properties");

        //1. Is jmeterPropertyFile defined?
        if (project.jmeter.propFile != null) {
            return project.jmeter.propFile;
        }

        //2. Does jmeter.properties exist in $srcDir/test/jmeter
        else if (propsInSrcDir.exists()) {
            return propsInSrcDir;
        }

        //3. If neither, use the default jmeter.properties
        else{
            File defPropsFile = new File(project.jmeter.workDir, System.getProperty("default_jm_properties"));
            return defPropsFile;
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
        DateFormat defaultFmt = new SimpleDateFormat("yyyyMMdd-HHmm");
		//if resultFilenameTimestamp is "useSaveServiceFormat" use saveservice.format
		if (project.jmeter.resultFilenameTimestamp.equals("useSaveServiceFormat")){
			String saveServiceFormat =  System.getProperty("jmeter.save.saveservice.timestamp_format");
			if (saveServiceFormat.equals("none")) {
                return new File(testConfig.reportDir, "${testConfig.testFile.getName()}.xml");
            }
			try
			{
                DateFormat saveFormat = new SimpleDateFormat(saveServiceFormat);
				return new File(testConfig.reportDir, "${testConfig.testFile.getName()}-${saveFormat.format(new Date())}.xml");
			}
			catch (Exception e)
			{
				// jmeter.save.saveservice.timestamp_format does not contain a valid format
				log.warn("jmeter.save.saveservice.timestamp_format Not defined, using default timestamp format");
			}
		}

        //if resultFilenameTimestamp is "none" do not use a timestamp in filename
        else if (project.jmeter.resultFilenameTimestamp.equals("none")) {
            return new File(testConfig.reportDir, "${testConfig.testFile.getName()}.xml");
        }

        else if (project.jmeter.resultFilenameTimestamp==null) {
            return new File(testConfig.reportDir, "${testConfig.testFile.getName()}-${defaultFmt.format(new Date())}.xml");
        }

        return new File(testConfig.reportDir, "${testConfig.testFile.getName()}-${defaultFmt.format(new Date())}.xml");
    }

	
    static  List<File> scanDir(Project project, String[] includes, String[] excludes, File baseDir) {
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
        return scanResults;
    }
}
