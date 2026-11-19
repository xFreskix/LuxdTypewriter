package io.github.xfreskix.luxdtypewriter.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.launch
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import io.github.xfreskix.luxdtypewriter.LuxServices
import kotlinx.coroutines.Dispatchers

/**
 * Switches the open window to a YAML dialogue without closing it, e.g. to hand
 * over to a shop written in LuxDialogues.
 */
@Entry("lux_redirect_dialogue", "Redirect the open dialogue to a YAML dialogue", Colors.ORANGE, "mdi:swap-horizontal")
class LuxRedirectDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),

    @Help("Id of the target dialogue, as defined in the LuxDialogues configuration.")
    val dialogueId: String = "",

    @Help("Page to start on. Leave empty to use the target dialogue's own start page.")
    val page: String = "",
) : ActionEntry {
    override fun ActionTrigger.execute() {
        if (dialogueId.isBlank()) return
        val player = player
        val target = dialogueId
        val startPage = page
        Dispatchers.UntickedAsync.launch { LuxServices.bridge.openById(player, target, startPage.ifBlank { null }) }
    }
}
