plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
}


group = "land.tbp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.kotlinx.serialization.json)

    implementation(libs.hoplite)


    implementation(libs.logback.classic)
    testImplementation(libs.kotlin.test.junit)

}

tasks.test {
    useJUnitPlatform()
}
