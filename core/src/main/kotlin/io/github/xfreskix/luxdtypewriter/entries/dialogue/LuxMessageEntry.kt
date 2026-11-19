package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.entries.Var
import io.github.xfreskix.luxdtypewriter.style.LuxStyleEntry
import org.bukkit.entity.Player

/**
 * A single message without answers, like the built-in `message`. Chained messages
 * reuse the open window.
 */
@Entry("lux_message", "A single LuxDialogues message with no answers", Colors.BLUE, "mdi:message-text")
class LuxMessageEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    override val speaker: Ref<SpeakerEntry> = emptyRef(),

    @Help("The look and feel to render with. Leave empty for the default LuxDialogues style.")
    override val style: Ref<LuxStyleEntry> = emptyRef(),

    @MultiLine
    @Placeholder
    @Colored
    @Help("The message shown to the player.")
    val text: Var<String> = ConstVar(""),
) : DialogueEntry, LuxDialogueSource {

    /** One page, so the panel shows a flat text field instead of a pages list. */
    override val pages: List<LuxPage>
        get() = listOf(LuxPage(lines = listOf(text)))

    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<*> =
        LuxDialogueMessenger(player, context, this)
}
