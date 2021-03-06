package com.avito.android.stats

class StubStatsdSender : StatsDSender {

    val paths = mutableListOf<String>()
    val metrics = mutableListOf<StatsMetric>()

    override fun send(prefix: String, metric: StatsMetric) {
        paths.add(prefix)
        metrics.add(metric)
    }
}
