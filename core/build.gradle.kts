import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "io.jitpack.nabto-webrtc-sdk-android"
            artifactId = "core"
            version = "0.0.1"

            // Delay configuration until after evaluation
            afterEvaluate {
                from(components["release"])
            }
        }
    }
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
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        aarMetadata {
            minCompileSdk = libs.versions.android.minSdk.get().toInt()
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val integrationTestServerUrl: String = props.getProperty("integrationTestServerUrl");

        buildConfigField("String", "INTEGRATION_TEST_SERVER_URL", "\"$integrationTestServerUrl\"")
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
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

    kotlinOptions {
        jvmTarget = libs.versions.android.kotlinJvmTarget.get()
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
    androidTestImplementation(libs.moshi)
    androidTestImplementation(libs.moshi.kotlin)
    androidTestImplementation(project(":generated_integration_test_openapi"))
}
