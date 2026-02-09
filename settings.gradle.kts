pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "2.1.0-1.0.29"
        kotlin("jvm") version "2.1.0"
        id("com.android.library") version "8.2.0-rc02"
        id("org.jetbrains.kotlin.android") version "2.1.0"
        id("com.android.application") version "8.12.2"
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
