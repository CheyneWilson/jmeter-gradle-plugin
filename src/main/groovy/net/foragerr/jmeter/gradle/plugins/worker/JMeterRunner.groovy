package net.foragerr.jmeter.gradle.plugins.worker

import groovy.io.FileType
import org.gradle.internal.os.OperatingSystem;
import net.foragerr.jmeter.gradle.plugins.JMSpecs
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

/**
 * The JMeterRunner executes a JMSpec using JMeter
 */
class JMeterRunner {

    private final static Logger LOGGER = Logging.getLogger(JMeterRunner.class)

    /**
     * Launch a process from the supplied processBuilder
     *
     * @param processBuilder The processBuilder to create a process from
     */
    private void launchProcess(ProcessBuilder processBuilder) {
        Process p = processBuilder.start()
        p.inputStream.eachLine {println it}  // Write the output to the console
        int processResult = p.waitFor()
        if (processResult != 0) {
            throw new GradleException("Something went wrong during jmeter test execution, Please see jmeter logs for more information")
        }
    }

    /**
     * Run JMeter with the test configuration provided using the chosen runner
     *
     * @param specs This contains the test configuration to run.
     * @param runnerType The type of JMeter instance to run the specs on. Can be one of
     *     GRADLE_PLUGIN,    // Use the JMeter bundled with this plugin
     *     SYSTEM_PATH,      // Use the JMeter installed under %PATH% or $PATH
     *     JMETER_BIN        // Use the JMeter installed under %JMETER_BIN% or $JMETER_BIN
     */
    void executeJmeterCommand(JMSpecs specs, JMeterRunnerType runnerType) {
        List<String> argumentsList

        switch (runnerType) {

            case JMeterRunnerType.GRADLE_PLUGIN:
                argumentsList = createArgumentList(specs)
                break

            case [JMeterRunnerType.JMETER_BIN, JMeterRunnerType.SYSTEM_PATH]:
                argumentsList = new ArrayList<>()
                String jmeter = "jmeter"

                if(runnerType == JMeterRunnerType.JMETER_BIN){
                    String jmeterBin = System.getenv()['JMETER_BIN']
                    jmeter = "${jmeterBin}jmeter"
                }

                if (OperatingSystem.current().isWindows()){
                    argumentsList.addAll(['cmd', '/c', jmeter])
                } else {
                    argumentsList.addAll(['sh', '-c', jmeter])
                }

                argumentsList.addAll(specs.getJmeterCommandLineArguments())
                break

            default:
                LOGGER.error("Unknown JMeterRunnerType")  //  Shouldn't occur
        }

        ProcessBuilder processBuilder = new ProcessBuilder(argumentsList as String[])
        processBuilder.inheritIO()
        processBuilder.directory(specs.workDir)
        processBuilder.redirectErrorStream(true)
        if(runnerType == JMeterRunnerType.JMETER_BIN || runnerType == JMeterRunnerType.SYSTEM_PATH ) {
            processBuilder.environment().put("JVM_ARGS", specs.getJavaCommandLineArguments().join(" "))
        }

        launchProcess(processBuilder);
    }

    /**
     * Create the argument list used by the JMETER_PLUGIN runner
     *
     * @param specs This contains the test configuration to run.
     * @return A list of arguments that can be supplied to a ProcessBuilder
     */
    private List<String> createArgumentList(JMSpecs specs) {
        final String JAVA_RUNTIME = "java"
        final String LAUNCH_CLASS = "org.apache.jmeter.NewDriver"  // This hasn't changed in years

        List<String> argumentsList = new ArrayList<>()
        argumentsList.add(JAVA_RUNTIME)

        argumentsList.addAll(specs.getJavaCommandLineArguments())

        argumentsList.add("-cp")
        String workDir = specs.workDir.getAbsolutePath()
        argumentsList.add(workDir + File.separator + "lib" + System.getProperty("path.separator") +
            workDir + File.separator + "lib" + File.separator + "ext" + System.getProperty("path.separator") +
            generatePatherJar(workDir).getAbsolutePath())

        argumentsList.add(LAUNCH_CLASS)
        List<String>  args = specs.getJmeterCommandLineArguments()
        LOGGER.info("JMeter is called with the following command line arguments: " + args.toString());
        argumentsList.addAll(args)

        LOGGER.debug("Command to run is $argumentsList")

        return argumentsList
    }

    /**
     * As a workaround for the command argument length being too long for Windows, more than 8K chars, generate
     *   a tmp .jar as a path container for the long classpath.
     *
     * @param workDir working directory of executed build
    */
    private File generatePatherJar(String workDir){
        File patherJar = new File(new File(workDir), "pather.jar")
        if (patherJar.exists()) patherJar.delete()
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        StringBuilder cpBuilder = new StringBuilder();

        //add from jmeter/lib
        new File(workDir, "lib").eachFileRecurse(FileType.FILES){ file ->
            cpBuilder.append(file.toURI())
            cpBuilder.append(" ")
        }
        //add from jmeter/lib/ext
        new File(workDir, "lib/ext").eachFileRecurse(FileType.FILES){ file ->
            cpBuilder.append(file.toURI())
            cpBuilder.append(" ")
        }

        URL[] classPath = ((URLClassLoader)this.getClass().getClassLoader()).getURLs()
        classPath.each {u ->
            cpBuilder.append(u.getPath())
            cpBuilder.append(" ")
        }
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, cpBuilder.substring(0, cpBuilder.size() - 1) )
        JarOutputStream target = new JarOutputStream(new FileOutputStream(patherJar.getCanonicalPath()), manifest);
        target.close();
        return patherJar
    }
}
