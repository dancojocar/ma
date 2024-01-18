// This is the top-level Gradle settings for a Skip App project.
// It reads from the Skip.env file in the root of the project

pluginManagement {
    repositories {
        maven("https://maven.skip.tools")
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.skip.tools")
        mavenCentral()
        google()
    }
}

// Use the properties in the Skip.env file for configuration
val parentFolder = file("..")
val envFile = parentFolder.resolve("Skip.env")
if (!envFile.exists()) {
    throw GradleException("Skip.env file missing from ${parentFolder}")
}

val skipenv = loadSkipEnv(envFile)

// Use the shared ../.build/Android/ build folder as the gradle build output
val buildOutput = parentFolder.resolve(".build/Android/")
gradle.projectsLoaded {
    rootProject.allprojects {
        layout.buildDirectory.set(buildOutput.resolve(project.name))
    }
}

rootProject.name = prop(key = "ANDROID_PACKAGE_NAME")
val swiftProjectName = prop(key = "SKIP_PROJECT_NAME")
val swiftModuleName = prop(key = "PRODUCT_NAME")

// After the settings have been evaluated, resolve the Skip transpilation output folders
gradle.settingsEvaluated {
    addSkipModules()
}

// Parse .env file into a map of strings
fun loadSkipEnv(file: File): Map<String, String> {
    val envMap = mutableMapOf<String, String>()
    file.forEachLine { line ->
        if (line.isNotBlank() && line[0] != '#' && !line.startsWith("//")) {
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                envMap[key] = value
            }
        }
    }

    // Set system properties prefixed with SKIP_ for each key-value pair in the .env file
    // access with getProperty("SKIP_PRODUCT_BUNDLE_IDENTIFIER")
    envMap.forEach { (key, value) ->
        System.setProperty("SKIP_" + key, value)
    }

    return envMap
}


fun prop(key: String): String {
    val value = System.getProperty("SKIP_" + key, System.getenv(key))
    if (value == null) {
        throw GradleException("Required key ${key} is not set in environment or Skip.env")
    }
    return value
}

fun addSkipModules() {
    // When running from Xcode, the BUILT_PRODUCTS_DIR environment
    // variable will point to the project's DerivedData path, like:
    // ~/Library/Developer/Xcode/DerivedData/NAME-HASH/Build/Products/Debug-iphonesimulator
    //
    // When unset, we assume using the local SwiftPM .build folder, and
    // will invoke `swift build` to perform transpilation at the
    // beginning of the build
    var builtProductsDir = System.getenv("BUILT_PRODUCTS_DIR")

    // In order to build and debug in an IDE using locally-sourced modules,
    // temporarily override this setting with the known-local BUILT_PRODUCTS_DIR,
    // which can be found in Xcode's Reports Navigator Build log for the app in the
    // environment settings list of the "Run custom shell script 'Run skip gradle'" log entry
    // builtProductsDir = "/Users/marc/Library/Developer/Xcode/DerivedData/Skip-App-aqywrhrzhkbvfseiqgxuufbdwdft/Build/Products/Debug-iphonesimulator"

    var skipOutputs: File
    if (builtProductsDir != null) {
        // BUILT_PRODUCTS_DIR is set when building from Xcode, in which case we will use Xcode's DerivedData plugin output
        skipOutputs = file(builtProductsDir).resolve("../../../SourcePackages/plugins/")
    } else {
        // SPM output folder is a peer of the parent Package.swift
        skipOutputs = parentFolder.resolve(".build/plugins/outputs/")

        // not running from xcode, so fork swift to build locally to ../.build/
        exec {
            logger.log(LogLevel.LIFECYCLE, "Skip transpile swift to kotlin")
            commandLine("swift", "build")
        }
    }

    val outputExt = if (builtProductsDir != null) ".output" else ""
    val projectDir = skipOutputs
        .resolve(swiftProjectName + outputExt)
        .resolve(swiftModuleName)
        .resolve("skipstone")

    if (!projectDir.exists()) {
        // If the directory does not exist, fail the build
        throw GradleException("Skip output directory does not exist at: $projectDir")
    }

    var skipDependencies: List<String> = listOf()
    projectDir.listFiles()?.forEach { outputDir ->
        // for each child package, include it in this build
        if (outputDir.resolve("build.gradle.kts").exists()) {
            val moduleName = outputDir.name
            logger.log(LogLevel.LIFECYCLE, "Skip module :${moduleName} added to project: ${outputDir}")
            include(":${moduleName}")
            project(":${moduleName}").projectDir = outputDir
            skipDependencies += ":${moduleName}"
        }
    }

    // pass down the list of dynamic Skip dependencies to the app build
    // we would prefer to use the `exta` property for this, but it doesn't seem to be readable in app/build.gradle.kts
    System.setProperty("SKIP_DEPENDENCIES", skipDependencies.joinToString(separator = ":"))
    include(":app")
}
