package net.foragerr.jmeter.gradle.plugins

/**
 * The plugin types used by PluginsCMDWorker
 *
 * Created by wilsc2 on 17/10/2016.
 */
//TODO: Would be nice if we could source these values from upstream rather than have our own enum we have to maintain.
enum ReportType {
    AGGREGATE_REPORT("AggregateReport"),
    SYNTHESIS_REPORT("SynthesisReport"),
    THREADS_STATE_OVER_TIME("ThreadsStateOverTime"),
    BYTES_THROUGHPUT_OVER_TIME("BytesThroughputOverTime"),
    HITS_PER_SECOND("HitsPerSecond"),
    LATENCIES_OVER_TIME("LatenciesOverTime"),
    PERF_MON("PerfMon"),
    RESPONSE_CODES_PER_SECOND("ResponseCodesPerSecond"),
    RESPONSE_TIMES_DISTRIBUTION("ResponseTimesDistribution"),
    RESPONSE_TIMES_OVER_TIME("ResponseTimesOverTime"),
    RESPONSE_TIMES_PERCENTILES("ResponseTimesPercentiles"),
    THROUGHPUT_VS_THREADS("ThroughputVsThreads"),
    TIMES_VS_THREADS("TimesVsThreads"),
    TRANSACTIONS_PER_SECOND("TransactionsPerSecond"),
    PAGE_DATA_EXTRACTOR_OVER_TIME("PageDataExtractorOverTime")
    //MERGE_RESULTS("MergeResults")

    private final String value;

    private ReportType(String value) {
        this.value = value;
    }

    @Override
    String toString() {
        return this.value
    }
}