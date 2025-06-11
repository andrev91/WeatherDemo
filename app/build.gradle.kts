import java.util.Properties

fun getApiKey(propertyKey: String): String {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties") // Access file from root project
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    // Return the property value or an empty string/placeholder if not found
    return properties.getProperty(propertyKey, "")
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

android {
    namespace = "com.example.adventure"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.adventure"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.adventure.TestRunner"

        val accuApiKey = getApiKey("ACCUWEATHER_API_KEY")
        // Add quotes around the string value for BuildConfig field
        buildConfigField("String", "ACCUWEATHER_API_KEY", "\"$accuApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        dataBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
//    testOptions {
//        unitTests.all { test-> test.jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005") }
//    }
}

dependencies {
    // Core library
    implementation("androidx.core:core-ktx:1.15.0")

    // Hilt/Dagger
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation(libs.androidx.work.runtime.ktx)
    kapt("com.google.dagger:hilt-compiler:2.56.2")

    // Lifecycle libraries
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    // Activity Compose library
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.03.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.compose.ui:ui-graphics")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.ui.tooling)
    implementation("androidx.compose.material3:material3")

    // Serialization libraries
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.hilt:hilt-work:1.2.0") // Or latest
    kapt("androidx.hilt:hilt-compiler:1.2.0") // Hilt Work compiler (often needed)

    // Testing libraries
    kaptTest(libs.hilt.compiler.v2561)
    kaptAndroidTest(libs.dagger.hilt.compiler)
    testImplementation(libs.hilt.android.testing.v2511)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin) // For Kotlin-specific Mockito helpers
    testImplementation(libs.androidx.core.testing) // For InstantTaskExecutorRule
    testImplementation(libs.kotlinx.coroutines.test) // For TestCoroutineDispatcher
    testImplementation(libs.turbine) // For testing Kotlin Flows
    testImplementation(libs.guava)
    testImplementation(libs.androidx.work.testing.v2101) // For testing WorkManager
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.03.01"))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing.v2511)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.work.testing)

    //API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.2.0")
    implementation("com.google.code.gson:gson:2.12.1")

}