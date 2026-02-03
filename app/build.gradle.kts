plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.sheshield"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sheshield"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {

    /* -------------------- Core AndroidX -------------------- */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    /* -------------------- Jetpack Compose -------------------- */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-extended")

    /* -------------------- Navigation -------------------- */
    implementation(libs.androidx.navigation.compose)

    /* -------------------- 🔥 Firebase (ONE BOM ONLY) -------------------- */
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")

    /* -------------------- Location & Maps -------------------- */
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.maps.compose) // Google Maps Compose
    implementation(libs.play.services.maps) // Google Maps SDK

    /* -------------------- WorkManager -------------------- */
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    /* -------------------- Retrofit -------------------- */
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.foundation)

    /* -------------------- Testing -------------------- */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    val cameraxVersion = "1.3.1" // Use the latest stable version

    // CameraX core library using the camera2 implementation
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    // CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    // CameraX View class (PreviewView)
    implementation("androidx.camera:camera-view:$cameraxVersion")
    // CameraX Extensions (optional but good for effects)
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Also ensure ML Kit is here for your face detection
    implementation("com.google.mlkit:face-detection:16.1.6")

    // Firebase Vertex AI for Gemini 3 Flash
    implementation("com.google.firebase:firebase-vertexai:16.0.0")

    // Ensure you also have the Firebase BoM (Bill of Materials)
    // to manage versions automatically
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
}