package io.github.xfreskix.luxdtypewriter.citizens

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class NpcPausesTest {
    private val alex = UUID.randomUUID()
    private val sam = UUID.randomUUID()

    @Test
    fun `an npc is paused by its first holder and resumed by its last`() {
        val pauses = NpcPauses()
        assertEquals(NpcPauses.Changes(setOf(1), emptySet()), pauses.sync(alex, setOf(1)))
        assertEquals(NpcPauses.Changes(emptySet(), emptySet()), pauses.sync(sam, setOf(1)))
        assertEquals(NpcPauses.Changes(emptySet(), emptySet()), pauses.sync(alex, emptySet()))
        assertEquals(NpcPauses.Changes(emptySet(), setOf(1)), pauses.sync(sam, emptySet()))
    }

    @Test
    fun `a speaker change moves the pause to the new npc`() {
        val pauses = NpcPauses()
        pauses.sync(alex, setOf(1))
        assertEquals(NpcPauses.Changes(setOf(2), setOf(1)), pauses.sync(alex, setOf(2)))
    }

    @Test
    fun `syncing the same npcs again changes nothing`() {
        val pauses = NpcPauses()
        pauses.sync(alex, setOf(1, 2))
        assertEquals(NpcPauses.Changes(emptySet(), emptySet()), pauses.sync(alex, setOf(1, 2)))
    }

    @Test
    fun `releasing a player who holds nothing changes nothing`() {
        assertEquals(NpcPauses.Changes(emptySet(), emptySet()), NpcPauses().sync(alex, emptySet()))
    }

    @Test
    fun `clear resumes every held npc once`() {
        val pauses = NpcPauses()
        pauses.sync(alex, setOf(1, 2))
        pauses.sync(sam, setOf(2, 3))
        assertEquals(setOf(1, 2, 3), pauses.clear())
        assertEquals(emptySet<Int>(), pauses.clear())
    }
}
