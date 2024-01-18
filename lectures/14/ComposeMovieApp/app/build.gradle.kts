plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.dagger.hilt.android")
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.example.composemovieapp"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.example.composemovieapp"
    minSdk = 33
    targetSdk = 34
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
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
  implementation("androidx.activity:activity-compose:1.8.2")
  val platform = platform("androidx.compose:compose-bom:2023.08.00")
  implementation(platform)

  implementation("androidx.compose.runtime:runtime")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.foundation:foundation-layout")
  implementation("androidx.compose.material:material")
  implementation("androidx.compose.runtime:runtime-livedata")
  implementation("androidx.compose.ui:ui-tooling")
  implementation("androidx.navigation:navigation-compose:2.7.6")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform)
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  // Hilt
  implementation("com.google.dagger:hilt-android:2.50")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0-alpha01")
  ksp("com.google.dagger:dagger-compiler:2.50") // Dagger compiler
  ksp("com.google.dagger:hilt-compiler:2.50")   // Hilt compiler

  //Retrofit Deps
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")
  //Retrofit get String response EASY
  implementation("com.squareup.retrofit2:converter-scalars:2.8.1")

  //Image lib
  implementation("io.coil-kt:coil-compose:2.4.0")

}