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

/** Closes the player's open dialogue, including YAML ones, e.g. from a "leave" answer. */
@Entry("lux_stop_dialogue", "Close the player's open LuxDialogues dialogue", Colors.RED, "mdi:close-box")
class LuxStopDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val player = player
        Dispatchers.UntickedAsync.launch { LuxServices.bridge.clear(player) }
    }
}
