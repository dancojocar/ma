// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register<Exec>("runMaestro") {
    group = "verification"
    description = "Installs the app and runs Maestro flows"
    dependsOn(":app:installDebug")
    
    // Ensure maestro is in PATH or configure path manually if needed
    // This assumes 'maestro' is available in the shell environment
    commandLine("maestro", "test", "maestro/flow.yaml")
    
    // Ignore exit value so Gradle build doesn't fail hard if tests fail (optional, but usually good for reporting)
    // isIgnoreExitValue = true 
}
