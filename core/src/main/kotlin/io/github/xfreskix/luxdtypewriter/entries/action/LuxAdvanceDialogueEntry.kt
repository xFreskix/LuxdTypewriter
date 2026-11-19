package io.github.xfreskix.luxdtypewriter.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.utils.launch
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import io.github.xfreskix.luxdtypewriter.LuxServices
import kotlinx.coroutines.Dispatchers

/** Advances the dialogue as a click would: skip typing, turn the page or confirm. */
@Entry("lux_advance_dialogue", "Advance the player's dialogue as if they clicked", Colors.CYAN, "mdi:cursor-default-click")
class LuxAdvanceDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val player = player
        Dispatchers.UntickedAsync.launch { LuxServices.bridge.advance(player) }
    }
}
