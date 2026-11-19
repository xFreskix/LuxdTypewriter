package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.entry.dialogue.DialogueMessenger
import com.typewritermc.engine.paper.entry.dialogue.MessengerState
import com.typewritermc.engine.paper.entry.dialogue.TickContext
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.interaction.startBlockingActionBar
import com.typewritermc.engine.paper.interaction.stopBlockingActionBar
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.ClickIntent
import io.github.xfreskix.luxdtypewriter.bridge.DialogueView
import io.github.xfreskix.luxdtypewriter.bridge.LuxBridge
import io.github.xfreskix.luxdtypewriter.bridge.clickIntent
import io.github.xfreskix.luxdtypewriter.bridge.interactAfterNativeInput
import io.github.xfreskix.luxdtypewriter.session.LuxSession
import io.github.xfreskix.luxdtypewriter.session.LuxSessionListener
import io.github.xfreskix.luxdtypewriter.session.LuxSessionRegistry
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import org.bukkit.entity.Player

/**
 * Shared base of both Lux messengers.
 *
 * - Action bar: LuxDialogues renders through it and Typewriter blocks it, so the
 *   block is lifted every tick and restored in [dispose] unless the interaction ended.
 * - Input: NPC clicks come from `LuxInteractionListener`; LuxDialogues performs them.
 * - State may only change on the main thread, so [verdict] is applied in [tick].
 */
abstract class LuxMessenger<E : DialogueEntry>(
    player: Player,
    context: InteractionContext,
    entry: E,
) : DialogueMessenger<E>(player, context, entry), LuxSessionListener {

    protected val bridge: LuxBridge by lazy { LuxServices.bridge }
    protected val registry: LuxSessionRegistry by lazy { LuxServices.registry }

    @Volatile
    protected var session: LuxSession? = null

    @Volatile
    protected var dialogueSent = false

    @Volatile
    protected var disposed = false

    @Volatile
    protected var verdict: MessengerState? = null

    /** The interaction ended, as opposed to handing over to the next entry. */
    private var endRan = false

    /** Once the interaction listener delivers clicks, [animationComplete] is ignored. */
    @Volatile
    private var listenerOwnsInput = false

    /** Typewriter's "next" signal: late and without a button, so only a fallback. */
    override var animationComplete: Boolean
        get() = false
        set(value) {
            if (value && !listenerOwnsInput) forwardClick(select = true)
        }

    override fun init() {
        super.init()
        if (!bridge.isAvailable) {
            LuxLogger.warnLuxUnavailable()
            state = MessengerState.CANCELLED
            return
        }
        start()
    }

    /** Called once LuxDialogues is known to be available. */
    protected abstract fun start()

    protected open fun acceptsInput(): Boolean = !disposed

    override fun onClick(session: LuxSession, select: Boolean) {
        if (session !== this.session) return
        listenerOwnsInput = true
        forwardClick(select)
    }

    private fun forwardClick(select: Boolean) {
        val session = session
        val view = bridge.view(player)
        val intent = clickIntent(view, select)
        LuxLogger.trace(player) { "click: select=$select intent=$intent sent=$dialogueSent disposed=$disposed" }
        if (!dialogueSent || session == null || !acceptsInput()) return
        if (intent == ClickIntent.NONE || view !is DialogueView.Open) return

        Dispatchers.UntickedAsync.launch {
            val forwarded = bridge.interactAfterNativeInput(player, session.dialogueId, view) { acceptsInput() }
            LuxLogger.trace(player) {
                "click: '${session.dialogueId}' page=${view.pageId} intent=$intent forwarded=$forwarded"
            }
        }
    }

    override fun tick(context: TickContext) {
        super.tick(context)
        player.stopBlockingActionBar()
        if (state != MessengerState.RUNNING) return
        val session = session
        if (dialogueSent && session != null) followWindow(session)
        verdict?.let { state = it }
    }

    /** Ends [session] when the conversation is over. */
    protected abstract fun followWindow(session: LuxSession)

    override fun end() {
        endRan = true
        super.end()
    }

    override fun dispose() {
        super.dispose()
        disposed = true
        if (!endRan) player.startBlockingActionBar()
        session?.let { release(it) }
    }

    /** Called from [dispose]; must leave a window the next entry took over. */
    protected abstract fun release(session: LuxSession)

    /** Ends [session] as aborted by us and closes its window if still shown. */
    protected suspend fun closeSession(session: LuxSession) {
        registry.release(session)
        session.markCloseRequested()
        session.markAborting()
        session.end()
        bridge.clearIfShowing(player, session.dialogueId)
    }
}
