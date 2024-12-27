plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.dld.bluewaves"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dld.bluewaves"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
    configurations.all {
        resolutionStrategy {
            force("com.google.firebase:firebase-firestore:25.1.1")
            force("com.google.protobuf:protobuf-javalite:3.25.1")
            force("com.google.api.grpc:proto-google-common-protos:2.9.6")
            force("com.google.firebase:protolite-well-known-types:18.0.0")
            force("androidx.core:core:1.15.0")
            force("androidx.media:media:1.0.0")
            exclude(group = "com.google.protobuf", module = "protobuf-java")
            exclude(group = "com.android.support", module = "support-compat")
            exclude(group = "com.android.support", module = "support-media-compat")

        }

        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
        exclude(group = "com.google.cloud", module = "proto-google-cloud-firestore-bundle-v1")
    }

}


dependencies {

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.auth)
    implementation(libs.fragment)
    implementation(libs.viewpager2)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Firebase
    implementation(libs.firebase.playintegrity) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.firebase.ui.firestore) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.firebase.storage) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.firebase.firestore) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
        exclude(group = "com.google.api.grpc", module = "proto-google-cloud-firestore-v1")
        exclude(group = "com.google.cloud", module = "proto-google-cloud-firestore-bundle-v1")
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")

    }
    implementation(libs.firebase.storage.ktx) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.firebase.database) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.firebase.auth) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation(libs.firebase.messaging) {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }

    implementation("com.google.firebase:protolite-well-known-types:18.0.0") {
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
    }

    // Protobuf
    implementation("com.google.protobuf:protobuf-javalite:3.25.1") {
        version { strictly("3.25.1") }
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }


    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //glide
    implementation(libs.glide)

    //Photo Viewer
    implementation(libs.photoview)

    // Tedimagepicker
    implementation(libs.tedimagepicker)

    //coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //image picker
    implementation(libs.imagepicker)

    //okhttp3
    implementation(libs.okhttp3)

    //oAuth2
    implementation(libs.oauth2)

    //flexbox
    implementation(libs.flexbox)

    //graphview
    implementation(libs.graphview)
}