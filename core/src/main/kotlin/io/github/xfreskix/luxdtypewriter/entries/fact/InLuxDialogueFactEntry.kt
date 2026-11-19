package io.github.xfreskix.luxdtypewriter.entries.fact

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.entries.GroupEntry
import com.typewritermc.engine.paper.entry.entries.ReadableFactEntry
import com.typewritermc.engine.paper.facts.FactData
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.DialogueView
import org.bukkit.entity.Player

/** `1` while the player is in any LuxDialogues dialogue, `0` otherwise. */
@Entry("in_lux_dialogue_fact", "1 while the player is in a LuxDialogues dialogue", Colors.PURPLE, "mdi:chat-question")
class InLuxDialogueFactEntry(
    override val id: String = "",
    override val name: String = "",
    override val comment: String = "",
    override val group: Ref<GroupEntry> = emptyRef(),

    @Help("Only report 1 for this dialogue id. Leave empty to report for any dialogue.")
    val dialogueId: String = "",
) : ReadableFactEntry {

    override fun readSinglePlayer(player: Player): FactData {
        val view = LuxServices.bridge.view(player)
        val inDialogue = view is DialogueView.Open && (dialogueId.isBlank() || view.dialogueId == dialogueId)
        return FactData(if (inDialogue) 1 else 0)
    }
}
