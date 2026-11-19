package io.github.xfreskix.luxdtypewriter.text

/**
 * Pixel widths in the default font. Markup (MiniMessage tags, `#rrggbb`, `§a`)
 * has no width, except glyph tags, which draw an image.
 */
object TextMetrics {
    /** 5 px glyph + 1 px spacing. */
    const val STANDARD_ADVANCE = 6

    const val SPACE_ADVANCE = 4

    const val GLYPH_ADVANCE = 9

    /** CJK, Hangul and other full-width characters. */
    const val WIDE_ADVANCE = 9

    private const val STANDARD_GLYPH_WIDTH = 5
    private const val GLYPH_SPACING = 1

    internal val TAG = Regex("""</?[a-zA-Z#!][^<>]*>""")
    internal val HEX_COLOR = Regex("#[0-9a-fA-F]{6}")
    internal val GLYPH = Regex("<(ld)?glyph:[^<>]+>", RegexOption.IGNORE_CASE)
    private const val SECTION = '§'

    /** Glyphs that are not 5 px wide. */
    private val GLYPH_WIDTHS: Map<Char, Int> = buildMap {
        "!',.:;|i".forEach { put(it, 1) }
        "`l".forEach { put(it, 2) }
        " \"()*[]{}It".forEach { put(it, 3) }
        "<>fk".forEach { put(it, 4) }
        "@~".forEach { put(it, 6) }
    }

    fun width(text: String): Int {
        var total = 0
        scan(text, onMarkup = { _, advance -> total += advance }, onCharacter = { total += advanceOf(it) })
        return total
    }

    /** Characters LuxDialogues types for [text], glyphs included. */
    fun visibleLength(text: String): Int {
        var count = 0
        scan(text, onMarkup = { markup, _ -> if (GLYPH.matches(markup)) count++ }, onCharacter = { count++ })
        return count
    }

    fun advanceOf(character: Char): Int = when {
        isZeroWidth(character) -> 0
        isWide(character) -> WIDE_ADVANCE
        else -> (GLYPH_WIDTHS[character] ?: STANDARD_GLYPH_WIDTH) + GLYPH_SPACING
    }

    /** Walks [text], reporting markup (with its width) and characters separately. */
    inline fun scan(text: String, onMarkup: (String, Int) -> Unit, onCharacter: (Char) -> Unit) {
        var index = 0
        while (index < text.length) {
            val markup = markupAt(text, index)
            if (markup != null) {
                onMarkup(markup, markupWidth(markup))
                index += markup.length
            } else {
                onCharacter(text[index])
                index++
            }
        }
    }

    /** The markup starting at [index], or `null`. */
    fun markupAt(text: String, index: Int): String? = when (text[index]) {
        // An escaped bracket (\<) is text.
        '<' -> if (index > 0 && text[index - 1] == '\\') null else TAG.matchAt(text, index)?.value
        '#' -> HEX_COLOR.matchAt(text, index)?.value
        SECTION -> if (index + 1 < text.length) text.substring(index, index + 2) else text.substring(index)
        else -> null
    }

    fun markupWidth(markup: String): Int = if (GLYPH.matches(markup)) GLYPH_ADVANCE else 0

    /** Combining marks (Thai vowels, accents) draw over the previous character. */
    private fun isZeroWidth(character: Char): Boolean = when (Character.getType(character).toByte()) {
        Character.NON_SPACING_MARK, Character.ENCLOSING_MARK, Character.FORMAT -> true
        else -> false
    }

    private fun isWide(character: Char): Boolean {
        val code = character.code
        return code in 0x1100..0x115F || code in 0x2E80..0xA4CF || code in 0xAC00..0xD7A3 ||
            code in 0xF900..0xFAFF || code in 0xFE30..0xFE4F || code in 0xFF00..0xFF60 || code in 0xFFE0..0xFFE6
    }
}
