package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.EntryTrigger
import com.typewritermc.engine.paper.entry.entries.EventTrigger
import com.typewritermc.engine.paper.entry.entries.Var

/** Choosing an answer ends the window; branching happens through [triggers]. */
data class LuxAnswer(
    @Placeholder
    @Colored
    @Help("The text of this answer.")
    val text: Var<String> = ConstVar(""),

    @Help("Conditions that must be met for this answer to be shown.")
    val criteria: List<Criteria> = emptyList(),

    @Help("Fact changes applied when this answer is chosen.")
    val modifiers: List<Modifier> = emptyList(),

    @Help("Entries triggered when this answer is chosen.")
    val triggers: List<Ref<TriggerableEntry>> = emptyList(),

    @MultiLine
    @Placeholder
    @Colored
    @Help("Optional chat message sent when this answer is picked, one message per line.")
    val replyMessage: String = "",
) {
    val eventTriggers: List<EventTrigger> get() = triggers.map(::EntryTrigger)
}
