import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.kotlindemos"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.kotlindemos"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        
        val properties = Properties()
        val localProperties = rootProject.file("local.properties")
        if (localProperties.exists()) {
            properties.load(localProperties.inputStream())
        }
        val mapsApiKey = properties.getProperty("MAPS_API_KEY", "")
        manifestPlaceholders["mapsApiKey"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("gms") {
            dimension = "version"
            applicationIdSuffix = ".gms"
            versionNameSuffix = "-gms"
        }
        create("v3") {
            dimension = "version"
            applicationIdSuffix = ".v3"
            versionNameSuffix = "-v3"
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
        viewBinding = true // Keep viewBinding for other activities
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.multidex)
    
    // Flavor specific dependencies need to be handled carefully in KTS
    // "gmsImplementation" -> "gmsImplementation" configuration
    // But we need to use string based configuration access or create configurations?
    // KTS supports `gmsImplementation(...)` via dynamic lookup or `add("gmsImplementation", ...)`
    
    add("gmsImplementation", libs.androidx.lifecycle.runtime.ktx)
    add("gmsImplementation", libs.maps.ktx)
    
    implementation(libs.easypermissions)
    
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register<Copy>("generateV3") {
    group = "V3 Beta"
    description = "Copies source code from GMS to V3 BETA."
    from("src/gms/java")
    into("src/v3/java")
    filter { line -> line.replace("com.google.android.gms.maps", "com.google.android.libraries.maps") }
}

tasks.register<Copy>("generateV3Layout") {
    group = "V3 Beta"
    description = "Copies layout files from GMS to V3 BETA."
    from("src/gms/res/layout")
    into("src/v3/res/layout")
    filter { line -> line.replace("com.google.android.gms.maps", "com.google.android.libraries.maps") }
}
