// Top-level build file where you can add configuration options common to all submodules.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.10")
        classpath("com.google.gms:google-services:4.4.0")
    }
}

// This block tells Gradle where to find dependencies for ALL modules
allprojects {
    repositories  {
        google()
        mavenCentral()
    }
}

// Clean task
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}