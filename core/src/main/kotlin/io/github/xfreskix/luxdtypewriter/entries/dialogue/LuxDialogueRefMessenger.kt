package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import io.github.xfreskix.luxdtypewriter.bridge.DialogueView
import io.github.xfreskix.luxdtypewriter.bridge.OpenResult
import io.github.xfreskix.luxdtypewriter.bridge.openAndConfirm
import io.github.xfreskix.luxdtypewriter.session.CompletionPolicy
import io.github.xfreskix.luxdtypewriter.session.LuxSession
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import org.bukkit.entity.Player

/**
 * Messenger of [LuxDialogueRefEntry]. YAML pages cannot be instrumented, so
 * completion follows [CompletionPolicy.ON_NATURAL_STOP].
 */
internal class LuxDialogueRefMessenger(
    player: Player,
    context: InteractionContext,
    entry: LuxDialogueRefEntry,
) : LuxMessenger<LuxDialogueRefEntry>(player, context, entry) {

    override fun acceptsInput(): Boolean = super.acceptsInput() && state == MessengerState.RUNNING

    override fun start() {
        val dialogueId = entry.dialogueId
        if (dialogueId.isBlank()) {
            LuxLogger.warnOnce(
                key = "ref-blank-id-${entry.id}",
                message = "lux_dialogue_ref ${entry.label} has no dialogue id, so its sequence was stopped.",
            )
            state = MessengerState.CANCELLED
            return
        }
        val startPage = entry.page.ifBlank { null }
        Dispatchers.UntickedAsync.launch { open(dialogueId, startPage) }
    }

    private suspend fun open(dialogueId: String, startPage: String?) {
        if (!bridge.hasDialogue(dialogueId)) {
            LuxLogger.warnOnce(
                key = "ref-missing-${entry.id}",
                message = "lux_dialogue_ref ${entry.label} points at LuxDialogues dialogue '$dialogueId', which " +
                    "does not exist, so its sequence was stopped.",
            )
            verdict = MessengerState.CANCELLED
            return
        }
        val session = LuxSession(player.uniqueId, dialogueId, CompletionPolicy.ON_NATURAL_STOP, this)
        this.session = session
        registry.open(session)

        val opened = bridge.openAndConfirm(player, dialogueId) { bridge.openById(player, dialogueId, startPage) }
        when {
            opened == OpenResult.OPENED && disposed -> bridge.clearIfShowing(player, session.dialogueId)
            opened == OpenResult.OPENED -> dialogueSent = true
            else -> {
                reportNotShown(opened, dialogueId)
                registry.release(session)
                session.markAborting()
                session.end()
            }
        }
    }

    private fun reportNotShown(opened: OpenResult, dialogueId: String) {
        when (opened) {
            OpenResult.NOT_SHOWN -> LuxLogger.warnOnce(
                key = "ref-never-shown-${entry.id}",
                message = "lux_dialogue_ref ${entry.label} asked LuxDialogues to open '$dialogueId' but it never " +
                    "showed, so its sequence was stopped. '/tw luxtw trace' shows each step.",
            )
            OpenResult.COOLDOWN -> LuxLogger.warnOnce(
                key = "ref-cooldown-${entry.id}",
                message = "lux_dialogue_ref ${entry.label} could not open '$dialogueId' while the player was on " +
                    "the LuxDialogues command cooldown, so its sequence was stopped.",
            )
            OpenResult.OPENED, OpenResult.UNAVAILABLE, OpenResult.FAILED -> Unit
        }
    }

    override fun followWindow(session: LuxSession) {
        if (bridge.view(player) != DialogueView.None) return
        registry.release(session)
        session.end()
    }

    override fun onEnded(session: LuxSession, completed: Boolean) {
        if (disposed) return
        verdict = if (completed) MessengerState.FINISHED else MessengerState.CANCELLED
    }

    override fun release(session: LuxSession) {
        if (!session.isActive) return
        Dispatchers.UntickedAsync.launch {
            // Not if a later entry already took the player over.
            if (registry.find(session.playerId) === session) closeSession(session)
        }
    }
}
