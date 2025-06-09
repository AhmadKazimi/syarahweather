// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
}

// Configure detekt
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/config/detekt.yml")
    baseline = file("$rootDir/config/baseline.xml")
    autoCorrect = true
}

// Create a task to format code using detekt
tasks.register("detektFormat") {
    dependsOn(tasks.detekt)
    group = "formatting"
    description = "Runs detekt with auto-correction enabled"
}

// Apply detekt to all subprojects
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${rootProject.libs.versions.detekt.get()}")
    }
}