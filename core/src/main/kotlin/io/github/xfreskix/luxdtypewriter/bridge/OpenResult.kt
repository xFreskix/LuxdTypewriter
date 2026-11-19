package io.github.xfreskix.luxdtypewriter.bridge

enum class OpenResult {
    OPENED,

    /** Accepted, but never shown. */
    NOT_SHOWN,

    /** The player is in LuxDialogues' command cooldown. */
    COOLDOWN,
    UNAVAILABLE,

    /** LuxDialogues threw. */
    FAILED,
}
