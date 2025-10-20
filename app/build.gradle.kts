plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.iptfinal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.iptfinal"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }



    buildFeatures {
        viewBinding = true
    }



}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
//    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


//    implementation("io.github.tutorialsandroid:kalertdialog:20.4.8")
//    implementation("com.github.TutorialsAndroid:progressx:v6.0.19")
    implementation("com.github.TutorialsAndroid:KAlertDialog:v7.0.19")

//    implementation("com.google.firebase:firebase-database-ktx")

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    implementation(libs.firebase.database)

    //Contact Number Nation Chooser
    implementation("com.hbb20:ccp:2.7.3")


//    implementation("com.google.android.gms:play-services-auth:21.4.0")

    implementation("com.google.android.material:material:1.9.0")
    //scanner for qr
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-view:1.3.4")




    implementation(libs.google.auth)
    implementation(libs.glide)
    kapt(libs.glide.compiler)

//
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
//
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
//
//    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
//
//
//    implementation("com.google.firebase:firebase-auth")
//    implementation("com.google.firebase:firebase-database")
}