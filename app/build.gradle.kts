plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mit_plateform"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mit_plateform"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Scable size unit
    implementation ("com.intuit.sdp:sdp-android:1.0.6")
    implementation ("com.intuit.ssp:ssp-android:1.0.6")


    //Rounded ImageView
    implementation ("com.makeramen:roundedimageview:2.3.0")


    //firebase
    implementation ("com.google.firebase:firebase-messaging:24.1.0")
    implementation ("com.google.firebase:firebase-firestore:25.1.1")
    implementation ("com.google.firebase:firebase-auth:22.3.1")
    implementation ("com.firebaseui:firebase-ui-firestore:8.0.2")
    // Google Sign-In
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    // Facebook Sign-In
    implementation ("com.facebook.android:facebook-android-sdk:16.2.0")

    //MultiDex
    implementation ("androidx.multidex:multidex:2.0.1")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    implementation ("androidx.activity:activity:1.7.0")
    implementation ("androidx.fragment:fragment:1.6.0")

    implementation ("androidx.core:core:1.10.0")
}

