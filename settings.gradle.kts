pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "1.9.10-1.0.13"
        kotlin("jvm") version "1.9.10"
        id("com.android.library") version "8.2.0-rc02"
        id("org.jetbrains.kotlin.android") version "1.9.10"
        id("com.android.application") version "8.8.1"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

rootProject.name = "aa-kotlin"
include(":core")
include(":alchemy")
include(":example")
include(":coinbase")
