import java.io.FileInputStream
import java.util.Properties

//import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

//@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
//    kotlin("kapt")
//    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.takaapoo.weatherer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.takaapoo.weatherer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "com.takaapoo.weatherer.CustomTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Code for hiding Google maps API key
        val secureProps = Properties()
        val secretsPropsFile = rootProject.file("secrets.properties")
        if (secretsPropsFile.exists()) secureProps.load(FileInputStream(secretsPropsFile))
        resValue("string", "maps_api_key", (secureProps.getProperty("MAPS_API_KEY") ?: ""))

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
    kotlinOptions {
        jvmTarget = "19"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.appcompat)

    // AnchoredDraggable
    implementation(libs.androidx.foundation)

    // Dagger-Hilt
    implementation(libs.hilt.android.v249)
    implementation(project(":renderscript-toolkit"))
    implementation(libs.androidx.foundation.android)
    implementation(libs.core)
    testImplementation("junit:junit:4.12")
    testImplementation("junit:junit:4.12")
    testImplementation("junit:junit:4.12")
    ksp(libs.hilt.compiler)

    // Kotlin serialization
    implementation(libs.kotlinx.serialization.json)

    // Retrofit
    implementation(libs.retrofit)
    // Retrofit with Kotlin serialization Converter
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.converter.scalars)
    implementation(libs.okhttp)

    // Extended material icon
//    implementation(libs.androidx.material.icons.extended)

    // lifecycle aware stateFlow collection
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation Component
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    //Data Store
    implementation("androidx.datastore:datastore-preferences:1.1.1")
//    implementation("androidx.datastore:datastore-preferences-core:1.0.0")

    //Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    // To use Kotlin annotation processing tool (kapt)
    ksp("androidx.room:room-compiler:2.6.1")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")

    //Local Date & Time
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    // Optionally, you can include the Compose utils library for Clustering, etc.
    implementation(libs.maps.compose.utils)
    // Optionally, you can include the widgets library for ScaleBar, etc.
    implementation(libs.maps.compose.widgets)

    // For managing permission request
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Google Play Services for location
    implementation(libs.play.services.location)

    // AnimatedImageVector
    implementation("androidx.compose.animation:animation-graphics:1.6.3")


    // Shared Element Animation
    implementation("androidx.compose.animation:animation:1.7.0-beta01")

    // Material
//    implementation("androidx.compose.material:material:1.5.3")

    // Constraint Layout
    implementation (libs.androidx.constraintlayout.compose)

    // Work Manager
    implementation(libs.androidx.work.runtime.ktx)

    // Hilt for WorkManager
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //Reorderable List
    implementation(libs.reorderable)




    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    //Truth
    testImplementation(libs.truth)
    androidTestImplementation(libs.truth)

    //Work Manager
    androidTestImplementation(libs.androidx.work.testing)

    //Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
}

// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}