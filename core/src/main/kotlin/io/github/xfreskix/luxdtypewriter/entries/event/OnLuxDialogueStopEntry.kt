package io.github.xfreskix.luxdtypewriter.entries.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.entries.EventEntry

/** Fires when any LuxDialogues dialogue closes. */
@Entry("on_lux_dialogue_stop", "When any LuxDialogues dialogue closes", Colors.YELLOW, "mdi:chat-remove")
class OnLuxDialogueStopEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),

    @Help("Only fire for this dialogue id. Leave empty to fire for every dialogue.")
    val dialogueId: String = "",

    @Help("Only fire for this Lux dialogue entry. Empty fires for every dialogue.")
    val dialogue: Ref<DialogueEntry> = emptyRef(),
) : EventEntry
