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
        minSdk = 28
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 72
        versionName = "0.72"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 指定要包含的 ABI 架构（所有架构）
        ndk {
            // 包含所有支持的架构：armeabi-v7a, arm64-v8a, x86, x86_64
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    val useSignKey = rootProject.hasProperty("storeFile") &&
        rootProject.hasProperty("storePassword") &&
        rootProject.hasProperty("keyAlias") &&
        rootProject.hasProperty("keyPassword")
    val devSignKey = rootProject.hasProperty("dbgFilePath") &&
        rootProject.hasProperty("dbgPassword") &&
        rootProject.hasProperty("dbgKeyAlias") &&
        rootProject.hasProperty("dbgKeyPaswd")

    signingConfigs {
        if (useSignKey) {
            create("sign_key") {
                storeFile = file(rootProject.property("storeFile") as String)
                storePassword = rootProject.property("storePassword") as String
                keyAlias = rootProject.property("keyAlias") as String
                keyPassword = rootProject.property("keyPassword") as String
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
        if (devSignKey) {
            create("debug_key") {
                storeFile = file(rootProject.property("dbgFilePath") as String)
                storePassword = rootProject.property("dbgPassword") as String
                keyAlias = rootProject.property("dbgKeyAlias") as String
                keyPassword = rootProject.property("dbgKeyPaswd") as String
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = if (devSignKey) {
                signingConfigs.getByName("debug_key")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (useSignKey) {
                signingConfigs.getByName("sign_key")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    // 配置 NDK 版本
    ndkVersion = "27.0.12077973" // 使用与 AGP 9.0.1 兼容的 NDK 版本
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    
    // Fragment
    implementation(libs.fragment.ktx)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // MTDataFilesProvider
    implementation(libs.mt.data.files.provider)

    // SQLite Android - 增强版 SQLite 库
    implementation(libs.sqlite.android)
    // 本地依赖，仅用于调试（仅在文件存在时添加）
    val localSqliteFile = file("libs/android.aar")
    if (localSqliteFile.exists()) {
        debugImplementation(files(localSqliteFile))
    }

    // debugImplementation("io.reactivex.rxjava3:rxjava:3.1.5")
    // debugImplementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    debugImplementation(libs.rxjava)
    debugImplementation(libs.rxandroid)
}
