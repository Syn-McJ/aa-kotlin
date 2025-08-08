plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.aakotlin.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.aakotlin.example"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/DISCLAIMER"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/INDEX*"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/FastDoubleParser-*"
            excludes += "/META-INF/io.netty.*"
        }
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":alchemy"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    implementation("com.github.Web3Auth:web3auth-android-sdk:7.1.0")
    implementation("org.web3j:core:4.12.0")
    implementation("org.web3j:contracts:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}