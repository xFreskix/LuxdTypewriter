package io.github.xfreskix.luxdtypewriter.session

import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import io.github.xfreskix.luxdtypewriter.bridge.DialogueEventSink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

/**
 * Turns LuxDialogues events into session transitions. Only events for the
 * dialogue a session owns count, so dialogues from other plugins are ignored.
 */
@Singleton
class LuxEventListener(
    private val registry: LuxSessionRegistry,
) : Listener, DialogueEventSink {

    override fun onStarted(playerId: UUID, dialogueId: String) {
        registry.findOwning(playerId, dialogueId)?.markStarted()
    }

    override fun onPageChanged(playerId: UUID, dialogueId: String, pageId: String) {
        registry.findOwning(playerId, dialogueId)?.markPageChanged(pageId)
    }

    override fun onAnswered(playerId: UUID, dialogueId: String, answerId: String) {
        registry.findOwning(playerId, dialogueId)?.markAnswered(answerId)
    }

    override fun onRedirected(playerId: UUID, fromDialogueId: String?, toDialogueId: String) {
        fromDialogueId?.let(::forgetOwnerLater)
        val session = registry.find(playerId) ?: return
        if (session.markRedirected(fromDialogueId, toDialogueId) == RedirectOutcome.ABORT) {
            registry.release(session)
            session.markAborting()
            session.end()
        }
    }

    override fun onStopped(playerId: UUID, dialogueId: String) {
        forgetOwnerLater(dialogueId)
        val session = registry.findOwning(playerId, dialogueId) ?: return
        registry.release(session)
        session.end()
    }

    /** A stale session would swallow the events of the player's next conversation. */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        registry.close(event.player.uniqueId, completed = false)
    }

    /** Delayed so the `on_lux_dialogue_stop` listener can still read it for the same event. */
    private fun forgetOwnerLater(dialogueId: String) {
        Dispatchers.UntickedAsync.launch {
            delay(OWNER_GRACE)
            registry.forget(dialogueId)
        }
    }

    private companion object {
        val OWNER_GRACE = 100.milliseconds
    }
}
