// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val composeVersion by extra("1.6.0")
    val kotlinVersion by extra("1.9.10")
    val web3jVersion by extra("4.10.3")
    val coroutinesVersion by extra("1.7.3")

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}
