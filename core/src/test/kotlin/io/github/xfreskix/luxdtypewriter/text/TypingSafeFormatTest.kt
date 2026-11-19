package io.github.xfreskix.luxdtypewriter.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class TypingSafeFormatTest {

    private val base = "#4f4a3e"

    private fun safe(line: String) = TypingSafeFormat.toTypingSafe(line, base)

    @Test
    fun `the reported line keeps its colour without any tag`() {
        val line = safe("สวัสดีครับ กระผม <aqua>[E-2041]</aqua>")
        assertEquals("สวัสดีครับ กระผม #55ffff[E-2041]", line)
        assertFalse('<' in line)
    }

    @Test
    fun `closing a colour returns to the base colour`() {
        assertEquals("Hola #ff5555mundo${base} cruel", safe("Hola <red>mundo</red> cruel"))
    }

    @Test
    fun `nested colours return to the enclosing one`() {
        assertEquals("#55ffffa#ff5555b#55ffffc", safe("<aqua>a<red>b</red>c</aqua>"))
    }

    @Test
    fun `hex and color tags become hex codes`() {
        assertEquals("#123abcx", safe("<#123ABC>x"))
        assertEquals("#ffaa00x", safe("<color:gold>x"))
        assertEquals("#00ff00x", safe("<c:#00ff00>x"))
    }

    @Test
    fun `a gradient keeps its first colour`() {
        assertEquals("#ff0000grad", safe("<gradient:#ff0000:#0000ff>grad</gradient>"))
    }

    @Test
    fun `decorations and unknown tags are dropped, glyphs kept`() {
        assertEquals("bold <ldglyph:coin> text", safe("<b>bold</b> <ldglyph:coin> <hover:show_text:'x'>text</hover>"))
    }

    @Test
    fun `reset returns to the base colour`() {
        assertEquals("#55ff55a${base}b", safe("<green>a<reset>b"))
    }

    @Test
    fun `plain text is untouched`() {
        assertEquals("Hola mundo", safe("Hola mundo"))
    }

    @Test
    fun `plain format strips colours but keeps glyphs`() {
        assertEquals("a <glyph:x> b", TypingSafeFormat.apply(TextFormat.PLAIN, "<red>a</red> <glyph:x> #ffffffb", base))
    }

    @Test
    fun `minimessage format sends the text as written`() {
        val line = "<aqua>x</aqua>"
        assertEquals(line, TypingSafeFormat.apply(TextFormat.MINI_MESSAGE, line, base))
    }

    @Test
    fun `typed length does not count colour codes`() {
        assertEquals(
            TextMetrics.visibleLength("สวัสดีครับ กระผม [E-2041]"),
            TextMetrics.visibleLength(safe("สวัสดีครับ กระผม <aqua>[E-2041]</aqua>")),
        )
    }
}
