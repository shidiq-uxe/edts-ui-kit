plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)

    id("maven-publish")
}

group = "id.co.edtslib.uikit"
version = "0.17.0"

afterEvaluate {

    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "id.co.edtslib.uikit"
                artifactId = "uikit"
                version = "0.17.0"

                pom {
                    name.set("EDTS UI Kit")
                    description.set("A Library with sets of UI components for Android")
                    url.set("https://github.com/shidiq-uxe/edts-ui-kit")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("shidiq-uxe")
                            name.set("Shidiq Bagus Ardi Prastian")
                            email.set("shidiq.bagus@sg-edts.com")
                        }
                    }
                }

            }
        }
    }
}

android {
    namespace = "id.co.edtslib.uikit"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures.viewBinding = true
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.swipeRefresh)
    implementation(libs.shimmer)
    implementation(libs.lottie)
    implementation(libs.blurView)

    implementation(libs.material)
    implementation(libs.glide)
    ksp(libs.glide.compiler)

    implementation(libs.spotlight)
}