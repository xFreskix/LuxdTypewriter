package io.github.xfreskix.luxdtypewriter.entries.audience

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.DialogueView
import io.github.xfreskix.luxdtypewriter.bridge.LuxDialogueViewChangedEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

/** Re-evaluated only when the player's dialogue view changes. */
class LuxDialogueAudienceFilter(
    ref: Ref<out AudienceFilterEntry>,
    private val dialogueId: String,
) : AudienceFilter(ref) {

    override fun filter(player: Player): Boolean = includes(LuxServices.bridge.view(player))

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDialogueViewChanged(event: LuxDialogueViewChangedEvent) {
        if (includes(event.previous) != includes(event.current)) event.player.refresh()
    }

    private fun includes(view: DialogueView): Boolean =
        view is DialogueView.Open && (dialogueId.isBlank() || view.dialogueId == dialogueId)
}
