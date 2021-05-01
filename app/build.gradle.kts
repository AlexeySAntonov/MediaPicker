plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.0")
    defaultConfig {
        applicationId = "com.aleksejantonov.mediapicker"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

object Versions {
    const val kotlin = "1.4.32"
    const val adapterDelegatesVersion = "3.0.1"
    const val glideVersion = "4.11.0"
    const val moxyVersion = "1.5.3"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.3.0")

    implementation("com.hannesdorfmann:adapterdelegates3:${Versions.adapterDelegatesVersion}")

    implementation("com.github.bumptech.glide:glide:${Versions.glideVersion}")
    kapt("com.github.bumptech.glide:compiler:${Versions.glideVersion}")

    implementation("com.jakewharton.timber:timber:4.7.1")
}
