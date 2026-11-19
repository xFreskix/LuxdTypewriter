package io.github.xfreskix.luxdtypewriter.citizens

import java.util.UUID

/**
 * Which players hold which NPCs. An NPC is paused by its first holder and resumed
 * when the last one lets go, so two players talking to it share one pause.
 * Main thread only.
 */
internal class NpcPauses {
    private val held = HashMap<UUID, Set<Int>>()
    private val holders = HashMap<Int, Int>()

    /** Makes [player] hold exactly [npcIds] and returns what to pause and resume. */
    fun sync(player: UUID, npcIds: Set<Int>): Changes {
        val before = held[player].orEmpty()
        if (npcIds.isEmpty()) held.remove(player) else held[player] = npcIds
        return Changes(
            pause = (npcIds - before).filterTo(HashSet()) { acquire(it) },
            resume = (before - npcIds).filterTo(HashSet()) { release(it) },
        )
    }

    /** Lets go of everything and returns every NPC to resume. */
    fun clear(): Set<Int> {
        val all = holders.keys.toSet()
        held.clear()
        holders.clear()
        return all
    }

    /** Returns whether [npcId] was not held before. */
    private fun acquire(npcId: Int): Boolean = holders.merge(npcId, 1, Int::plus) == 1

    /** Returns whether nobody holds [npcId] any more. */
    private fun release(npcId: Int): Boolean {
        val left = (holders[npcId] ?: return false) - 1
        if (left > 0) holders[npcId] = left else holders.remove(npcId)
        return left <= 0
    }

    data class Changes(val pause: Set<Int>, val resume: Set<Int>)
}
