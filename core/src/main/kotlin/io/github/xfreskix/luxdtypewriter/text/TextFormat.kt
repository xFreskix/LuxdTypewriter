package io.github.xfreskix.luxdtypewriter.text

/**
 * How dialogue text is sent. LuxDialogues types the raw string and applies
 * MiniMessage only at the end, so tags show while typing and the box shifts.
 */
enum class TextFormat {
    /** Colours become `#rrggbb` codes; other tags except glyphs are dropped. */
    TYPING_SAFE,

    /** Sent as written. */
    MINI_MESSAGE,

    /** Everything but glyphs is removed. */
    PLAIN,
}
