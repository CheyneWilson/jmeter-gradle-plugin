package net.foragerr.jmeter.gradle.plugins.worker

/**
 * The instance of JMeter used to execute tests can be changed via this parameter.
 *
 * The default is to use the version of JMeter assembled by this plugin, but another instance that is installed can
 * be used which is useful if you have a custom JMeter install that can't easily be replicated in this build.
 *
 * Created by Cheyne Wilson on 26/09/2016.
 */
enum JMeterRunnerType  {
    GRADLE_PLUGIN,    // Use the JMeter bundled with this plugin
    SYSTEM_PATH,      // Use the JMeter installed under %PATH% or $PATH
    JMETER_BIN        // Use the JMeter installed under %JMETER_BIN% or $JMETER_BIN
}