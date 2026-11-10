
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties
import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


//@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
//    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler/reports")
    metricsDestination = layout.buildDirectory.dir("compose_compiler/metrics")
    stabilityConfigurationFiles = listOf(rootProject.layout.projectDirectory.file("stability_config.conf"))
}
/*tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                        layout.buildDirectory.dir("compose-reports").get().asFile.absolutePath,
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                        layout.buildDirectory.dir("compose-metrics").get().asFile.absolutePath,
                "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${rootProject.projectDir}/stability_config.conf"
            )
        )
    }
}*/

/*extensions.configure<ApplicationExtension>*/android {

    namespace = "com.takaapoo.weatherer"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.takaapoo.weatherer"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "0.0"

        testInstrumentationRunner = "com.takaapoo.weatherer.CustomTestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Load Google Maps API key
        val secureProps = Properties()
        val secretsPropsFile = rootProject.file("secrets.properties")
        if (secretsPropsFile.exists()) secureProps.load(FileInputStream(secretsPropsFile))
        resValue("string", "maps_api_key", secureProps.getProperty("MAPS_API_KEY") ?: "")
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
//android {
//    buildFeatures { compose = true }
//}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    testImplementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.review.ktx)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation("androidx.compose.material:material-icons-core:1.7.7")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.appcompat)
    implementation(libs.androidx.locationbutton.compose)

    // AnchoredDraggable
    implementation(libs.androidx.foundation)

    // Dagger-Hilt
    implementation(libs.hilt.android.v249)
//    implementation(project(":renderscript-toolkit"))
    implementation(libs.androidx.foundation.android)
    implementation(libs.core)
//    implementation(libs.androidx.runner)
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
    implementation(libs.kotlin.reflect)
//    implementation(kotlin("reflect"))

    // Coil
    implementation(libs.bundles.coil)

    // Navigation3
    implementation(libs.bundles.navigation3)
    implementation(libs.kotlinx.serialization.core)

    // Jetpack Webkit
    implementation(libs.jetpack.webkit)



    // Rebugger
    debugImplementation(libs.rebugger)

    // Liquid Effect
    implementation(libs.backdrop)

    // Credential Manager
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)

    // Kotlin Coroutine for play service location finder
    implementation(libs.kotlinx.coroutines.play.services)



    testImplementation(libs.junit)
    testImplementation(libs.bundles.coroutines)
    testImplementation(libs.ui.test.junit4)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
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

    //Robolectric
    testImplementation(libs.robolectric)

    //Mockito
    testImplementation(libs.bundles.mockito)

    //Turbine
    testImplementation(libs.turbine)
}

// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}