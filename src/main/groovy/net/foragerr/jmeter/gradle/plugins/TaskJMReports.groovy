package net.foragerr.jmeter.gradle.plugins

import kg.apc.jmeter.PluginsCMDWorker
import net.foragerr.jmeter.gradle.plugins.utils.JMUtils
import net.foragerr.jmeter.gradle.plugins.utils.ReportTransformer
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.jmeter.util.JMeterUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.rendersnake.HtmlAttributes
import org.rendersnake.HtmlCanvas
import org.rendersnake.tools.PrettyWriter

import javax.xml.transform.TransformerException

import static org.rendersnake.HtmlAttributesFactory.class_
import static org.rendersnake.HtmlAttributesFactory.lang

public class TaskJMReports extends DefaultTask {

    protected final Logger log = Logging.getLogger(getClass())

    @InputDirectory
    @Optional
    File reportDir

    List<ReportSpecs> reports = new ArrayList<>()

//	TODO: createReports should only kick-in if there are new jtl files to process.

    @InputFile
    @Optional
    File resultFile

    @TaskAction
    jmCreateReport(){

        List<File> jmResultFiles = new ArrayList<File>()

        reportDir = reportDir ?: project.jmeter.reportDir ?: new File(project.buildDir, "jmeter-report")
        if (resultFile != null) {
            jmResultFiles.add(resultFile)
        }
		else {
            //Get List of resultFiles
            jmResultFiles.addAll(JMUtils.scanDir(["**/*.xml"] as String[], [] as String[], reportDir))
        }

        // If no reports are specified, use all values in report types
        if (reports.isEmpty()){
            for (ReportType reportType : ReportType.values()){
                ReportSpecs specs = new ReportSpecs()
                specs.reportType = reportType

                if(reportType in [ReportType.AGGREGATE_REPORT, ReportType.SYNTHESIS_REPORT]){
                    // Only CSV is supported for AGGREGATE_REPORT, and SYNTHESIS_REPORT
                    specs.mode = EnumSet.of(ReportSpecs.GraphMode.CSV)
                }
                reports.add(specs)
            }
        }

        // Set the output file names
        int i = 1
        // for (ReportSpecs specs in reports){   // Results in new instance that not the one referenced in reports
        for (specs in reports){

            // If someone specifies a name, use that, else determine a file name
            if (specs.outputFileName == null) {
                // Often we want to have different graphs of success or failure, so we let that influence the naming pattern
                // We also want to number the files in case there are repeats with different settings
                // Could expand the conditions that affect the name, or better, use some kind of pattern
                // But instead we may move to a different way of rendering results..?
                String success = specs.successFilter == null ? "" : (specs.successFilter ? " (success)" : " (failure)")
                specs.outputFileName = "${name}_${i}_${specs.reportType}${success}"
                i += 1
            }

        }

		if (jmResultFiles.size()==0) {
            log.warn("There are no results file to create reports from")
        }
		
        if (project.jmeter.enableReports == true) {
            for (File resultFile : jmResultFiles) {
                makeHTMLReport(resultFile, reports)
            }
        }
        if (project.jmeter.enableExtendedReports == true) {
            for (File resultFile : jmResultFiles) {
                makeExtendedReports(resultFile, reports)
            }
        }
    }

    /**
     * Generate the PNG and CSV files from the resultFile and reportSpecs and create an HTML page containing them.
     *
     * @param resultFile The JMeter result file to use (JTL or CSV)
     * @param reportSpecs The specifications to use for the generated file(s).
     * @throws IOException
     */
	private void makeExtendedReports(File resultFile, List<ReportSpecs> reportSpecs) throws IOException {
        File workDir = project.jmeter.workDir ?: new File("./build/jmeter")

        String name = FilenameUtils.removeExtension(resultFile.getName());
        initializeJMeter(name, JMUtils.getJmeterPropsFile(project), workDir , reportDir);

        try {
            log.info("Creating Extended Reports {}", resultFile.getName());
            for (ReportSpecs specs : reportSpecs) {
                generateReportFile(resultFile, specs)
            }
            makeHTMLExtendedReport(reportSpecs, resultFile)
        } catch (Throwable e) {
            log.error("Failed to create extended report for " + resultFile, e);
        }
	}

    /**
     * Generate a PNG and/or CSV from a supplied JMeter results file.
     *
     * @param resultFile The JMeter result file to use (JTL or CSV)
     * @param specs The specifications to use for the generated file(s).
     */
    private void generateReportFile(File resultFile, ReportSpecs specs) {

        // TODO: We don't need to do this every time
        File imgDir = new File(reportDir, "extReport-img")
        File csvDir = new File(reportDir, "extReport-csv")
        imgDir.mkdirs()
        csvDir.mkdirs()

        PluginsCMDWorker worker = new PluginsCMDWorker()
        try {
            worker.setInputFile(resultFile.getAbsolutePath());
            worker.setPluginType(specs.pluginType);
            worker.addExportMode(specs.getOutputMode())

            if (specs.mode.contains(ReportSpecs.GraphMode.PNG)){
                worker.setOutputPNGFile(imgDir.getCanonicalPath() + File.separator + "${specs.outputFileName}.png");
            }
            if (specs.mode.contains(ReportSpecs.GraphMode.CSV)){
                worker.setOutputCSVFile(csvDir.getCanonicalPath() + File.separator + "${specs.outputFileName}.csv");
            }

            if (specs.width != null ){ worker.graphWidth = specs.width }
            if (specs.height != null ){ worker.graphHeight = specs.height }
            if (specs.granulation != null ){ worker.granulation = specs.granulation }
            if (specs.relativeTimes != null ){ worker.relativeTimes = specs.relativeTimes ? 1 : 0 }
            if (specs.aggregateRows != null ){ worker.aggregate = specs.aggregateRows ? 1 : 0}
            if (specs.paintGradient != null ){ worker.gradient = specs.paintGradient ? 1 : 0 }

            if (specs.paintZeroing != null ){ worker.zeroing = specs.paintZeroing ? 1 : 0 }
            if (specs.paintMarkers != null ){ worker.markers = specs.paintMarkers ? 1 : 0 }
            if (specs.preventOutliers != null ){ worker.preventOutliers = specs.preventOutliers ? 1 : 0 }
            if (specs.limitRows != null ){ worker.rowsLimit = specs.limitRows }
            if (specs.forceY != null ){ worker.forceY = specs.forceY}
            if (specs.hideLowCounts != null ){ worker.hideLowCounts = specs.hideLowCounts ? 1 : 0 }
            if (specs.successFilter != null ){ worker.successFilter = specs.successFilter ? 1 : 0 }

            if (specs.includeLabels != null ){ worker.includeLabels = specs.includeLabels.join(",") }
            if (specs.excludeLabels != null ){ worker.excludeLabels = specs.excludeLabels.join(",") }
            if (specs.autoScale != null ){ worker.autoScaleRows = specs.autoScale ? 1 : 0 }
            if (specs.lineWeight != null ){  worker.lineWeight = specs.lineWeight }

            //worker. = specs.extractorRegexps  // TODO: Update version of JmeterPlugins so this is accessible
            if (specs.includeLabelRegex != null ){  worker.includeSamplesWithRegex = specs.includeLabelRegex ? 1 : 0 }
            if (specs.excludeLabelRegex != null ){  worker.excludeSamplesWithRegex = specs.excludeLabelRegex ? 1 : 0 }
            if (specs.startOffset != null ){  worker.startOffset = specs.startOffset as String }
            if (specs.endOffset != null ){  worker.endOffset = specs.endOffset as String}

            worker.doJob();
        } catch (Exception e) {
            log.error("Failed to create report: ${specs.reportType} for ${name} due to: ", e);
        }

    }


    private static void initializeJMeter(String name, File jmProps, File jmHome, File reportBaseDir) {
        // Initialize JMeter settings..
        JMeterUtils.setJMeterHome(jmHome.getAbsolutePath());
        JMeterUtils.loadJMeterProperties(jmProps.getAbsolutePath());
        JMeterUtils.setProperty("log_file", reportBaseDir.getCanonicalPath() + File.separator + name + ".log");
        JMeterUtils.initLogging();
        JMeterUtils.initLocale();
    }


    /**
     * Makes the JMeter HTML Report
     * @param resultFile
     * @param specs
     */
    private void makeHTMLReport(File resultFile, List<ReportSpecs> specs) {
        try {
            ReportTransformer transformer;
            transformer = new ReportTransformer(getXslt());
            log.info("Building HTML Report.");

            String reportTitle = project.jmeter.reportTitle ?: "Generated from: " + resultFile.getName();
            final File outputFile = new File(toOutputFileName(resultFile.getAbsolutePath()));
            log.info("transforming: {} to {}", resultFile, outputFile);
            transformer.transform(resultFile, outputFile, reportTitle);

        } catch (FileNotFoundException e) {
            log.error("Can't transform result", e);
            throw new GradleException("Error writing report file jmeter file.", e);
        } catch (TransformerException e) {
            log.error("Can't transform result", e);
            throw new GradleException("Error transforming jmeter results", e);
        } catch (IOException e) {
            log.error("Can't transform result", e);
            throw new GradleException("Error copying resources to jmeter results", e);
        }  catch (Exception e) {
            log.error("Can't transform result", e);
        }
    }

    private String toOutputFileName(String fileName) {
        if (fileName.endsWith(".xml")) {
            return fileName.replace(".xml", project.jmeter.reportPostfix + ".html");
        } else {
            return fileName + project.jmeter.reportPostfix;
        }
    }

    private InputStream getXslt() throws IOException {
        if (project.jmeter.reportXslt == null) {
            //if we are using the default report, also copy the images out.
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("reports/collapse.jpg"), new FileOutputStream(reportDir.getPath() + File.separator + "collapse.jpg"))
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("reports/expand.jpg"), new FileOutputStream(reportDir.getPath() + File.separator + "expand.jpg"))
            log.debug("Using reports/jmeter-results-detail-report_21.xsl for building report")
            return Thread.currentThread().getContextClassLoader().getResourceAsStream("reports/jmeter-results-detail-report_21.xsl")
        } else {
            log.debug("Using {} for building report", project.jmeter.reportXslt);
            return new FileInputStream(project.jmeter.reportXslt);
        }
    }


    private makeHTMLExtendedReport(List<ReportSpecs> reportSpecs, File resultFile){
        //Get list of images
        File imgDir = new File(reportDir, "extReport-img")
        String[] includePattern = reportSpecs
            .findAll {
                it.mode.contains(ReportSpecs.GraphMode.PNG)
            }.collect {
                it.outputFileName + ".png"
            }

//      Don't really need the complexity of using scan dir at the includePattern is now just file names
//      File[] listOfImages = JMUtils.scanDir(includePattern, [] as String[], imgDir)

        new File(reportDir.getPath(), "assets").mkdirs()
        FileOutputStream fos = new FileOutputStream(reportDir.getPath() + "/assets/style.css")
        IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("reports/assets/style.css"), fos)

        //create HTML
        HtmlCanvas html = new HtmlCanvas(new PrettyWriter());
        html.html(lang("en"))
            .head()
                .title().content("Extended Test Report - generated by jmeter-gradle-plugin")
                .macros().stylesheet("assets/style.css")
        html._head()
            .body()
                .div(class_("simple"))
                    .h1().content("Extended Test Report")
                ._div()

        includePattern.each{ String imageFileName ->
            html.div(class_("image"))
                .p().content(imageFileName)
                .img(new HtmlAttributes().src(imgDir.getName() + File.separator + imageFileName))
            ._div()
        }

        html.p().content("Generated by jmeter-gradle-plugin")

        html._body()._html()


        new File(reportDir, resultFile.getName() + "-extReport.html").write(html.toHtml())
    }

}