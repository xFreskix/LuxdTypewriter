package io.github.xfreskix.luxdtypewriter.preview

import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import io.github.xfreskix.luxdtypewriter.bridge.LuxBridge
import io.github.xfreskix.luxdtypewriter.bridge.OpenResult
import io.github.xfreskix.luxdtypewriter.build.DialogueCallbacks
import io.github.xfreskix.luxdtypewriter.build.LuxDialogueFactory
import io.github.xfreskix.luxdtypewriter.build.PageIds
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxAnswer
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxDialogueSource
import io.github.xfreskix.luxdtypewriter.session.LuxSessionRegistry
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import org.bukkit.entity.Player

/** Shows a dialogue as players see it, without triggers, facts or a session. */
@Singleton
class PreviewSandbox(
    private val factory: LuxDialogueFactory,
    private val bridge: LuxBridge,
    private val registry: LuxSessionRegistry,
) {
    suspend fun preview(source: LuxDialogueSource, player: Player, speakerName: String): PreviewResult {
        if (!bridge.isAvailable) return PreviewResult.LUX_UNAVAILABLE
        if (registry.find(player)?.isActive == true) return PreviewResult.IN_CONVERSATION

        val dialogueId = registry.generateDialogueId()
        val built = runCatching {
            factory.build(source, player, null, dialogueId, speakerName, ClosingCallbacks(player, dialogueId))
        }.getOrElse { failure ->
            LuxLogger.error("The preview of Lux dialogue '${source.name}' (${source.id}) could not be built.", failure)
            return PreviewResult.FAILED
        }
        return when (bridge.open(player, built.blueprint, PageIds.FIRST)) {
            OpenResult.OPENED -> PreviewResult.OPENED
            OpenResult.COOLDOWN -> PreviewResult.COOLDOWN
            OpenResult.UNAVAILABLE -> PreviewResult.LUX_UNAVAILABLE
            OpenResult.NOT_SHOWN, OpenResult.FAILED -> PreviewResult.FAILED
        }
    }

    /** Picking an answer just closes the preview. */
    private inner class ClosingCallbacks(
        private val player: Player,
        private val dialogueId: String,
    ) : DialogueCallbacks {
        override fun onAnswerChosen(index: Int, answer: LuxAnswer) {
            Dispatchers.UntickedAsync.launch { bridge.clearIfShowing(player, dialogueId) }
        }

        override fun onReachedEnding() = Unit
    }
}
