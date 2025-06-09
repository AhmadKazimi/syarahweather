import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}
// Function to safely read a property from local.properties
fun getApiKeyFromLocalProperties(propertyName: String): String {
    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    return properties.getProperty(propertyName, "")
}
android {
    namespace = "com.kazimi.syarahweather"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kazimi.syarahweather"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        val apiKey = getApiKeyFromLocalProperties("API_KEY_OPEN_WERATHER")
        val googlePlacesApiKey = getApiKeyFromLocalProperties("GOOGLE_PLACES_API_KEY")
        // Make it available in BuildConfig
        buildConfigField("String", "API_KEY", apiKey)
        buildConfigField("String", "GOOGLE_PLACES_API_KEY", googlePlacesApiKey)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get() // Reference from TOML
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)

    // Jetpack Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    // Retrofit for Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson) // Or Moshi
    implementation(libs.okhttp.logging.interceptor) // For logging API requests

    // Coroutines & Flow
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Location
    implementation(libs.play.services.location)
    implementation(libs.play.services.places)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Material Components (for shape attributes)
    implementation(libs.material)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)
    // Mockito for mocking
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline) // For mocking final classes/methods
    // Turbine for testing Flow
    testImplementation(libs.turbine)
}