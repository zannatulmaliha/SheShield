plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.sheshield"
    compileSdk = 36 // Stable for early 2026

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
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

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

    /* -------------------- 🔥 Firebase (Updated 2026) -------------------- */
    // Using the latest 2026 BoM to manage versions automatically
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))

    // ✅ FIXED: Removed "-ktx" from all dependencies.
    // Kotlin extensions are now included in the main modules.
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-functions")

    // ✅ NEW: Modern AI Logic library for Gemini 3
    implementation("com.google.firebase:firebase-ai")

    /* -------------------- Location & Maps -------------------- */
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    /* -------------------- Camera & ML Kit -------------------- */
    val cameraxVersion = "1.4.1" // Updated to a more recent stable version
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")
    implementation("com.google.mlkit:face-detection:16.1.7")

    /* -------------------- Other Services -------------------- */
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    /* -------------------- Testing -------------------- */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
