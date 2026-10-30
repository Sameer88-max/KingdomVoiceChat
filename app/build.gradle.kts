
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val zegoAppSign = providers.gradleProperty("ZEGO_APP_SIGN")
    .orElse(providers.environmentVariable("ZEGO_APP_SIGN"))
    .getOrElse("")

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

    implementation("im.zego:zego_uikit_prebuilt_live_audio_room_android:+")
}
