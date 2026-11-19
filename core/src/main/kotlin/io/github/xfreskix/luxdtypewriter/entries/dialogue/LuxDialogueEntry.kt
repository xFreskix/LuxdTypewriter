package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import io.github.xfreskix.luxdtypewriter.style.LuxStyleEntry
import org.bukkit.entity.Player

/**
 * A multi-page conversation in one entry, ending in optional answers that branch
 * through Typewriter triggers.
 */
@Entry("lux_dialogue", "A full multi-page conversation shown with LuxDialogues", Colors.BLUE, "mdi:forum")
@ContextKeys(LuxDialogueContextKeys::class)
class LuxDialogueEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val speaker: Ref<SpeakerEntry> = emptyRef(),

    @Help("The look and feel to render with. Leave empty for the default LuxDialogues style.")
    override val style: Ref<LuxStyleEntry> = emptyRef(),

    @Help("The pages of the conversation, played in order.")
    override val pages: List<LuxPage> = emptyList(),
) : DialogueEntry, LuxDialogueSource {

    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<*> =
        LuxDialogueMessenger(player, context, this)
}
