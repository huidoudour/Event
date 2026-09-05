plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.huidou.event"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "dev.huidou.event"
        minSdk = 28
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 53
        versionName = "0.53"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // 包含所有支持的架构："armeabi-v7a", "arm64-v8a", "x86", "x86_64"
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf( "arm64-v8a" )
        }
    }

    val useSecKey = rootProject.hasProperty("SecKeyFile") &&
        rootProject.hasProperty("SecKeyPasswd") &&
        rootProject.hasProperty("SecAlias") &&
        rootProject.hasProperty("SecPassword")

    signingConfigs {
        if (useSecKey) {
            create("sec_sign_key") {
                storeFile = file(rootProject.property("SecKeyFile") as String)
                storePassword = rootProject.property("SecKeyPasswd") as String
                keyAlias = rootProject.property("SecAlias") as String
                keyPassword = rootProject.property("SecPassword") as String
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = false
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            signingConfig = if (useSecKey) {
                signingConfigs.getByName("sec_sign_key")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (useSecKey) {
                signingConfigs.getByName("sec_sign_key")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    ndkVersion = "27.0.12077973"
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

    // MTDataFilesProvider
    implementation(libs.mt.data.files.provider)

    // 本地依赖，仅用于调试（仅在文件存在时添加）
    val localSqliteFile = file("libs/android.aar")
    if (localSqliteFile.exists()) {
        debugImplementation(files(localSqliteFile))
    }
}
