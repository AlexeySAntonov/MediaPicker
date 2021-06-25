plugins {
  id("com.android.application")
  id("kotlin-android")
  id("kotlin-android-extensions")
  id("kotlin-kapt")
}

android {
  compileSdkVersion(30)
  buildToolsVersion("29.0.3")
  defaultConfig {
    applicationId = "com.aleksejantonov.mediapicker"
    minSdkVersion(21)
    targetSdkVersion(30)
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
  const val kotlin = "1.5.20"
  const val adapterDelegatesVersion = "4.3.0"
  const val glideVersion = "4.11.0"
  const val camerax_version = "1.1.0-alpha05"
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
  implementation("androidx.fragment:fragment-ktx:1.3.5")
  implementation("androidx.appcompat:appcompat:1.3.0")
  implementation("androidx.core:core-ktx:1.5.0")
  implementation("androidx.constraintlayout:constraintlayout:2.0.4")
  implementation("com.google.android.material:material:1.3.0")

  implementation("com.hannesdorfmann:adapterdelegates4:${Versions.adapterDelegatesVersion}")

  implementation("com.github.bumptech.glide:glide:${Versions.glideVersion}")
  kapt("com.github.bumptech.glide:compiler:${Versions.glideVersion}")

  implementation("com.jakewharton.timber:timber:4.7.1")

  // CameraX core library using camera2 implementation
  implementation("androidx.camera:camera-camera2:${Versions.camerax_version}")
  // CameraX Lifecycle Library
  implementation("androidx.camera:camera-lifecycle:${Versions.camerax_version}")
  // CameraX View class
  implementation("androidx.camera:camera-view:1.0.0-alpha25")
}
