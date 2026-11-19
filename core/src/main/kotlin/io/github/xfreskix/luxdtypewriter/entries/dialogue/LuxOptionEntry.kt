package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.ContextKeys
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
 * A single question with answers, like the built-in `option`. The choice is
 * published as [LuxDialogueContextKeys.SELECTED_ANSWER].
 */
@Entry("lux_option", "A single LuxDialogues question with answers", Colors.MEDIUM_SEA_GREEN, "mdi:format-list-bulleted")
@ContextKeys(LuxDialogueContextKeys::class)
class LuxOptionEntry(
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
    @Help("The question shown above the answers.")
    val text: Var<String> = ConstVar(""),

    @Help("The answers the player can choose from.")
    val options: List<LuxAnswer> = emptyList(),
) : DialogueEntry, LuxDialogueSource {

    override val pages: List<LuxPage>
        get() = listOf(LuxPage(lines = listOf(text), answers = options))

    override fun messenger(player: Player, context: InteractionContext): DialogueMessenger<*> =
        LuxDialogueMessenger(player, context, this)
}
