package io.github.xfreskix.luxdtypewriter.entries.cinematic

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.entries.CinematicAction
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.entries.activeSegmentAt
import com.typewritermc.engine.paper.entry.entries.canFinishAt
import com.typewritermc.engine.paper.interaction.startBlockingActionBar
import com.typewritermc.engine.paper.interaction.stopBlockingActionBar
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.OpenResult
import io.github.xfreskix.luxdtypewriter.build.PageIds
import io.github.xfreskix.luxdtypewriter.build.SimpleDialogueBuilder
import io.github.xfreskix.luxdtypewriter.style.LuxStyleEntry
import io.github.xfreskix.luxdtypewriter.style.StyleResolver
import io.github.xfreskix.luxdtypewriter.text.TextResolver
import org.bukkit.entity.Player

/** Rebuilds the dialogue only when the active segment changes. */
internal class LuxDialogueCinematicAction(
    private val player: Player,
    private val speaker: SpeakerEntry?,
    private val style: Ref<LuxStyleEntry>,
    private val segments: List<LuxDialogueSegment>,
) : CinematicAction {

    private val bridge get() = LuxServices.bridge

    /** One id for all segments, so they reuse the open window. */
    private val dialogueId: String by lazy { LuxServices.registry.generateDialogueId() }

    private var activeSegment: LuxDialogueSegment? = null
    private var windowOpen = false

    override suspend fun tick(frame: Int) {
        val segment = segments activeSegmentAt frame
        // Cinematics block the action bar LuxDialogues renders through.
        if (windowOpen) player.stopBlockingActionBar()
        // Between segments the window stays; teardown closes it.
        if (segment == null || segment == activeSegment) return

        val dialogue = SimpleDialogueBuilder.singlePage(
            dialogueId = dialogueId,
            style = StyleResolver.resolve(style),
            speakerName = TextResolver.resolve(speaker?.displayName?.get(player).orEmpty(), player),
            line = TextResolver.resolve(segment.text, player),
        )
        val opened = bridge.open(player, dialogue, PageIds.FIRST)
        // Retried on the next frame.
        if (opened == OpenResult.COOLDOWN) return
        activeSegment = segment
        if (!windowOpen && opened == OpenResult.OPENED) {
            windowOpen = true
            player.stopBlockingActionBar()
        }
    }

    override suspend fun teardown() {
        if (windowOpen) {
            bridge.clearIfShowing(player, dialogueId)
            windowOpen = false
            player.startBlockingActionBar()
        }
        activeSegment = null
    }

    override fun canFinish(frame: Int): Boolean = segments canFinishAt frame
}
