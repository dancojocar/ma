plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.gms.google-services")
  id("com.google.firebase.crashlytics")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  namespace = "com.example.dan.crashlyticsdemo"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.example.dan.crashlyticsdemo"
    minSdk = 33
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  buildFeatures {
    viewBinding = true
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.6"
  }
}

dependencies {
  val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
  implementation(composeBom)
  androidTestImplementation(composeBom)
  implementation("androidx.compose.material3:material3")
  implementation("androidx.activity:activity-compose:1.9.3")
//  implementation("androidx.compose.ui:ui-tooling-preview")
//  debugImplementation("androidx.compose.ui:ui-tooling")
//  implementation("androidx.compose.ui:ui")
//  implementation("androidx.core:core-ktx:1.12.0")
//  implementation("androidx.appcompat:appcompat:1.6.1")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  // Import the BoM for the Firebase platform
  implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
  // Declare the dependencies for the Crashlytics and Analytics libraries
  // When using the BoM, you don't specify versions in Firebase library dependencies
  implementation("com.google.firebase:firebase-crashlytics")
  implementation("com.google.firebase:firebase-analytics")
}