
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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.takaapoo.weatherer"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.0"

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
        viewBinding = true
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
    implementation("androidx.compose.material:material-icons-core:1.7.7")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.appcompat)

    // AnchoredDraggable
    implementation(libs.androidx.foundation)

    // Dagger-Hilt
    implementation(libs.hilt.android.v249)
    implementation(project(":renderscript-toolkit"))
    implementation(libs.androidx.foundation.android)
    implementation(libs.core)
    implementation(libs.androidx.runner)
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
    implementation(libs.datastorePreferences)
//    implementation("androidx.datastore:datastore-preferences-core:1.0.0")

    //Room
    implementation(libs.bundles.room)
    // To use Kotlin annotation processing tool (kapt)
    ksp(libs.roomCompiler)

    //Local Date & Time
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    // Optionally, you can include the Compose utils library for Clustering, etc.
    implementation(libs.maps.compose.utils)
    // Optionally, you can include the widgets library for ScaleBar, etc.
    implementation(libs.maps.compose.widgets)

    // For managing permission request
    implementation(libs.fragmentKtx)
    implementation(libs.activityKtx)

    // Google Play Services for location
    implementation(libs.play.services.location)

    // AnimatedImageVector
    implementation(libs.composeAnimationGraphics)


    // Shared Element Animation
    implementation(libs.composeAnimation)

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

    // Adaptive Screen
    implementation(libs.composeAdaptive)
//    implementation("androidx.compose.material3:material3:1.3.1")
//    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
//    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.1.0-alpha08")
//    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.1.0-alpha08")

    // Immutable Collections
    implementation(libs.immutableCollections)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // For AppWidgets support
    implementation(libs.bundles.glance)

    // Play console Billing
    implementation(libs.billing)
    implementation(libs.billingKtx)

    // Lottie Animation
    implementation(libs.lottieCompose)

    // Used for Extracting Dynamic colors
//    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.10")

    // ViewBinding
    implementation(libs.androidx.ui.viewbinding)

    // Used for getPropertyBySerialName function in UsefulFunc.kt
    implementation(kotlin("reflect"))

    // Coil
    implementation(libs.bundles.coil)




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
    androidTestImplementation(libs.androidx.rules)
    kspAndroidTest(libs.hilt.android.compiler)
}

// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}