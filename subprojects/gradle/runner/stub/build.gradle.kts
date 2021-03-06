plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

extra["artifact-id"] = "runner-stub"

dependencies {
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:runner:service"))
    implementation(project(":gradle:runner:shared"))
}
