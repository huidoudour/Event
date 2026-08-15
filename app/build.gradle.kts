import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// ── Git 版本控制 ──
val baseVersionCode = 10
val baseVersionName = "0.9-beta03"
val backVersionCode = 90

fun Project.gitCommitCount(): Int = try {
    providers.exec { commandLine("git", "rev-list", "--count", "HEAD") }
        .standardOutput.asText.get().trim().toInt()
} catch (_: Exception) { backVersionCode }

fun Project.gitHash(): String = try {
    providers.exec { commandLine("git", "rev-parse", "--short=7", "HEAD") }
        .standardOutput.asText.get().trim()
} catch (_: Exception) {
    SimpleDateFormat("MMddHHmm").format(Date())
}

// 统一计算版本信息，供 defaultConfig 与构建结束打印共用
val appVersionCode = baseVersionCode + gitCommitCount()
val appVersionName = "${baseVersionName}.${gitCommitCount()}.${gitHash()}"

// 构建结束后打印版本号（assemble/bundle 任务完成时输出）
tasks.matching { it.name.startsWith("assemble") || it.name.startsWith("bundle") }.configureEach {
    doLast {
        println(">>> Event-[$name]: $appVersionName($appVersionCode) <<<")
    }
}

android {
    namespace = "me.huidoudour.event"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "me.huidoudour.event"
        minSdk = 28
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 指定要包含的 ABI 架构（所有架构）
        ndk {
            // 包含所有支持的架构：armeabi-v7a, arm64-v8a, x86, x86_64
            abiFilters += listOf("arm64-v8a" , "x86_64")
        }
    }

    val useSignKey = rootProject.hasProperty("storeFile") &&
        rootProject.hasProperty("storePassword") &&
        rootProject.hasProperty("keyAlias") &&
        rootProject.hasProperty("keyPassword")

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
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = if (useSignKey) {
                signingConfigs.getByName("sign_key")
            } else {
                signingConfigs.getByName("debug")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (useSignKey) {
                signingConfigs.getByName("sign_key")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        compose = true
    }
    
    // 配置 NDK 版本
    ndkVersion = "27.0.12077973" // 使用与 AGP 9.0.1 兼容的 NDK 版本
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.room.runtime)
    //noinspection KspUsageInsteadOfKapt
    ksp("androidx.room:room-compiler:${libs.versions.room.get()}")
    
    // Fragment (仅兼容过渡期使用)
    implementation(libs.fragment.ktx)
    
    // Compose
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime.livedata)
    debugImplementation(libs.compose.ui.tooling)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // MTDataFilesProvider
    implementation(libs.mt.data.files.provider)

    // SQLite Android - 增强版 SQLite 库
    implementation(libs.sqlite.android)

    // MaterialKolor - 动态取色
    implementation(libs.materialkolor)

    // 本地依赖，仅用于调试（仅在文件存在时添加）
    val localSqliteFile = file("libs/android.aar")
    if (localSqliteFile.exists()) {
        debugImplementation(files(localSqliteFile))
    }

    debugImplementation(libs.rxjava)
    debugImplementation(libs.rxandroid)
}
