import java.util.Properties // ⚠️ 必須加這行，不然會報錯 "Unresolved reference: util"

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.fitness"
    compileSdk = 36

    defaultConfig {
        applicationId = "tw.edu.fju.myfitnessapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ★★★ 關鍵修正：讀取 local.properties 的正確寫法 ★★★
        val localProperties = Properties()
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localProperties.load(localFile.inputStream())
        }

        // 這裡會去抓 local.properties 裡面的 geminiApiKey
        // 如果讀不到，就會是空字串，導致連不上 Gemini
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY") ?: ""}\"")

        // URL 比較不敏感，如果放在 gradle.properties 可以用 project.findProperty，或者統一都放 local.properties
        buildConfigField("String", "GEMINI_API_URL", "\"${project.findProperty("geminiApiUrl") ?: ""}\"")
        buildConfigField("String", "EDAMAM_APP_ID", "\"${localProperties.getProperty("EDAMAM_APP_ID") ?: ""}\"")
        buildConfigField("String", "EDAMAM_APP_KEY", "\"${localProperties.getProperty("EDAMAM_APP_KEY") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("release/my-release-key")
            storePassword = project.findProperty("storePassword")?.toString() ?: "defaultPassword"
            keyAlias = project.findProperty("keyAlias")?.toString() ?: "defaultAlias"
            keyPassword = project.findProperty("keyPassword")?.toString() ?: "defaultPassword"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            // isZipAlignEnabled = true // 這行已過時，新版 Android Studio 會自動處理，建議刪除避免警告
            signingConfig = signingConfigs.getByName("release")
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
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=com.google.android.xr.compose.subspace.ExperimentalSubspaceApi"
        )
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    // Google Play Services Ads
    implementation("com.google.android.gms:play-services-ads:23.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Auth & AI
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Networking & Utils
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:32.0.1-android")
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("androidx.concurrent:concurrent-futures:1.2.0")

    // RevenueCat (保留這行正確的，刪除原本最下面那行重複的 9.19.1)
    implementation("com.revenuecat.purchases:purchases:7.3.1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    // AndroidX & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ML Kit & Camera
    implementation("com.google.mlkit:digital-ink-recognition:19.0.0")
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")

    // Other UI
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.makeramen:roundedimageview:2.3.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}