plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.example.android.treasureHunt"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.example.android.treasureHunt"
    minSdk = 33
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("com.google.android.gms:play-services-maps:18.2.0")
  implementation("com.google.android.gms:play-services-location:21.0.1")
  implementation("com.android.support.constraint:constraint-layout:2.0.4")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.databinding:databinding-runtime:8.2.0")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test:runner:1.5.2")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  implementation("com.google.android.material:material:1.11.0")
  implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")
}
