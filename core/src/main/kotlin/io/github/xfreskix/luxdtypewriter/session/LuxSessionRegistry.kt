package io.github.xfreskix.luxdtypewriter.session

import com.typewritermc.core.extension.annotations.Singleton
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/** Which player is in which LuxdTypewriter dialogue. Thread-safe, lock-free. */
@Singleton
class LuxSessionRegistry {

    private val sessions = ConcurrentHashMap<UUID, LuxSession>()

    /** Generated dialogue id → entry id. */
    private val dialogueOwners = ConcurrentHashMap<String, String>()

    private val idCounter = AtomicLong()

    val activeCount: Int get() = sessions.size

    /** Numeric and far above hand-written ids, so it never collides with a YAML dialogue. */
    fun generateDialogueId(): String = (NUMERIC_ID_BASE + idCounter.incrementAndGet()).toString()

    fun claim(dialogueId: String, entryId: String) {
        dialogueOwners[dialogueId] = entryId
    }

    /** `null` for YAML dialogues. */
    fun ownerOf(dialogueId: String): String? = dialogueOwners[dialogueId]

    fun forget(dialogueId: String) {
        dialogueOwners.remove(dialogueId)
    }

    /** Ends any previous session: LuxDialogues shows one dialogue at a time. */
    fun open(session: LuxSession) {
        val previous = sessions.put(session.playerId, session)
        if (previous != null && previous.isActive) {
            LuxLogger.warnOnce(
                key = "session-replaced",
                message = "A dialogue was opened for a player who already had one. The previous " +
                    "dialogue was cancelled. This is handled safely, but usually means two " +
                    "sequences are competing for the same player.",
            )
            previous.markAborting()
            previous.end()
        }
    }

    fun find(playerId: UUID): LuxSession? = sessions[playerId]

    fun find(player: Player): LuxSession? = sessions[player.uniqueId]

    fun activeSessions(): List<LuxSession> = sessions.values.filter { it.isActive }

    /** The session of [playerId] only if it owns [dialogueId]. */
    fun findOwning(playerId: UUID, dialogueId: String?): LuxSession? {
        if (dialogueId == null) return null
        return sessions[playerId]?.takeIf { it.dialogueId == dialogueId }
    }

    /** Removes [session] only if still current: a replaced dialogue may close late. */
    fun release(session: LuxSession) {
        sessions.remove(session.playerId, session)
    }

    fun close(playerId: UUID, completed: Boolean = false) {
        val session = sessions.remove(playerId) ?: return
        if (completed) session.markCompleting() else session.markAborting()
        session.end()
    }

    /** Called on shutdown and `/typewriter reload`. */
    fun closeAll() {
        val snapshot = sessions.values.toList()
        sessions.clear()
        dialogueOwners.clear()
        snapshot.forEach { session ->
            session.markAborting()
            session.end()
        }
    }

    private companion object {
        const val NUMERIC_ID_BASE = 7_900_000_000L
    }
}
