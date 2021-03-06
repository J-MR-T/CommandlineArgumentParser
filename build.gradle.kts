import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    `maven-publish`
}

group = "com.github.J-MR-T"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.kotest:kotest-runner-junit5-jvm:4.6.0")
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation("com.github.jitpack:gradle-simple:1.0")
}



tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}