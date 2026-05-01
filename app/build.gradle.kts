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
        targetSdk = 36
        versionCode = 39
        versionName = "0.39"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 指定要包含的 ABI 架构（所有架构）
        ndk {
            // 包含所有支持的架构：armeabi-v7a, arm64-v8a, x86, x86_64
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
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
    debugImplementation(libs.mt.data.files.provider)
    implementation(libs.mt.data.files.provider)

    // SQLite Android - 增强版 SQLite 库
    debugImplementation(libs.sqlite.android)
    implementation(libs.sqlite.android)
    // 本地依赖，仅用于调试（仅在文件存在时添加）
    val localSqliteFile = file("libs/android.aar")
    if (localSqliteFile.exists()) {
        debugImplementation(files(localSqliteFile))
    }
}
