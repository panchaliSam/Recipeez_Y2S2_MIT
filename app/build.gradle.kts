plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.recipeez"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.recipeez"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Core Android libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.9.2")

    // Firebase BOM (Bill of Materials) - manages Firebase versions
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Firebase dependencies
    implementation("com.google.firebase:firebase-database-ktx")  // Firebase Realtime Database
    implementation("com.google.firebase:firebase-storage-ktx")   // Firebase Storage
    implementation("com.google.firebase:firebase-auth-ktx")      // Firebase Authentication
    implementation("com.google.firebase:firebase-analytics-ktx") // Firebase Analytics

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.15.1")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Update the annotation processor version to match Glide version
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

}
