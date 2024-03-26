plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

group = "org.aakotlin"
version = "0.1.3"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")
    implementation("org.web3j:core:4.10.3")

    // Tests
    testImplementation("org.testng:testng:6.9.6")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
