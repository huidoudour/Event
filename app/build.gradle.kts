plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "me.huidoudour.event"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "me.huidoudour.event"
        minSdk = 29
        targetSdk = 36
        versionCode = 11
        versionName = "0.11"

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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // MTDataFilesProvider
    debugImplementation("com.github.L-JINBIN:MTDataFilesProvider:v1.0.0") // Debug版本
    // implementation("com.github.L-JINBIN:MTDataFilesProvider:v1.0.0")  // Release版本
}