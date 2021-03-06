@file:Suppress("UnstableApiUsage", "MoveVariableDeclarationIntoWhen")

package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazyProperty
import com.avito.logger.SimpleLoggerFactory
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

/**
 * Warning! used in build.gradle files
 */
fun Project.gitState(): Provider<GitState> =
    lazyProperty("GIT_STATE_PROVIDER") { project ->
        project.objects.property<GitState>().apply {
            val strategy = project.getOptionalStringProperty("avito.git.state", default = "env")

            val loggerFactory = SimpleLoggerFactory()

            set(
                when (strategy) {
                    "local" -> GitLocalStateImpl.from(
                        project = project,
                        loggerFactory = loggerFactory
                    )
                    "env" -> GitStateFromEnvironment.from(
                        project = project,
                        loggerFactory = loggerFactory
                    )
                    else -> throw RuntimeException("Unknown git state strategy: $strategy")
                }
            )
        }
    }
