plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.funktionaleTry)
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:artifactory-app-backup"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:files"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:impact"))
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:instrumentation-tests"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:lint-report"))
    implementation(project(":gradle:prosector"))
    implementation(project(":gradle:qapps"))
    implementation(project(":gradle:signer"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:test-summary"))
    implementation(project(":gradle:tms"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(project(":gradle:upload-to-googleplay"))

    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
    testImplementation(project(":gradle:artifactory-app-backup-test-fixtures"))
    testImplementation(project(":gradle:impact-shared-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("cicd") {
            id = "com.avito.android.cd" // todo rename to ci-steps
            implementationClass = "com.avito.ci.CiStepsPlugin"
            displayName = "CI/CD"
        }
    }
}
