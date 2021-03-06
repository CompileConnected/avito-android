package com.avito.runner.test

sealed class StubActionResult<T> {

    abstract fun get(): T

    class Success<T>(private val result: T) : StubActionResult<T>() {
        override fun get(): T = result
    }

    class Failed<T>(private val t: Throwable) : StubActionResult<T>() {
        override fun get(): T {
            throw t
        }
    }
}
