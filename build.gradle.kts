/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    plugin(Deps.Plugins.detekt) apply false
    plugin(Deps.Plugins.dokka) apply false
}

buildscript {
    repositories {
        maven { url = uri("https://dl.bintray.com/icerockdev/plugins") }
    }
    dependencies {
        plugin(Deps.Plugins.bintrayPublish)
    }
}

allprojects {
    repositories {
        google()
        jcenter()

        maven { url = uri("https://kotlin.bintray.com/kotlin") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { url = uri("https://dl.bintray.com/icerockdev/moko") }
    }

    apply(plugin = Deps.Plugins.detekt.id)
    apply(plugin = Deps.Plugins.dokka.id)

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        input.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/main/kotlin"
        )
    }

    dependencies {
        "detektPlugins"(Deps.Libs.Jvm.detektFormatting)
    }

    plugins.withId(Deps.Plugins.androidLibrary.id) {
        configure<com.android.build.gradle.LibraryExtension> {
            compileSdkVersion(Deps.Android.compileSdk)

            defaultConfig {
                minSdkVersion(Deps.Android.minSdk)
                targetSdkVersion(Deps.Android.targetSdk)
            }
        }
    }

    plugins.withId(Deps.Plugins.mavenPublish.id) {
        configure<PublishingExtension> {
            repositories.maven("https://api.bintray.com/maven/icerockdev/moko/moko-mvvm/;publish=1") {
                name = "bintray"

                credentials {
                    username = System.getProperty("BINTRAY_USER")
                    password = System.getProperty("BINTRAY_KEY")
                }
            }
        }

        apply(plugin = Deps.Plugins.bintrayPublish.id)
    }
}

tasks.register("clean", Delete::class).configure {
    group = "build"
    delete(rootProject.buildDir)
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>().all {
    removeChildTasks(listOf(
        ":mvvm",
        ":sample",
        ":sample:android-app",
        ":sample:mpp-library"
    ).map { project(it) })

    doLast {
        val dir = outputDirectory.get()
        val from = File(dir,"-modules.html")
        val to = File(dir,"index.html")

        from.renameTo(to)

        dir.renameTo(file("docs"))
    }
}
