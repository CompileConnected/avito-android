package com.avito.instrumentation.impact

import com.avito.android.isAndroid
import com.avito.bytecode.DIRTY_STUB
import com.avito.impact.ConfigurationType
import com.avito.impact.ModifiedProject
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.configuration.internalModule
import com.avito.impact.util.AndroidPackage
import com.avito.impact.util.AndroidProject
import com.avito.impact.util.Test
import com.avito.instrumentation.impact.metadata.ScreenToModulePath
import com.avito.instrumentation.impact.model.AffectionType
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import org.gradle.util.Path

internal data class ImpactSummary(
    val affectedPackages: AffectedPackages,
    val affectedTests: AffectedTests,
    val allTests: Set<Test>,
    val addedTests: Set<Test>,
    val modifiedTests: Set<Test>,
    val skippedTests: Set<Test>,
    val testsToRun: Set<Test>
) {
    data class AffectedPackages(
        val implementation: Set<AndroidPackage>,
        val androidTestImplementation: Set<AndroidPackage>
    )

    data class AffectedTests(
        val implementation: Set<Test>,
        val androidTestImplementation: Set<Test>,
        val codeChanges: Set<Test>
    )
}

internal class AnalyzeTestImpactAction(
    private val targetModule: AndroidProject,
    private val bytecodeAnalyzeSummary: BytecodeAnalyzeSummary,
    private val packageFilter: String?,
    private val finder: ModifiedProjectsFinder,
    loggerFactory: GradleLoggerFactory
) {

    private val logger = loggerFactory.create<AnalyzeTestImpactAction>()

    private val tests: Set<Test> = bytecodeAnalyzeSummary
        .testsByScreen
        .values
        .flatten()
        .toSet()

    private val affectedByCodeChanges = bytecodeAnalyzeSummary
        .testsModifiedByUser
        .plus(bytecodeAnalyzeSummary.testsAffectedByDependentOnUserChangedCode)
        .map { it.methods }
        .flatten()
        .toSet()

    private val addedTests: Set<Test> = bytecodeAnalyzeSummary
        .testsModifiedByUser
        .filter { it.affectionType == AffectionType.TEST_ADDED }
        .flatMap { it.methods }
        .toSet()

    private val modifiedTests: Set<Test> = bytecodeAnalyzeSummary
        .testsModifiedByUser
        .filter { it.affectionType == AffectionType.TEST_MODIFIED }
        .flatMap { it.methods }
        .toSet()

    private val filteredTest =
        if (packageFilter != null) tests.filter { it.startsWith(packageFilter) }.toSet() else tests

    fun computeImpact(): ImpactSummary {
        val affectedImplProjects = findModifiedAndroidProjects(
            finder.findModifiedProjects(ConfigurationType.IMPLEMENTATION)
        )
        val affectedAndroidTestProjects = findModifiedAndroidProjects(
            @Suppress("DEPRECATION")
            finder.findModifiedProjectsWithoutDependencyToAnotherConfigurations(ConfigurationType.ANDROID_TESTS)
        )

        val affectedTestsByImpl = getAffectedTestsByChangedModules(
            changedModules = affectedImplProjects.toSet(),
            screenToModulePaths = bytecodeAnalyzeSummary.screenToModule
        )

        val affectedTestsByAndroidTest = getAffectedTestsByAndroidTest(
            affectedAndroidTestProjects = affectedAndroidTestProjects
        )

        val testsToRun: Set<Test> =
            affectedTestsByImpl + affectedTestsByAndroidTest + affectedByCodeChanges
        val skippedTests: Set<Test> = filteredTest - testsToRun

        return ImpactSummary(
            affectedPackages = ImpactSummary.AffectedPackages(
                implementation = affectedImplProjects.map { it.toString() }.toSet(),
                androidTestImplementation = affectedAndroidTestProjects.map { it.toString() }.toSet()
            ),
            affectedTests = ImpactSummary.AffectedTests(
                implementation = affectedTestsByImpl,
                androidTestImplementation = affectedTestsByAndroidTest,
                codeChanges = affectedByCodeChanges
            ),
            allTests = filteredTest,
            addedTests = addedTests,
            modifiedTests = modifiedTests,
            skippedTests = skippedTests,
            testsToRun = testsToRun
        )
    }

    private fun getAffectedTestsByAndroidTest(
        affectedAndroidTestProjects: List<AndroidProject>
    ): Set<Test> {

        val isTargetModuleAffected = affectedAndroidTestProjects.any { it.path == targetModule.path }

        val targetModuleHasModifiedAndroidTestDependency = targetModule.internalModule
            .getConfiguration(ConfigurationType.ANDROID_TESTS)
            .dependencies
            .any { it.isModified }

        return if (isTargetModuleAffected && targetModuleHasModifiedAndroidTestDependency) {
            filteredTest
        } else {
            getAffectedTestsByChangedModules(
                changedModules = affectedAndroidTestProjects.toSet(),
                screenToModulePaths = bytecodeAnalyzeSummary.screenToModule
            )
        }
    }

    private fun getAffectedTestsByChangedModules(
        changedModules: Set<AndroidProject>,
        screenToModulePaths: Collection<ScreenToModulePath>
    ): Set<Test> {

        return bytecodeAnalyzeSummary
            .testsByScreen
            .flatMap { (screen, tests) ->
                when (screen) {
                    // We couldn't determine screen to test relation, so we will run all of these tests
                    DIRTY_STUB -> tests
                    else -> {
                        val screenToModuleMaps = screenToModulePaths
                            .map { it.screenClass to it.modulePath }
                            .toMap()

                        when (val screensModule: Path? = screenToModuleMaps[screen]) {
                            null -> {
                                logger.info("Module not found for screen $screen")
                                tests
                            }
                            else -> when {
                                changedModules.map { it.path }.contains(screensModule.path) -> {
                                    logger.info(
                                        "Module $screensModule for screen $screen changed, will run its tests"
                                    )
                                    tests
                                }
                                else -> {
                                    logger.info(
                                        "Module $screensModule for screen $screen not changed, won't run it tests"
                                    )
                                    emptySet()
                                }
                            }
                        }
                    }
                }
            }.toSet()
    }

    private fun findModifiedAndroidProjects(projects: Set<ModifiedProject>) =
        projects
            .map { it.project }
            .filter { it.isAndroid() }
            .map { AndroidProject(it) }
}
