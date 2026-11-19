package io.github.xfreskix.luxdtypewriter.text

import java.util.Locale

/** Wrapping, shortening, small caps and stripping. Markup is never split or measured. */
object TextLayout {
    private const val ELLIPSIS = "..."
    private const val BLANK_LINE = " "

    /** Tags that never need re-opening on the next line. */
    private val STANDALONE_TAGS = setOf("glyph", "ldglyph", "br", "newline")

    /**
     * Splits on line breaks and wraps to [maxWidth] pixels. Open formatting is
     * re-opened on each new line, since LuxDialogues renders lines separately.
     */
    fun layout(text: String, maxWidth: Int): List<String> {
        val paragraphs = text.lines().dropLastWhile { it.isBlank() }
        if (paragraphs.isEmpty()) return listOf(BLANK_LINE)
        val lines = mutableListOf<String>()
        val consumed = StringBuilder()
        for (paragraph in paragraphs) {
            val reopened = openFormatting(consumed.toString())
            consumed.append(paragraph)
            if (paragraph.isBlank()) lines += BLANK_LINE
            else lines += wrap(reopened + paragraph, maxWidth)
        }
        return lines
    }

    /** Breaks on spaces; a word wider than the line is kept whole. */
    fun wrap(text: String, maxWidth: Int): List<String> {
        if (maxWidth <= 0 || TextMetrics.width(text) <= maxWidth) return listOf(text)
        val words = text.split(' ').filter { it.isNotEmpty() }
        if (words.isEmpty()) return listOf(text)

        val lines = mutableListOf<String>()
        var line = StringBuilder()
        var used = 0
        // Words without the re-opened prefixes, so formatting is not counted twice.
        val consumed = StringBuilder()
        for (word in words) {
            val wordWidth = TextMetrics.width(word)
            if (used > 0 && used + TextMetrics.SPACE_ADVANCE + wordWidth > maxWidth) {
                lines += line.toString()
                line = StringBuilder(openFormatting(consumed.toString()))
                used = 0
            }
            if (used > 0) {
                line.append(' ')
                used += TextMetrics.SPACE_ADVANCE
            }
            line.append(word)
            used += wordWidth
            if (consumed.isNotEmpty()) consumed.append(' ')
            consumed.append(word)
        }
        if (line.isNotEmpty()) lines += line.toString()
        return lines
    }

    /** Shortens [text] to [maxWidth] pixels with "..."; answers cannot wrap. */
    fun fit(text: String, maxWidth: Int): String {
        if (maxWidth <= 0 || TextMetrics.width(text) <= maxWidth) return text
        val budget = maxWidth - TextMetrics.width(ELLIPSIS)
        val out = StringBuilder(text.length)
        var used = 0
        var index = 0
        while (index < text.length) {
            val markup = TextMetrics.markupAt(text, index)
            val piece = markup ?: text[index].toString()
            val advance = markup?.let(TextMetrics::markupWidth) ?: TextMetrics.advanceOf(text[index])
            if (used + advance > budget) return out.append(ELLIPSIS).toString()
            used += advance
            out.append(piece)
            index += piece.length
        }
        return out.toString()
    }

    /** ꜱᴍᴀʟʟ ᴄᴀᴘꜱ, leaving markup untouched. */
    fun toSmallCaps(input: String): String {
        val out = StringBuilder(input.length)
        TextMetrics.scan(input, onMarkup = { markup, _ -> out.append(markup) }) { character ->
            out.append(SMALL_CAPS[character] ?: character)
        }
        return out.toString()
    }

    fun stripFormatting(input: String): String {
        val out = StringBuilder(input.length)
        TextMetrics.scan(input, onMarkup = { _, _ -> }) { out.append(it) }
        return out.toString()
    }

    /** The formatting still open at the end of [consumed]. */
    internal fun openFormatting(consumed: String): String {
        val active = ArrayDeque<String>()
        var index = 0
        while (index < consumed.length) {
            val markup = TextMetrics.markupAt(consumed, index)
            if (markup == null) {
                index++
                continue
            }
            index += markup.length
            when {
                markup.startsWith("#") -> {
                    active.removeAll { it.startsWith("#") }
                    active.addLast(markup)
                }
                markup.equals("§r", ignoreCase = true) -> active.clear()
                markup.startsWith("§") -> active.addLast(markup)
                markup.equals("<reset>", ignoreCase = true) -> active.clear()
                markup.startsWith("</") -> {
                    val name = markup.tagName()
                    val position = active.indexOfLast { it.tagName() == name || name.isEmpty() }
                    if (position >= 0) active.removeAt(position)
                }
                !isStandalone(markup) -> active.addLast(markup)
            }
        }
        return active.joinToString("")
    }

    private fun isStandalone(markup: String): Boolean {
        val name = markup.tagName()
        return name in STANDALONE_TAGS || name.startsWith("wait.")
    }

    internal fun String.tagName(): String =
        removePrefix("<").removePrefix("/").removeSuffix(">").substringBefore(':').lowercase(Locale.ROOT)

    /** `s` and `q` have no small capital; `ѕ` and `ǫ` are the usual stand-ins. */
    private val SMALL_CAPS: Map<Char, Char> = buildMap {
        val pairs = "abcdefghijklmnopqrstuvwxyz".zip("ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀѕᴛᴜᴠᴡxʏᴢ")
        for ((lower, small) in pairs) {
            put(lower, small)
            put(lower.uppercaseChar(), small)
        }
    }
}
