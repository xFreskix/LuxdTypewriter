package io.github.xfreskix.luxdtypewriter.bridge

import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.entry.triggerAllFor
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.entries.event.LuxAnswerEventContextKeys
import io.github.xfreskix.luxdtypewriter.entries.event.OnLuxAnswerEntry
import io.github.xfreskix.luxdtypewriter.entries.event.OnLuxDialogueStartEntry
import io.github.xfreskix.luxdtypewriter.entries.event.OnLuxDialogueStopEntry
import io.github.xfreskix.luxdtypewriter.text.TextResolver
import org.aselstudios.luxdialoguesapi.Events.DialogueAnsweredEvent
import org.aselstudios.luxdialoguesapi.Events.DialogueStartEvent
import org.aselstudios.luxdialoguesapi.Events.DialogueStopEvent

// Listeners of the `on_lux_*` event entries.

@EntryListener(OnLuxDialogueStartEntry::class)
fun onLuxDialogueStart(event: DialogueStartEvent, query: Query<OnLuxDialogueStartEntry>) {
    val id = event.dialogue?.dialogueID ?: return
    query.findWhere { matchesDialogue(it.dialogueId, it.dialogue, id) }.triggerAllFor(event.player, context())
}

@EntryListener(OnLuxDialogueStopEntry::class)
fun onLuxDialogueStop(event: DialogueStopEvent, query: Query<OnLuxDialogueStopEntry>) {
    val id = event.dialogue?.dialogueID ?: return
    query.findWhere { matchesDialogue(it.dialogueId, it.dialogue, id) }.triggerAllFor(event.player, context())
}

@EntryListener(OnLuxAnswerEntry::class)
fun onLuxAnswer(event: DialogueAnsweredEvent, query: Query<OnLuxAnswerEntry>) {
    val dialogueId = event.dialogue?.dialogueID ?: return
    val answer = event.answer ?: return
    val answerId = answer.answerID ?: return
    val answerText = answer.answerText.orEmpty()

    query.findWhere {
        matchesDialogue(it.dialogueId, it.dialogue, dialogueId) && (it.answerId.isBlank() || it.answerId == answerId)
    }.triggerAllFor(event.player) {
        LuxAnswerEventContextKeys.DIALOGUE_ID withValue dialogueId
        LuxAnswerEventContextKeys.ANSWER_ID withValue answerId
        LuxAnswerEventContextKeys.ANSWER_TEXT withValue TextResolver.stripFormatting(answerText)
    }
}

/**
 * Whether an entry's filters accept [openDialogueId]. A panel [dialogue] is matched
 * through the registry, since generated ids are not known in advance.
 */
private fun matchesDialogue(dialogueId: String, dialogue: Ref<DialogueEntry>, openDialogueId: String): Boolean {
    if (dialogueId.isNotBlank() && dialogueId != openDialogueId) return false
    if (!dialogue.isSet) return true
    return LuxServices.registry.ownerOf(openDialogueId) == dialogue.id
}
