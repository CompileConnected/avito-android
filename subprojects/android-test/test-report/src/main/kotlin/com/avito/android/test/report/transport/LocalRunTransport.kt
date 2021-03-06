package com.avito.android.test.report.transport

import android.os.Looper
import com.avito.android.test.report.ReportState
import com.avito.logger.Logger
import com.avito.report.ReportViewer
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.DeviceName
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestName
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage

class LocalRunTransport(
    reportViewerUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val deviceName: DeviceName,
    private val logger: Logger,
    private val reportsApi: ReportsApi
) : Transport, PreTransportMappers {

    private val localBuildId: String? = null

    private val reportViewer: ReportViewer = ReportViewer.Impl(reportViewerUrl)

    override fun send(state: ReportState.Initialized.Started) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread { sendInternal(state) }.apply {
                start()
                join()
            }
        } else {
            sendInternal(state)
        }
    }

    private fun sendInternal(state: ReportState.Initialized.Started) {
        try {
            val testName = TestName(
                className = state.testMetadata.className,
                methodName = state.testMetadata.methodName!!
            )

            val testStaticData = TestStaticDataPackage(
                name = testName,
                device = deviceName,
                description = state.testMetadata.description,
                testCaseId = state.testMetadata.caseId,
                dataSetNumber = state.testMetadata.dataSetNumber,
                externalId = state.testMetadata.externalId,
                tagIds = state.testMetadata.tagIds,
                featureIds = state.testMetadata.featureIds,
                priority = state.testMetadata.priority,
                behavior = state.testMetadata.behavior,
                kind = state.testMetadata.kind,
                flakiness = state.testMetadata.flakiness
            )

            val result = reportsApi.addTest(
                reportCoordinates = reportCoordinates,
                buildId = localBuildId,
                test = AndroidTest.Completed.create(
                    testStaticData = testStaticData,
                    testRuntimeData = TestRuntimeDataPackage(
                        incident = state.incident,
                        dataSetData = state.dataSet?.serialize() ?: emptyMap(),
                        video = state.video,
                        preconditions = transformStepList(state.preconditionStepList),
                        steps = transformStepList(state.testCaseStepList),
                        startTime = state.startTime,
                        endTime = state.endTime
                    ),
                    // local runs already has logcat in place
                    stdout = "",
                    stderr = ""
                )
            )

            logger.info(
                "Report link for test ${testName.name}: ${reportViewer.generateSingleTestRunUrl(result.get())}"
            )

            @Suppress("ControlFlowWithEmptyBody")
            if (reportCoordinates.runId.contains("local", ignoreCase = true)) {
                // todo find a way to display info in user context, it's a secret knowledge about logcat line
            }
        } catch (e: Exception) {
            logger.warn("Report send failed", e)
        }
    }
}
