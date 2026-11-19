package io.github.xfreskix.luxdtypewriter.bridge

/** What LuxDialogues shows a player, as last read by [LuxDialoguesTracker]. */
sealed interface DialogueView {
    data object None : DialogueView

    /** LuxDialogues threw while being read. */
    data object Unknown : DialogueView

    /**
     * @param pageId `null` while LuxDialogues is between pages.
     * @param selectedAnswer 1-based highlighted answer, 0 for none.
     */
    data class Open(
        val dialogueId: String,
        val pageId: String?,
        val pageHasAnswers: Boolean,
        val typing: Boolean,
        val selectedAnswer: Int,
    ) : DialogueView
}

/** Whether the dialogue or page changed; typing and selection changes do not count. */
internal fun hasMoved(previous: DialogueView, current: DialogueView): Boolean =
    if (previous is DialogueView.Open && current is DialogueView.Open) {
        previous.dialogueId != current.dialogueId || previous.pageId != current.pageId
    } else {
        previous != current
    }
