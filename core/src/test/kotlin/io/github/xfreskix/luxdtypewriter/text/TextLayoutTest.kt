package io.github.xfreskix.luxdtypewriter.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TextLayoutTest {

    @Test
    fun `small caps rewrites letters only`() {
        assertEquals("ꜰʀᴀɴᴄʜᴇѕᴄᴏ", TextLayout.toSmallCaps("Franchesco"))
        assertEquals("ᴀ1, ʙ2!", TextLayout.toSmallCaps("A1, B2!"))
        assertEquals("", TextLayout.toSmallCaps(""))
    }

    @Test
    fun `small caps leaves every kind of markup intact`() {
        assertEquals("<gold>ʜᴏʟᴀ</gold>", TextLayout.toSmallCaps("<gold>Hola</gold>"))
        assertEquals("<#cdff29>ʟɪᴍᴇ", TextLayout.toSmallCaps("<#cdff29>lime"))
        assertEquals("#abcdefʜɪ", TextLayout.toSmallCaps("#abcdefhi"))
    }

    @Test
    fun `short text is not wrapped`() {
        assertEquals(listOf("Hola mundo"), TextLayout.wrap("Hola mundo", 34 * TextMetrics.STANDARD_ADVANCE))
    }

    @Test
    fun `long text wraps on spaces within the width`() {
        val width = 20 * TextMetrics.STANDARD_ADVANCE
        val lines = TextLayout.wrap("¡Ah, una cara nueva! Bienvenido a mi tienda, viajero.", width)
        assertTrue(lines.size > 1)
        lines.forEach { assertTrue(TextMetrics.width(it) <= width, "'$it' is too wide") }
    }

    @Test
    fun `formatting open at a break is reopened on the next line`() {
        val width = 20 * TextMetrics.STANDARD_ADVANCE
        val lines = TextLayout.wrap("<gold>uno dos tres cuatro cinco seis siete ocho", width)
        assertTrue(lines.size > 1)
        lines.forEach { assertTrue(it.startsWith("<gold>"), "'$it' lost its colour") }
    }

    @Test
    fun `a single word wider than the line is kept whole`() {
        val word = "Supercalifragilistico"
        assertEquals(listOf(word), TextLayout.wrap(word, 10 * TextMetrics.STANDARD_ADVANCE))
    }

    @Test
    fun `layout keeps author line breaks and reopens colours across them`() {
        val lines = TextLayout.layout("<aqua>uno\ndos</aqua>\ntres", 1000)
        assertEquals(listOf("<aqua>uno", "<aqua>dos</aqua>", "tres"), lines)
    }

    @Test
    fun `layout of empty text is one blank line`() {
        assertEquals(listOf(" "), TextLayout.layout("", 100))
        assertEquals(listOf(" "), TextLayout.layout("\n\n", 100))
    }

    @Test
    fun `fit shortens with an ellipsis and never splits markup`() {
        val width = 15 * TextMetrics.STANDARD_ADVANCE
        val fitted = TextLayout.fit("<green>Una respuesta demasiado larga para la caja", width)
        assertTrue(fitted.startsWith("<green>"))
        assertTrue(fitted.endsWith("..."))
        assertTrue(TextMetrics.width(fitted) <= width)
    }

    @Test
    fun `strip removes tags, hex codes and section codes`() {
        assertEquals("Hola mundo!", TextLayout.stripFormatting("<gold>Hola</gold> #ff0000mundo§a!"))
    }

    @Test
    fun `markup has no width`() {
        assertEquals(TextMetrics.width("[E-2041]"), TextMetrics.width("<aqua>[E-2041]</aqua>"))
        assertEquals(TextMetrics.width("[E-2041]"), TextMetrics.width("#55ffff[E-2041]"))
    }

    @Test
    fun `thai combining marks take no room`() {
        // ั (U+0E31) and ี (U+0E35) draw on top of the previous letter.
        assertEquals(TextMetrics.width("สวสด"), TextMetrics.width("สวัสดี"))
        assertEquals(4, TextMetrics.visibleLength("สวัสดี") - 2)
    }

    @Test
    fun `escaped brackets are text, not tags`() {
        assertEquals(TextMetrics.advanceOf('\\') + TextMetrics.advanceOf('<'), TextMetrics.width("\\<"))
        assertEquals("\\<b>", TextLayout.stripFormatting("\\<b>"))
    }
}
