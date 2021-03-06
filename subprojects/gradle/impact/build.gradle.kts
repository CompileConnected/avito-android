plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":gradle:impact-shared"))

    implementation(gradleApi())
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:files"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:statsd-config"))

    implementation(Dependencies.antPattern)
    implementation(Dependencies.Gradle.kotlinPlugin)

    testImplementation(project(":gradle:impact-shared-test-fixtures"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:git-test-fixtures"))
    testImplementation(Dependencies.Test.mockitoKotlin)
}

gradlePlugin {
    plugins {
        create("impact") {
            id = "com.avito.android.impact"
            implementationClass = "com.avito.impact.plugin.ImpactAnalysisPlugin"
            displayName = "Impact analysis"
        }
    }
}
