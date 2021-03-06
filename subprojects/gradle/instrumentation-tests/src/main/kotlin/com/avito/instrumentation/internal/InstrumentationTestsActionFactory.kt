package com.avito.instrumentation.internal

import com.avito.instrumentation.internal.finalizer.FinalizerFactory
import com.avito.instrumentation.internal.finalizer.InstrumentationTestActionFinalizer
import com.avito.instrumentation.internal.scheduling.TestsScheduler
import com.avito.instrumentation.internal.scheduling.TestsSchedulerFactory
import com.avito.instrumentation.report.Report
import com.google.common.annotations.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal interface InstrumentationTestsActionFactory {
    fun provideFinalizer(): InstrumentationTestActionFinalizer
    fun provideScheduler(): TestsScheduler

    class Impl : InstrumentationTestsActionFactory {

        private val gson: Gson
        private val sourceReport: Report
        private val schedulerFactory: TestsSchedulerFactory
        private val finalizerFactory: FinalizerFactory

        constructor(params: InstrumentationTestsAction.Params) : this(
            params = params,
            sourceReport = params.reportFactory.createReport(params.reportConfig),
            gson = Companion.gson
        )

        @VisibleForTesting
        internal constructor(
            params: InstrumentationTestsAction.Params,
            sourceReport: Report,
            gson: Gson
        ) {
            this.gson = gson
            this.sourceReport = sourceReport
            this.schedulerFactory = TestsSchedulerFactory.Impl(
                params = params,
                sourceReport = sourceReport,
                gson = gson
            )
            this.finalizerFactory = FinalizerFactory.Impl(
                params = params,
                sourceReport = sourceReport,
                gson = gson
            )
        }

        override fun provideScheduler() = schedulerFactory.create()

        override fun provideFinalizer() = finalizerFactory.create()
    }

    companion object {
        val gson: Gson by lazy {
            GsonBuilder()
                .registerTypeHierarchyAdapter(TestRunResult.Verdict::class.java, TestRunResultVerdictJsonSerializer)
                .setPrettyPrinting()
                .create()
        }
    }
}
