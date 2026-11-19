package io.github.xfreskix.luxdtypewriter.citizens

/** State of the optional Citizens support. */
enum class CitizensStatus(val description: String) {
    ACTIVE("active"),
    NOT_INSTALLED("off, Citizens is not installed"),
    EXTENSION_MISSING("off, Typewriter's Citizens extension is not installed"),
    FAILED("off, it failed to start (see the console)"),
}
