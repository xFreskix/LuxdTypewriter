package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import org.bukkit.entity.Player

/**
 * Plays a dialogue from the LuxDialogues YAML files as a sequence step. Closing on
 * its own counts as finished; a teardown (disconnect, cancel) as aborted.
 */
@Entry("lux_dialogue_ref", "Play a YAML-defined LuxDialogues dialogue as a step", Colors.ORANGE, "mdi:file-document-outline")
class LuxDialogueRefEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val speaker: Ref<SpeakerEntry> = emptyRef(),

    @Help("Id of the dialogue as defined in the LuxDialogues configuration.")
    val dialogueId: String = "",

    @Help("Page to start on. Leave empty to use the dialogue's own start page.")
    val page: String = "",
) : DialogueEntry {

    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<*> =
        LuxDialogueRefMessenger(player, context, this)
}
