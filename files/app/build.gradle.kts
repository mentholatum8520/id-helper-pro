plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.idhelper.pro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.idhelper.pro"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // 介面與圖片處理
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("io.coil-kt:coil-compose:2.4.0")
    
    // Google ML Kit 文字辨識 (本地端，不需 API Key)
    implementation("com.google.mlkit:text-recognition:16.0.0")
    
    // 網路請求與網頁解析
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.jsoup:jsoup:1.16.1")

    // Compose & AndroidX 基礎（範例）
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
}