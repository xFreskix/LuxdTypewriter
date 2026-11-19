package io.github.xfreskix.luxdtypewriter.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entries.CinematicAction
import com.typewritermc.engine.paper.entry.entries.CinematicEntry
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import io.github.xfreskix.luxdtypewriter.style.LuxStyleEntry
import org.bukkit.entity.Player

/**
 * Shows LuxDialogues pages on a cinematic timeline, one page per segment. The
 * window closes when the cinematic ends, also when it is skipped.
 */
@Entry("lux_dialogue_cinematic", "Show LuxDialogues pages on a cinematic timeline", Colors.CYAN, "mdi:movie-open")
class LuxDialogueCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),

    @Help("The speaker whose name and portrait are shown.")
    val speaker: Ref<SpeakerEntry> = emptyRef(),

    @Help("The look and feel to render with. Leave empty for the default LuxDialogues style.")
    val style: Ref<LuxStyleEntry> = emptyRef(),

    @Segments(icon = "mdi:movie-open")
    val segments: List<LuxDialogueSegment> = emptyList(),
) : CinematicEntry {

    override fun create(player: Player): CinematicAction =
        LuxDialogueCinematicAction(player, speaker.get(), style, segments)
}
