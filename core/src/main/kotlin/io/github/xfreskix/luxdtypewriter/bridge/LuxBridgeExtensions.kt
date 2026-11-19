package io.github.xfreskix.luxdtypewriter.bridge

import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import kotlin.time.Duration.Companion.milliseconds

private const val COOLDOWN_RETRIES = 20
private val COOLDOWN_RETRY_DELAY = 50.milliseconds
private val SHOW_CONFIRM_DELAY = 100.milliseconds
private val NATIVE_INPUT_GRACE = 100.milliseconds

/**
 * Retries [open] for up to a second while LuxDialogues reports its command
 * cooldown, which two chained dialogues always hit.
 */
suspend fun retryWhileOnCooldown(open: suspend () -> OpenResult): OpenResult {
    repeat(COOLDOWN_RETRIES) {
        val result = open()
        if (result != OpenResult.COOLDOWN) return result
        delay(COOLDOWN_RETRY_DELAY)
    }
    return OpenResult.COOLDOWN
}

/**
 * Opens a dialogue and checks it reached the screen: LuxDialogues may accept a
 * call and show nothing, which would leave the sequence waiting forever.
 */
suspend fun LuxBridge.openAndConfirm(
    player: Player,
    dialogueId: String,
    open: suspend () -> OpenResult,
): OpenResult {
    val opened = retryWhileOnCooldown(open)
    if (opened != OpenResult.OPENED) return opened
    if (view(player).isShowing(dialogueId)) return OpenResult.OPENED
    delay(SHOW_CONFIRM_DELAY)
    if (refreshView(player).isShowing(dialogueId)) return OpenResult.OPENED
    LuxLogger.trace(player) { "open: '$dialogueId' was accepted but is not on screen" }
    return OpenResult.NOT_SHOWN
}

/**
 * Forwards a click after LuxDialogues had a moment to handle it itself; if it
 * already did, the view no longer matches [seen] and nothing happens.
 */
suspend fun LuxBridge.interactAfterNativeInput(
    player: Player,
    dialogueId: String,
    seen: DialogueView.Open,
    guard: () -> Boolean,
): Boolean {
    val pageId = seen.pageId ?: return false
    delay(NATIVE_INPUT_GRACE)
    return interactIfUnchanged(player, dialogueId, pageId, seen.typing, guard)
}

private fun DialogueView.isShowing(dialogueId: String): Boolean =
    this is DialogueView.Open && this.dialogueId == dialogueId
