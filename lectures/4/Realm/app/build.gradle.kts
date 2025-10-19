plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("io.realm.kotlin")
  id("org.jetbrains.kotlin.plugin.compose")
  id("kotlin-parcelize")
}

android {
  namespace = "com.example.realm"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.realm"
    minSdk = 33
    targetSdk = 36
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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.13"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {

  implementation("androidx.core:core-ktx:1.17.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
  implementation("androidx.activity:activity-compose:1.11.0")
  implementation(platform("androidx.compose:compose-bom:2025.10.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended:1.7.8")
  implementation("androidx.navigation:navigation-compose:2.9.5")

  implementation("io.realm.kotlin:library-base:3.0.0")
  implementation("io.realm.kotlin:library-sync:2.3.0")  // If using Device Sync

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2") // If using coroutines with the SDK

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.3.0")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
  androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
}