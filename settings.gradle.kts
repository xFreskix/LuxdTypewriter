// LuxdTypewriter: one extension jar (:core) that shows Typewriter dialogue
// through LuxDialogues.

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // Typewriter module plugin 2.2.0 (needed by engine 0.9) is on the beta channel.
        maven("https://maven.typewritermc.com/beta")
    }
}

plugins {
    // Downloads a JDK 21 toolchain when none is installed.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "LuxdTypewriter"

include("core")
