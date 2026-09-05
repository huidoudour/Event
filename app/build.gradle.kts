import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val baseVersionCode = 10
val baseVersionName = "1.5"
val backVersionCode = 95

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

val appVersionCode = baseVersionCode + gitCommitCount()
val appVersionName = "${baseVersionName}.${gitCommitCount()}.${gitHash()}"

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
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // "armeabi-v7a","arm64-v8a","x86","x86_64"
            abiFilters += listOf( "arm64-v8a" , "x86_64" )
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
    ndkVersion = "27.0.12077973"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

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
    // 排除 JetBrains Compose Multiplatform 传递依赖，避免与官方 androidx Compose 类同名冲突导致 AbstractMethodError
    implementation(libs.materialkolor) {
        exclude(group = "org.jetbrains.compose.material3")
        exclude(group = "org.jetbrains.compose.foundation")
        exclude(group = "org.jetbrains.compose.ui")
        exclude(group = "org.jetbrains.compose.runtime")
        exclude(group = "org.jetbrains.compose.animation")
    }

    implementation(libs.compose.markdown)
    implementation(libs.rxjava)
    implementation(libs.rxkotlin)
    implementation(libs.rxandroid)

    val localSqliteFile = file("libs/android.aar")
    if (localSqliteFile.exists()) {
        debugImplementation(files(localSqliteFile))
    }

}
