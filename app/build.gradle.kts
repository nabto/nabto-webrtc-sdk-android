import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Load properties from options file or fallback to default
val optionsFile = rootProject.file("app/options.properties")
val defaultOptionsFile = rootProject.file("app/options.properties.default")
val props = Properties()

when {
    optionsFile.exists() -> optionsFile.inputStream().use { props.load(it) }
    defaultOptionsFile.exists() -> defaultOptionsFile.inputStream().use { props.load(it) }
    else -> throw GradleException("Missing options.properties and options.properties.default.")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PRODUCT_ID", "\"${props["product_id"]}\"")
        buildConfigField("String", "DEVICE_ID", "\"${props["device_id"]}\"")
        buildConfigField("String", "SHARED_SECRET", "\"${props["shared_secret"]}\"")
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

    kotlinOptions {
        jvmTarget = libs.versions.android.kotlinJvmTarget.get()
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(project(":core"))
    implementation(project(":util"))
    implementation(project(":util-org-webrtc"))
    implementation(libs.stream.webrtc.android)
    implementation(libs.stream.webrtc.android.ui)
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
