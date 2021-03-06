package com.avito.time

import java.util.Date

class StubTimeProvider : TimeProvider {

    private val timeProvider = DefaultTimeProvider()

    lateinit var now: Date

    override fun nowInMillis(): Long = timeProvider.nowInMillis()

    override fun nowInSeconds(): Long = timeProvider.nowInSeconds()

    override fun now(): Date = now

    override fun toDate(seconds: Long): Date = timeProvider.toDate(seconds)
}
