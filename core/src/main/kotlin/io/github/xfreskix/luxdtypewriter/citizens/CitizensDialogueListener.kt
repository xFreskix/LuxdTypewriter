package io.github.xfreskix.luxdtypewriter.citizens

import com.typewritermc.citizens.entries.entity.ReferenceNpcEntry
import com.typewritermc.engine.paper.entry.dialogue.speakersInDialogue
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.DialogueView
import io.github.xfreskix.luxdtypewriter.bridge.LuxDialogueViewChangedEvent
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import net.citizensnpcs.api.event.NPCClickEvent
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Holds Citizens NPCs while they speak in a dialogue and forwards clicks on them,
 * split by button. Only created by [CitizensSupport], so servers without Citizens
 * never load this class.
 */
internal class CitizensDialogueListener : Listener {

    private val pauses = NpcPauses()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDialogueViewChanged(event: LuxDialogueViewChangedEvent) {
        when (event.current) {
            is DialogueView.Open -> sync(event.player, speakingNpcIds(event.player))
            DialogueView.None -> sync(event.player, emptySet())
            DialogueView.Unknown -> Unit // A failed read; keep the NPCs as they are.
        }
    }

    // Before Typewriter's own NPC click handling, which cannot tell the buttons apart.
    @EventHandler(priority = EventPriority.LOWEST)
    fun onLeftClick(event: NPCLeftClickEvent) = click(event, select = false)

    @EventHandler(priority = EventPriority.LOWEST)
    fun onRightClick(event: NPCRightClickEvent) = click(event, select = true)

    /** Resumes every held NPC; called when the support shuts down. */
    fun releaseAll() {
        pauses.clear().forEach(CitizensNpcControl::resume)
    }

    private fun click(event: NPCClickEvent, select: Boolean) {
        val player = event.clicker
        val session = LuxServices.registry.find(player)?.takeIf { it.isActive } ?: return
        if (event.npc.id !in speakingNpcIds(player)) return
        LuxLogger.trace(player) { "citizens: npc=${event.npc.id} select=$select" }
        session.click(select)
    }

    private fun sync(player: Player, npcIds: Set<Int>) {
        val changes = pauses.sync(player.uniqueId, npcIds)
        changes.pause.forEach(CitizensNpcControl::pause)
        changes.resume.forEach(CitizensNpcControl::resume)
    }

    private fun speakingNpcIds(player: Player): Set<Int> =
        player.speakersInDialogue.mapNotNullTo(HashSet()) { (it.get() as? ReferenceNpcEntry)?.npcId }
}
