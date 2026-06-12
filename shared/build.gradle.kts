import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.itinera.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/secrets/kotlin"))   // ⬅ ADD: generated Secrets.kt
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.datetime.wheel.picker)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.itinera.app.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

// ⬅ reads unsplashAccessKey from local.properties and generates a Kotlin constant
val generateSecrets by tasks.registering {
    val propsFile = rootProject.file("local.properties")
    val outputDir = layout.buildDirectory.dir("generated/secrets/kotlin")
    outputs.dir(outputDir)

    doLast {
        val props = Properties().apply {
            if (propsFile.exists()) propsFile.inputStream().use { load(it) }
        }
        val key = props.getProperty("unsplashAccessKey") ?: "YOUR_UNSPLASH_ACCESS_KEY"
        val dir = outputDir.get().asFile
        dir.mkdirs()
        File(dir, "Secrets.kt").writeText(
            """
            package com.itinera.app.config

            internal object Secrets {
                const val UNSPLASH_ACCESS_KEY: String = "$key"
            }
            """.trimIndent()
        )
    }
}

// ⬅ make every Kotlin compilation run the generator first
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateSecrets)
}