package io.github.xfreskix.luxdtypewriter.session

/** How a session tells a finished conversation (triggers fire) from an abandoned one. */
enum class CompletionPolicy {
    /** Callbacks on the last page and the answers announce it (generated dialogues). */
    EXPLICIT,

    /** Any close LuxdTypewriter did not cause counts as finished (YAML dialogues). */
    ON_NATURAL_STOP,
}
