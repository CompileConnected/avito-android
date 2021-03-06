package com.avito.instrumentation.internal.reservation.client

import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.DeviceCoordinate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface ReservationClient {

    class ClaimResult(
        val deviceCoordinates: ReceiveChannel<DeviceCoordinate>
    )

    fun claim(
        reservations: Collection<Reservation.Data>,
        scope: CoroutineScope
    ): ClaimResult

    suspend fun remove(
        podName: String,
        scope: CoroutineScope
    )

    suspend fun release()
}
