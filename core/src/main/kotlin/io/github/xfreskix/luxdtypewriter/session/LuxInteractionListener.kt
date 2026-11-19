package io.github.xfreskix.luxdtypewriter.session

import com.github.retrooper.packetevents.protocol.player.InteractionHand
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.engine.paper.entry.dialogue.speakersInDialogue
import com.typewritermc.engine.paper.entry.entries.EntityDefinitionEntry
import com.typewritermc.engine.paper.entry.entries.EntityInstanceEntry
import com.typewritermc.engine.paper.events.AsyncEntityDefinitionInteract
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Forwards clicks on Typewriter NPCs to the open dialogue. They are packet
 * entities, so LuxDialogues never sees these clicks itself.
 *
 * Left-click (`ATTACK`) skips or turns the page; right-click (`INTERACT_AT`)
 * confirms. Runs on a packet thread, so it only forwards the button.
 */
@Singleton
class LuxInteractionListener(
    private val registry: LuxSessionRegistry,
) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(event: AsyncEntityDefinitionInteract) {
        // The off hand fires a duplicate.
        if (event.hand != InteractionHand.MAIN_HAND) return

        val session = registry.find(event.player)?.takeIf { it.isActive }
        LuxLogger.trace(event.player) { "listener: action=${event.action} session=${session?.state ?: "none"}" }
        if (session == null) return

        if (!isSpeakerClick(event)) {
            LuxLogger.trace(event.player) {
                "listener: ignored a click on '${event.definition.id}', which is not a speaker"
            }
            return
        }

        when (event.action) {
            InteractAction.ATTACK -> session.click(select = false)
            InteractAction.INTERACT_AT -> session.click(select = true)
            else -> Unit // INTERACT duplicates INTERACT_AT.
        }
    }

    /**
     * Whether the clicked NPC speaks in the open dialogue. Without an entity
     * speaker (plain speaker, Citizens NPC) any NPC counts.
     */
    private fun isSpeakerClick(event: AsyncEntityDefinitionInteract): Boolean {
        val speakers = event.player.speakersInDialogue
        if (speakers.any { it.id == event.definition.id || it.id == event.instance.id }) return true
        return speakers.none { ref ->
            val entry = ref.get()
            entry is EntityDefinitionEntry || entry is EntityInstanceEntry
        }
    }
}
