package io.github.xfreskix.luxdtypewriter.text

import java.util.Locale

/** Applies a [TextFormat] to one dialogue line. */
object TypingSafeFormat {

    private val NAMED_COLORS: Map<String, String> = mapOf(
        "black" to "#000000", "dark_blue" to "#0000aa", "dark_green" to "#00aa00", "dark_aqua" to "#00aaaa",
        "dark_red" to "#aa0000", "dark_purple" to "#aa00aa", "gold" to "#ffaa00", "gray" to "#aaaaaa",
        "grey" to "#aaaaaa", "dark_gray" to "#555555", "dark_grey" to "#555555", "blue" to "#5555ff",
        "green" to "#55ff55", "aqua" to "#55ffff", "red" to "#ff5555", "light_purple" to "#ff55ff",
        "yellow" to "#ffff55", "white" to "#ffffff",
    )

    private val COLOR_TAGS = setOf("color", "colour", "c")
    private val FIRST_COLOR_TAGS = setOf("gradient", "transition")

    /** [baseColor] is the style's text colour as `#rrggbb`. */
    fun apply(format: TextFormat, line: String, baseColor: String): String = when (format) {
        TextFormat.TYPING_SAFE -> toTypingSafe(line, baseColor)
        TextFormat.MINI_MESSAGE -> line
        TextFormat.PLAIN -> toPlain(line)
    }

    /** Closing a colour returns to the enclosing one, or to [baseColor]. */
    fun toTypingSafe(line: String, baseColor: String): String {
        if ('<' !in line) return line
        val base = baseColor.lowercase(Locale.ROOT)
        val out = StringBuilder(line.length)
        val stack = ArrayList<Pair<String, String?>>()
        var shown = base
        var inlineColor: String? = null

        fun effectiveColor(): String = stack.lastOrNull { it.second != null }?.second ?: inlineColor ?: base
        fun emitColor() {
            val color = effectiveColor()
            if (color != shown) {
                out.append(color)
                shown = color
            }
        }

        TextMetrics.scan(line, onMarkup = { markup, _ ->
            when {
                TextMetrics.GLYPH.matches(markup) -> out.append(markup)
                markup.startsWith("#") -> {
                    inlineColor = markup.lowercase(Locale.ROOT)
                    stack.clear()
                    emitColor()
                }
                markup.startsWith("§") -> Unit
                else -> {
                    applyTag(markup, stack, onReset = { inlineColor = null })
                    emitColor()
                }
            }
        }) { character -> out.append(character) }
        return out.toString().trimTrailingColor()
    }

    fun toPlain(line: String): String {
        val out = StringBuilder(line.length)
        TextMetrics.scan(line, onMarkup = { markup, _ ->
            if (TextMetrics.GLYPH.matches(markup)) out.append(markup)
        }) { out.append(it) }
        return out.toString()
    }

    private inline fun applyTag(tag: String, stack: MutableList<Pair<String, String?>>, onReset: () -> Unit) {
        val inner = tag.removePrefix("<").removeSuffix(">")
        when {
            inner.equals("reset", ignoreCase = true) -> {
                stack.clear()
                onReset()
            }
            inner.startsWith("/") -> {
                val name = inner.removePrefix("/").substringBefore(':').lowercase(Locale.ROOT)
                val position = if (name.isEmpty()) stack.lastIndex else stack.indexOfLast { it.first == name }
                if (position >= 0) stack.removeAt(position)
            }
            inner.startsWith("!") -> Unit
            else -> {
                val name = inner.substringBefore(':').lowercase(Locale.ROOT)
                stack += name to colorOf(inner, name)
            }
        }
    }

    /** `null` when the tag sets no colour. */
    private fun colorOf(inner: String, name: String): String? {
        val arguments = inner.split(':').drop(1)
        return when {
            name.startsWith("#") -> hex(name)
            name in NAMED_COLORS -> NAMED_COLORS[name]
            name in COLOR_TAGS -> arguments.firstOrNull()?.let(::colorArgument)
            name in FIRST_COLOR_TAGS -> arguments.firstNotNullOfOrNull(::colorArgument)
            else -> null
        }
    }

    private fun colorArgument(argument: String): String? {
        val value = argument.trim().removeSurrounding("'").removeSurrounding("\"").lowercase(Locale.ROOT)
        return NAMED_COLORS[value] ?: hex(value)
    }

    private fun hex(value: String): String? = value.takeIf { TextMetrics.HEX_COLOR.matches(it) }

    /** A trailing colour code colours nothing. */
    private fun String.trimTrailingColor(): String {
        var result = this
        while (result.length >= 7 && TextMetrics.HEX_COLOR.matches(result.takeLast(7))) result = result.dropLast(7)
        return result
    }
}
