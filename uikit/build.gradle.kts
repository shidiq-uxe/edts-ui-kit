import com.android.build.gradle.internal.utils.createPublishingInfoForLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.rootPublicationComponentName

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.kapt)

    id("maven-publish")
}

group = "id.co.edtslib.uikit"
version = "0.15.8"

afterEvaluate {

    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "id.co.edtslib.uikit"
                artifactId = "uikit"
                version = "0.15.8"

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
    compileSdk = 33

    defaultConfig {
        minSdk = 21

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
    configurations.all {
        resolutionStrategy {
            force( "org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
            force ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures.viewBinding = true
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.swipeRefresh)
    implementation(libs.shimmer)

    implementation(libs.material)
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    implementation(libs.spotlight)
}