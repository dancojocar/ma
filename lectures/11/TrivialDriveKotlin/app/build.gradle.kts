plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ro.ubbcluj.cs.ds"
    compileSdk = 35

    defaultConfig {
        applicationId = "ro.ubbcluj.cs.ds"
        minSdk = 33
        targetSdk = 35
        versionCode = 20241213
        versionName = "2024.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = "android_ks"
            keyPassword = "ks123456"
            storeFile = file("/Users/dan/keyStore/android-release.keystore")
            storePassword = "ks123456"
        }
        create("release") {
            keyAlias = "android_ks"
            keyPassword = "ks123456"
            storeFile = file("/Users/dan/keyStore/android-release.keystore")
            storePassword = "ks123456"
        }

    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
//        compose = true
    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.4.3"
//    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("com.android.billingclient:billing-ktx:7.1.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.databinding:databinding-runtime:8.7.3")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0")
    implementation("android.arch.navigation:navigation-ui-ktx:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
}
