
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val localProps = java.util.Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}

val zegoAppSign = providers.gradleProperty("ZEGO_APP_SIGN").orNull
    ?: System.getenv("ZEGO_APP_SIGN")
    ?: localProps.getProperty("ZEGO_APP_SIGN")
    ?: ""

android {
    namespace = "com.kingdomvoicechat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kingdomvoicechat"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "ZEGO_APP_SIGN",
            "\"${zegoAppSign.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // ZEGOCLOUD Live Audio Room
    implementation("im.zego:zego_uikit_prebuilt_live_audio_room_android:+")
}
