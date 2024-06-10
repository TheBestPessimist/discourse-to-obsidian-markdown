private val ktor = "2.3.11"
private val hoplite = "2.7.5"



plugins {
    kotlin("jvm") version "2.0.0"
    id("java")
}

group = "land.tbp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.11")

    implementation("com.sksamuel.hoplite:hoplite-core:$hoplite")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
