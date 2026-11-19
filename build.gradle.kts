// Kotlin must match the version the Typewriter module plugin is built with
// (2.3.20, KSP 2.3.8); any other makes KSP fail with "incompatible plugin".

plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("com.typewritermc.module-plugin") version "2.2.0" apply false
}

val engineVersion: String = providers.gradleProperty("typewriter.engineVersion").get()
val luxApiVersion: String = providers.gradleProperty("luxdialogues.apiVersion").get()
val extensionVersion: String = providers.gradleProperty("extension.version").get()

extra["engineVersion"] = engineVersion
extra["luxApiVersion"] = luxApiVersion

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.typewritermc.module-plugin")

    group = "io.github.xfreskix"
    version = extensionVersion

    repositories {
        mavenCentral()
        maven("https://repo.aselstudios.com/releases")
        // pathetic-bukkit, needed by RoadNetworkExtension, is only on JitPack.
        maven("https://jitpack.io")
    }

    dependencies {
        // Provided by the LuxDialogues plugin at runtime.
        "compileOnly"("org.aselstudios:LuxDialoguesAPI:$luxApiVersion")
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>("kotlin") {
        jvmToolchain(21)
        compilerOptions {
            // Used by Typewriter 0.9 APIs.
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    // Copies the jar to a test server; only registered when `server.path` is set.
    val serverPath = providers.gradleProperty("server.path").orNull
    if (serverPath != null) {
        tasks.register<Copy>("buildAndMove") {
            group = "build"
            description = "Builds the extension and copies it into the test server."
            dependsOn(tasks.named("jar"))
            from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
            into(rootProject.file(serverPath))
            // A fixed name, so every build replaces the previous jar on the server.
            rename { "LuxdTypewriter.jar" }
            outputs.upToDateWhen { false }
        }
    }
}
