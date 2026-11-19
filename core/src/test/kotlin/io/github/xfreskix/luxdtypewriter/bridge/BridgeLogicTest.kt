package io.github.xfreskix.luxdtypewriter.bridge

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BridgeLogicTest {

    @Test
    fun `lux versions are compared numerically`() {
        assertTrue(isSupportedLuxVersion("4.1.3"))
        assertTrue(isSupportedLuxVersion("4.1.10"))
        assertTrue(isSupportedLuxVersion("4.2"))
        assertTrue(isSupportedLuxVersion("5.0.0-SNAPSHOT"))
        assertFalse(isSupportedLuxVersion("4.1.2"))
        assertFalse(isSupportedLuxVersion("4.0.0"))
        assertFalse(isSupportedLuxVersion("dev"))
    }

    private fun open(page: String? = "p1", answers: Boolean = false, typing: Boolean = false) =
        DialogueView.Open("1", page, answers, typing, 0)

    @Test
    fun `a click skips typing, turns pages and confirms answers only with select`() {
        assertEquals(ClickIntent.SKIP, clickIntent(open(typing = true), select = false))
        assertEquals(ClickIntent.TURN, clickIntent(open(), select = false))
        assertEquals(ClickIntent.CONFIRM, clickIntent(open(answers = true), select = true))
        assertEquals(ClickIntent.NONE, clickIntent(open(answers = true), select = false))
        assertEquals(ClickIntent.NONE, clickIntent(open(page = null), select = true))
        assertEquals(ClickIntent.NONE, clickIntent(DialogueView.None, select = true))
    }

    @Test
    fun `only dialogue or page changes count as moving`() {
        assertFalse(hasMoved(open(), open(typing = true)))
        assertTrue(hasMoved(open(), open(page = "p2")))
        assertTrue(hasMoved(DialogueView.None, open()))
        assertFalse(hasMoved(DialogueView.None, DialogueView.None))
    }

    @Test
    fun `a redirect uses an unconditional start page`() {
        assertEquals("intro", unconditionalStartPage("shop", listOf("intro"), listOf("intro", "buy")))
    }

    @Test
    fun `a redirect without a usable start page fails clearly`() {
        assertThrows<IllegalStateException> { unconditionalStartPage("shop", null, listOf("intro")) }
        assertThrows<IllegalStateException> { unconditionalStartPage("shop", listOf("missing"), listOf("intro")) }
    }
}
