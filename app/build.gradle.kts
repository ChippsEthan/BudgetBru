plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.budgetbruprog7313"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.budgetbruprog7313"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"  // Updated to latest stable
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose BOM (manages versions for most Compose libraries)
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))  // Updated BOM
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Material Icons Extended - Version is managed by BOM, no version needed!
    implementation("androidx.compose.material:material-icons-extended")

    // Remove this conflicting line:
    // implementation("androidx.compose.material:material-icons-core:1.7.8")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.0")  // Updated

    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Lifecycle & Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.0")  // Updated
    implementation("androidx.camera:camera-camera2:1.4.0")  // Updated
    implementation("androidx.camera:camera-lifecycle:1.4.0")  // Updated
    implementation("androidx.camera:camera-view:1.4.0")  // Updated
}

kapt {
    correctErrorTypes = true
    useBuildCache = false
}