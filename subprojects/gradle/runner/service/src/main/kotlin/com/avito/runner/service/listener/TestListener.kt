package com.avito.runner.service.listener

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device

interface TestListener {

    /**
     * Actual test execution on device is about to start,
     * am instrument will be called right after
     */
    fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    )

    fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    )
}
