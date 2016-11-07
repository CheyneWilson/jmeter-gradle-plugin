package net.foragerr.jmeter.gradle.plugins

import groovy.transform.Canonical

/**
 * This Class contains the specifications to generate a jmeter-plugins graph (https://jmeter-plugins.org/wiki/JMeterPluginsCMD/)
 * Created by Cheyne Wilson on 11/10/2016.
 */


@Canonical
class ReportSpecs {
    ReportType reportType             // The type of report, AggregateReport, ResponseTimesDistribution, etc

    String outputFileName             // The name of the output file (excluding extension)

    Set<GraphMode> mode = EnumSet.allOf(GraphMode) // The mode, CSV and/or PNG
    Integer width                     //  --width <pixels> for PNG only - width of the image, default is 800
    Integer height                    //  --height <pixels> for PNG only - height of the image, default is 600
    Integer granulation               //  --granulation <ms> granulation time for samples
    Boolean relativeTimes             //  --relative-times <yes/no> use relative X axis times, no will set absolute times
    Boolean aggregateRows             //  --aggregate-rows <yes/no> aggregate all rows into one
    Boolean paintGradient             //  --paint-gradient <yes/no> paint gradient background
    Boolean paintZeroing              //  --paint-zeroing <yes/no> paint zeroing lines
    Boolean paintMarkers              //  --paint-markers <yes/no> paint markers on data points (since 1.1.3)
    Boolean preventOutliers           //  --prevent-outliers <yes/no> prevent outliers on distribution graph
    Integer limitRows                 //  --limit-rows <num of points> limit number of points in row
    Integer forceY                    //  --force-y <limit> force Y axis limit
    Integer hideLowCounts             //  --hide-low-counts <limit> hide points with sample count below limit
    Boolean successFilter            //  --success-filter <true/false> filter samples by success flag (since 0.5.6), possible values are true, false, if not set no filtering on success flag will occur
    String[] includeLabels            //  --include-labels <labels list> include in report only samples with specified labels, comma-separated
    String[] excludeLabels            //  --exclude-labels <labels list> exclude from report samples with specified labels, comma-separated
    Boolean autoScale                 //  --auto-scale <yes/no> enable/disable auto-scale multipliers for perfmon/composite graph
    Float lineWeight                  //  --line-weight <num of pixels> line thickness for graph rows
    // TODO: Not yet supported
    // String[] extractorRegexps      //  --extractor-regexps <regExps list> list of keyRegExp and valRegExp pairs separated with {;}, only used by PageDataExtractorOverTime
    Boolean includeLabelRegex         //  --include-label-regex <true/false> include samples using regular expression
    Boolean excludeLabelRegex         //  --exclude-label-regex <true/false> exclude samples using regular expression
    Integer startOffset               //  --start-offset <sec> include in report only samples with (timestamp - relativeStartTime) > startOffset
    Integer endOffset                 //  --end-offset <sec> include in report only samples with (timestamp - relativeStartTime) < endOffset


    ReportSpecs(){

    }
    /**
     * @return An int value that represents CSV and/or PNG used by PluginsCMDWorker
     */
    int getOutputMode() {
        int value = 0
        Iterator<?> i = this.mode.iterator()
        for(GraphMode m in i) {
            value += m.value
        }
        return value
    }

    /**
     * @return A String representation of the pluginType used by PluginsCMDWorker
     */
    String getPluginType() {
        return  reportType.toString()
    }

}
