/**
 * The mode out output that PluginsCMDWorker supports
 *
 * Created by Cheyne Wilson on 2/11/2016.
 */

package net.foragerr.jmeter.gradle.plugins
import kg.apc.jmeter.PluginsCMDWorker

enum GraphMode {
    PNG(PluginsCMDWorker.EXPORT_PNG),
    CSV(PluginsCMDWorker.EXPORT_CSV)

    final int value

    private GraphMode(int value) {
        this.value = value
    }
}