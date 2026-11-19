package io.github.xfreskix.luxdtypewriter.style

import io.github.xfreskix.luxdtypewriter.util.LuxLogger

/** Validation of Minecraft sound keys such as `block.cherry_wood.hit`. */
object SoundKeys {
    private val FORMAT = Regex("^([a-z0-9._-]+:)?[a-z0-9._/-]+$")

    fun isValid(key: String): Boolean = FORMAT.matches(key)

    /** Whether [sound] should play; warns once about a malformed key. */
    fun isPlayable(sound: LuxSound): Boolean {
        if (!sound.isEnabled) return false
        if (isValid(sound.key)) return true
        LuxLogger.warnOnce(
            key = "invalid-sound-${sound.key}",
            message = "The Lux style sound '${sound.key}' is not a valid Minecraft sound key, so it was muted. " +
                "Write it lowercase and dot separated, e.g. 'block.cherry_wood.hit' instead of " +
                "'BLOCK_CHERRY_WOOD_HIT'.",
        )
        return false
    }
}
