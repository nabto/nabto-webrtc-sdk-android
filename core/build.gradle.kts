import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

val props = Properties()

rootProject.file("project.properties").takeIf { it.exists() }?.inputStream()?.use {
    props.load(it)
}

rootProject.file("project.properties.local").takeIf { it.exists() }?.inputStream()?.use {
    props.load(it) // override
}

android {
    namespace = "com.nabto.webrtc"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val integrationTestServerUrl: String = props.getProperty("integrationTestServerUrl");

        buildConfigField("String", "INTEGRATION_TEST_SERVER_URL", "\"$integrationTestServerUrl\"")
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
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.okhttp)
    implementation(libs.moshi)
    androidTestImplementation(libs.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(project(":generated_integration_test_openapi"))
}
