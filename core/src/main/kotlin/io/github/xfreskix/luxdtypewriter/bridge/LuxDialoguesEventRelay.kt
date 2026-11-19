package io.github.xfreskix.luxdtypewriter.bridge

import com.typewritermc.core.extension.annotations.Singleton
import org.aselstudios.luxdialoguesapi.Events.DialogueAnsweredEvent
import org.aselstudios.luxdialoguesapi.Events.DialogueGoToEvent
import org.aselstudios.luxdialoguesapi.Events.DialoguePageChangeEvent
import org.aselstudios.luxdialoguesapi.Events.DialogueStartEvent
import org.aselstudios.luxdialoguesapi.Events.DialogueStopEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/** The only listener of LuxDialogues' events; forwards them to the [DialogueEventSink]. */
@Singleton
class LuxDialoguesEventRelay(
    private val sink: DialogueEventSink,
    private val tracker: LuxDialoguesTracker,
) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDialogueStart(event: DialogueStartEvent) {
        val dialogueId = event.dialogue?.dialogueID ?: return
        sink.onStarted(event.player.uniqueId, dialogueId)
        tracker.watch(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPageChange(event: DialoguePageChangeEvent) {
        val dialogueId = event.dialogue?.dialogueID ?: return
        val pageId = event.newPage?.id ?: return
        sink.onPageChanged(event.player.uniqueId, dialogueId, pageId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onAnswered(event: DialogueAnsweredEvent) {
        val dialogueId = event.dialogue?.dialogueID ?: return
        val answerId = event.answer?.answerID ?: return
        sink.onAnswered(event.player.uniqueId, dialogueId, answerId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onGoTo(event: DialogueGoToEvent) {
        val dialogueId = event.newDialogue?.dialogueID ?: return
        sink.onRedirected(event.player.uniqueId, event.oldDialogue?.dialogueID, dialogueId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDialogueStop(event: DialogueStopEvent) {
        val dialogueId = event.dialogue?.dialogueID ?: return
        sink.onStopped(event.player.uniqueId, dialogueId)
    }
}
