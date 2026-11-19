import com.typewritermc.moduleplugin.ReleaseChannel

// Requires LuxDialogues and Typewriter's Entity extension. Citizens is optional
// and deliberately not a declared dependency (see CitizensSupport).

val engineVersion = rootProject.extra["engineVersion"] as String

// Artifacts are published without the beta suffix (0.9.0-beta-177 → 0.9.0).
val artifactVersion = engineVersion.substringBefore("-beta")

val citizensVersion: String = providers.gradleProperty("citizens.version").get()

repositories {
    maven("https://repo.citizensnpcs.co/")
}

dependencies {
    // EntityExtension does not publish its dependencies, so they are listed here.
    compileOnly("com.typewritermc:EntityExtension:$artifactVersion")
    compileOnly("com.typewritermc:RoadNetworkExtension:$artifactVersion")
    compileOnly("com.typewritermc:QuestExtension:$artifactVersion")

    // Optional Citizens support; only loaded when Citizens is installed.
    compileOnly("com.typewritermc:CitizensExtension:$artifactVersion")
    compileOnly("net.citizensnpcs:citizens-main:$citizensVersion") {
        // Its transitive tree clashes with Paper's; only its API is needed.
        exclude(group = "*", module = "*")
    }

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

tasks.test {
    useJUnitPlatform()
}

// LuxdTypewriter-<version>.jar instead of core-<version>.jar.
tasks.named<Jar>("jar") {
    archiveBaseName.set("LuxdTypewriter")
}

typewriter {
    namespace = "xfreskix"

    extension {
        name = "LuxdTypewriter"
        shortDescription = "Show Typewriter dialogue through the LuxDialogues interface"
        description = """
            |LuxdTypewriter renders Typewriter dialogue with the LuxDialogues interface.
            |
            |Write the story in the Typewriter web panel — speakers, pages, answers, criteria,
            |modifiers and triggers — and the player sees it through the LuxDialogues UI instead
            |of chat. Everything stays inside Typewriter's interaction system, so NPC activities,
            |cinematics, quests and the interaction context keep working exactly as they do with
            |the built-in dialogue entries.
            |
            |It also works the other way around: dialogues you already authored in LuxDialogues'
            |own YAML files can be played from a sequence and can start Typewriter sequences of
            |their own.
            |
            |Works with Typewriter's Entity NPCs out of the box.
            |
            |Optional (soft dependency): Citizens together with Typewriter's Citizens extension. When
            |both are installed, Citizens NPCs can speak in Lux dialogues, stand still while they talk
            |and react to left and right clicks. Without them the extension works the same.
        """.trimMargin()
        engineVersion = rootProject.extra["engineVersion"] as String
        channel = ReleaseChannel.BETA

        dependencies {
            dependency("typewritermc", "Entity")
        }

        paper {
            dependency("LuxDialogues")
        }
    }
}
