package io.github.xfreskix.luxdtypewriter.bridge

import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.utils.switchContext
import com.typewritermc.engine.paper.utils.Sync
import io.github.xfreskix.luxdtypewriter.build.DialogueBlueprint
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import org.aselstudios.luxdialoguesapi.DialogueProvider
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * [LuxBridge] backed by the LuxDialogues API. Writes run on the main thread;
 * every failure is logged and turned into a neutral result.
 */
@Singleton
class LuxDialoguesBridge(
    private val connection: LuxDialoguesConnection,
    private val tracker: LuxDialoguesTracker,
) : LuxBridge {

    override val availability: LuxAvailability get() = connection.availability

    override suspend fun open(player: Player, dialogue: DialogueBlueprint, pageId: String): OpenResult =
        onMainThread(player, "open", OpenResult.UNAVAILABLE, OpenResult.FAILED) { provider ->
            val luxDialogue = dialogue.toLuxDialogue()
            when {
                provider.isInDialogue(player) == true -> {
                    provider.redirectDialogue(player, luxDialogue, pageId)
                    OpenResult.OPENED
                }
                provider.isInCommandCooldown(player) == true -> OpenResult.COOLDOWN
                else -> {
                    tracker.watch(player)
                    provider.sendDialogue(player, luxDialogue, pageId)
                    OpenResult.OPENED
                }
            }
        }

    override suspend fun openById(player: Player, dialogueId: String, pageId: String?): OpenResult =
        onMainThread(player, "openById", OpenResult.UNAVAILABLE, OpenResult.FAILED) { provider ->
            when {
                provider.isInDialogue(player) == true -> {
                    provider.redirectDialogue(player, dialogueId, redirectPage(provider, dialogueId, pageId))
                    OpenResult.OPENED
                }
                provider.isInCommandCooldown(player) == true -> OpenResult.COOLDOWN
                else -> {
                    tracker.watch(player)
                    if (pageId == null) provider.sendDialogue(player, dialogueId)
                    else provider.sendDialogue(player, dialogueId, pageId)
                    OpenResult.OPENED
                }
            }
        }

    override suspend fun clear(player: Player): Boolean =
        onMainThread(player, "clear", false, false) { provider ->
            provider.clearDialogue(player)
            true
        }

    override suspend fun clearIfShowing(player: Player, dialogueId: String): Boolean =
        onMainThread(player, "clearIfShowing", false, false) { provider ->
            if (provider.getDialogue(player)?.dialogueID == dialogueId) provider.clearDialogue(player)
            true
        }

    override suspend fun advance(player: Player): Boolean =
        onMainThread(player, "advance", false, false) { provider ->
            provider.triggerInteraction(player)
            true
        }

    override suspend fun interactIfUnchanged(
        player: Player,
        dialogueId: String,
        pageId: String,
        typing: Boolean,
        guard: () -> Boolean,
    ): Boolean = onMainThread(player, "interactIfUnchanged", false, false) { provider ->
        val unchanged = guard() &&
            provider.getDialogue(player)?.dialogueID == dialogueId &&
            provider.getCurrentPage(player)?.id == pageId &&
            (provider.isTyping(player) == true) == typing
        if (unchanged) provider.triggerInteraction(player)
        unchanged
    }

    override suspend fun focusIfShowing(player: Player, dialogueId: String, target: () -> Location?): Boolean =
        onMainThread(player, "focusIfShowing", false, false) { provider ->
            if (provider.getDialogue(player)?.dialogueID != dialogueId) return@onMainThread false
            val location = target() ?: return@onMainThread false
            provider.focusLocation(player, location)
            true
        }

    override fun view(player: Player): DialogueView = tracker.view(player)

    override suspend fun refreshView(player: Player): DialogueView =
        Dispatchers.Sync.switchContext { tracker.refresh(player) }

    override fun hasDialogue(dialogueId: String): Boolean =
        query("hasDialogue", false) { it.getDialogueById(dialogueId) != null }

    override fun hasPage(dialogueId: String, pageId: String): Boolean =
        query("hasPage", false) { provider ->
            provider.getDialogueById(dialogueId)?.pages?.values?.any { it.id == pageId } == true
        }

    /** A redirect needs an explicit page: LuxDialogues resolves `start-page` only on a fresh open. */
    private fun redirectPage(provider: DialogueProvider, dialogueId: String, pageId: String?): String {
        if (pageId != null) return pageId
        val dialogue = provider.getDialogueById(dialogueId)
            ?: error("LuxDialogues has no dialogue '$dialogueId' to redirect to.")
        return unconditionalStartPage(dialogueId, dialogue.startPage, dialogue.pages.values.map { it.id })
    }

    private suspend inline fun <T> onMainThread(
        player: Player,
        operation: String,
        unavailable: T,
        failed: T,
        crossinline block: (DialogueProvider) -> T,
    ): T {
        val provider = connection.provider ?: run {
            LuxLogger.warnLuxUnavailable()
            return unavailable
        }
        return Dispatchers.Sync.switchContext {
            val result = runCatching { block(provider) }.getOrElse { failure ->
                LuxLogger.error("LuxDialogues threw while performing '$operation'.", failure)
                failed
            }
            tracker.refresh(player)
            result
        }
    }

    private inline fun <T> query(operation: String, fallback: T, block: (DialogueProvider) -> T): T {
        val provider = connection.provider ?: run {
            LuxLogger.warnLuxUnavailable()
            return fallback
        }
        return runCatching { block(provider) }.getOrElse { failure ->
            LuxLogger.error("LuxDialogues threw while reading '$operation'.", failure)
            fallback
        }
    }
}
