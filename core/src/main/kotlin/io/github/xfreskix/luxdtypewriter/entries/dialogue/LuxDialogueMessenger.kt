package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.entries.EventTrigger
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.DialogueView
import io.github.xfreskix.luxdtypewriter.bridge.OpenResult
import io.github.xfreskix.luxdtypewriter.bridge.openAndConfirm
import io.github.xfreskix.luxdtypewriter.build.BuiltDialogue
import io.github.xfreskix.luxdtypewriter.build.DialogueCallbacks
import io.github.xfreskix.luxdtypewriter.build.PageIds
import io.github.xfreskix.luxdtypewriter.session.CompletionPolicy
import io.github.xfreskix.luxdtypewriter.session.LuxSession
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.milliseconds

/**
 * Messenger of generated dialogues (`lux_dialogue`, `lux_message`, `lux_option`).
 * The factory's [DialogueCallbacks] report every ending ([CompletionPolicy.EXPLICIT]).
 */
class LuxDialogueMessenger<E : LuxDialogueSource>(
    player: Player,
    context: InteractionContext,
    entry: E,
) : LuxMessenger<E>(player, context, entry), DialogueCallbacks {

    private val factory by lazy { LuxServices.factory }

    @Volatile
    private var built: BuiltDialogue? = null

    @Volatile
    private var lastPageReached = 0

    @Volatile
    private var chosenAnswer: LuxAnswer? = null

    override val eventTriggers: List<EventTrigger>
        get() = entry.eventTriggers + chosenAnswer?.eventTriggers.orEmpty()

    override val modifiers: List<Modifier>
        get() = entry.modifiers + chosenAnswer?.modifiers.orEmpty()

    override fun acceptsInput(): Boolean = super.acceptsInput() && chosenAnswer == null

    override fun start() {
        val dialogueId = registry.generateDialogueId()
        Dispatchers.UntickedAsync.launch { open(dialogueId) }
    }

    private suspend fun open(dialogueId: String) {
        val session = LuxSession(player.uniqueId, dialogueId, CompletionPolicy.EXPLICIT, this)
        this.session = session

        val result = runCatching {
            factory.build(entry, player, context, dialogueId, entry.speakerDisplayName.get(player, context), this)
        }.getOrElse { failure ->
            LuxLogger.error("Lux dialogue ${entry.label} could not be built, so its sequence was stopped.", failure)
            verdict = MessengerState.CANCELLED
            return
        }
        built = result
        registry.claim(dialogueId, entry.id)
        registry.open(session)

        val opened = bridge.openAndConfirm(player, dialogueId) { bridge.open(player, result.blueprint, PageIds.FIRST) }
        when {
            // The sequence moved on while the window was opening.
            opened == OpenResult.OPENED && disposed -> bridge.clearIfShowing(player, dialogueId)
            opened != OpenResult.OPENED -> {
                reportNotShown(opened)
                registry.forget(dialogueId)
                registry.release(session)
                session.markAborting()
                session.end()
            }
            else -> {
                dialogueSent = true
                if (result.blueprint.style.behaviour.npcFocus) {
                    val focused = bridge.focusIfShowing(player, dialogueId) { speakerEyeLocation(player) }
                    LuxLogger.trace(player) { "focus: '$dialogueId' focused=$focused" }
                }
            }
        }
    }

    private fun reportNotShown(opened: OpenResult) {
        when (opened) {
            OpenResult.NOT_SHOWN -> LuxLogger.warnOnce(
                key = "never-shown-${entry.id}",
                message = "LuxDialogues accepted Lux dialogue ${entry.label} but never showed it, so its sequence " +
                    "was stopped. Another plugin may own the dialogue window; '/tw luxtw trace' shows each step.",
            )
            OpenResult.COOLDOWN -> LuxLogger.warnOnce(
                key = "cooldown-${entry.id}",
                message = "Lux dialogue ${entry.label} could not open while the player was on the LuxDialogues " +
                    "command cooldown, so its sequence was stopped. Lower 'command-cooldown' in the LuxDialogues config.",
            )
            // Already reported by the bridge.
            OpenResult.OPENED, OpenResult.UNAVAILABLE, OpenResult.FAILED -> Unit
        }
    }

    override fun followWindow(session: LuxSession) {
        when {
            chosenAnswer != null -> {
                registry.release(session)
                session.markCompleting()
                session.end()
            }
            bridge.view(player) == DialogueView.None -> {
                if (reachedTheEnd()) session.markCompleting() else session.markAborting()
                registry.release(session)
                session.end()
            }
        }
    }

    private fun reachedTheEnd(): Boolean {
        val built = built ?: return false
        if (built.answers.isNotEmpty()) return false
        return lastPageReached == 0 || lastPageReached >= built.pageCount
    }

    override fun release(session: LuxSession) {
        Dispatchers.UntickedAsync.launch {
            // Gives the next entry a moment to take over the open window.
            delay(HANDOVER_GRACE)
            closeSession(session)
        }
    }

    override fun onAnswerChosen(index: Int, answer: LuxAnswer) {
        if (disposed || chosenAnswer != null) return
        val result = checkNotNull(built) { "Lux dialogue '${entry.id}' reported an answer before it was built." }
        context[entry, LuxDialogueContextKeys.SELECTED_ANSWER] = index
        context[entry, LuxDialogueContextKeys.SELECTED_ANSWER_TEXT] = result.answerTexts[index - 1]
        chosenAnswer = answer
        session?.markAnswered(index.toString())
    }

    override fun onReachedEnding() {
        session?.markCompleting()
    }

    override fun onPageReached(renderedPage: Int, authoredPage: Int) {
        if (disposed) return
        if (renderedPage > lastPageReached) lastPageReached = renderedPage
        context[entry, LuxDialogueContextKeys.LAST_PAGE] = authoredPage
        session?.markPageChanged(PageIds.of(renderedPage))
    }

    override fun onEnded(session: LuxSession, completed: Boolean) {
        LuxLogger.trace(player) { "ended: completed=$completed answered=${chosenAnswer != null} disposed=$disposed" }
        if (disposed) return
        verdict = if (!completed && chosenAnswer == null) MessengerState.CANCELLED else MessengerState.FINISHED
    }

    private companion object {
        val HANDOVER_GRACE = 100.milliseconds
    }
}
