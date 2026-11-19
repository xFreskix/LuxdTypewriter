package io.github.xfreskix.luxdtypewriter.session

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

/**
 * One player's dialogue. State changes use compare-and-set, so concurrent
 * [end] calls notify [LuxSessionListener.onEnded] exactly once.
 *
 * @param dialogueId the LuxDialogues dialogue this session owns.
 */
class LuxSession(
    val playerId: UUID,
    dialogueId: String,
    val policy: CompletionPolicy,
    private val listener: LuxSessionListener,
) {
    private val stateRef = AtomicReference(LuxSessionState.OPENING)

    val state: LuxSessionState get() = stateRef.get()

    val isActive: Boolean get() = state != LuxSessionState.ENDED

    /** Changes only when a YAML dialogue redirects into another one. */
    @Volatile
    var dialogueId: String = dialogueId
        private set

    @Volatile
    var currentPageId: String? = null
        private set

    @Volatile
    var lastAnswerId: String? = null
        private set

    /** Last page change or answer; read by `lux_in_dialogue_activity`. */
    @Volatile
    var lastProgressAt: Long = System.currentTimeMillis()
        private set

    /** Set when LuxdTypewriter closed the window itself. */
    @Volatile
    private var closeRequestedByUs: Boolean = false

    /** Forwards a click on the speaker NPC to the session owner. */
    fun click(select: Boolean) {
        if (isActive) listener.onClick(this, select)
    }

    /** No-op unless [LuxSessionState.OPENING]. */
    fun markStarted() {
        if (stateRef.compareAndSet(LuxSessionState.OPENING, LuxSessionState.RUNNING)) {
            touch()
            listener.onStarted(this)
        }
    }

    fun markPageChanged(pageId: String) {
        // A page change can arrive before the start event.
        markStarted()
        if (state != LuxSessionState.RUNNING) return
        currentPageId = pageId
        touch()
        listener.onPageChanged(this, pageId)
    }

    fun markAnswered(answerId: String) {
        if (state != LuxSessionState.RUNNING) return
        lastAnswerId = answerId
        touch()
        listener.onAnswered(this, answerId)
    }

    /**
     * YAML dialogues may chain into each other, so the session follows them. A
     * generated dialogue never redirects itself: leaving it means it was replaced.
     */
    fun markRedirected(fromDialogueId: String?, toDialogueId: String): RedirectOutcome {
        if (toDialogueId == dialogueId || fromDialogueId != dialogueId) return RedirectOutcome.IGNORE
        if (policy != CompletionPolicy.ON_NATURAL_STOP) return RedirectOutcome.ABORT
        dialogueId = toDialogueId
        touch()
        return RedirectOutcome.FOLLOW
    }

    /** The player reached an ending; [end] follows once the window closes. */
    fun markCompleting() {
        stateRef.compareAndSet(LuxSessionState.OPENING, LuxSessionState.COMPLETING)
        stateRef.compareAndSet(LuxSessionState.RUNNING, LuxSessionState.COMPLETING)
    }

    /** Never downgrades a session that is already [LuxSessionState.COMPLETING]. */
    fun markAborting() {
        stateRef.compareAndSet(LuxSessionState.OPENING, LuxSessionState.ABORTING)
        stateRef.compareAndSet(LuxSessionState.RUNNING, LuxSessionState.ABORTING)
    }

    fun markCloseRequested() {
        closeRequestedByUs = true
    }

    /** Ends the session; only the first call notifies. Returns whether this call ended it. */
    fun end(): Boolean {
        val previous = stateRef.getAndSet(LuxSessionState.ENDED)
        if (previous == LuxSessionState.ENDED) return false
        listener.onEnded(this, resolveCompleted(previous))
        return true
    }

    private fun resolveCompleted(previous: LuxSessionState): Boolean = when (previous) {
        LuxSessionState.COMPLETING -> true
        LuxSessionState.ABORTING -> false
        // LuxDialogues closed the window itself: the only "finished" signal of a
        // YAML dialogue, an abort for a generated one.
        else -> policy == CompletionPolicy.ON_NATURAL_STOP && !closeRequestedByUs
    }

    private fun touch() {
        lastProgressAt = System.currentTimeMillis()
    }

    override fun toString(): String =
        "LuxSession(player=$playerId, dialogue='$dialogueId', state=$state, page=$currentPageId)"
}
