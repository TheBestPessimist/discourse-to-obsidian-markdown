
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias (libs.plugins.kotlin.serialization)
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

    implementation(libs.kotlinx.coroutines)
    // implementation(libs.kotlinx.serialization)

    implementation(libs.base62)

    implementation(libs.hoplite)


    implementation(libs.logback.classic)
    testImplementation(libs.kotlin.test.junit)

}

tasks {
    wrapper {
        gradleVersion ="8.13"
        distributionType = Wrapper.DistributionType.BIN
    }

    test {
        useJUnitPlatform()
    }
}
