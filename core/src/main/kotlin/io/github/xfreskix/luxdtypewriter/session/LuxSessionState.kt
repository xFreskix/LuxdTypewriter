package io.github.xfreskix.luxdtypewriter.session

/**
 * ```
 *  OPENING ──► RUNNING ──► COMPLETING ──┐
 *     │           │                     ├──► ENDED
 *     └───────────┴──► ABORTING ────────┘
 * ```
 * COMPLETING/ABORTING hold the decision until the window is actually closed.
 */
enum class LuxSessionState {
    /** Sent, not on screen yet. */
    OPENING,

    RUNNING,

    COMPLETING,

    ABORTING,

    ENDED,
}
