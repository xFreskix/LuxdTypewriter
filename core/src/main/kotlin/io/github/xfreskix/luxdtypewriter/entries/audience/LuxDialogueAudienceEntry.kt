package io.github.xfreskix.luxdtypewriter.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.AudienceFilter
import com.typewritermc.engine.paper.entry.entries.AudienceFilterEntry
import com.typewritermc.engine.paper.entry.entries.Invertible

/**
 * Keeps players who are in a LuxDialogues dialogue (or, inverted, who are not),
 * e.g. to hide a sidebar while talking.
 */
@Entry("lux_dialogue_audience", "Players who are in a LuxDialogues dialogue", Colors.MEDIUM_SEA_GREEN, "mdi:account-voice")
class LuxDialogueAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    override val children: List<Ref<out AudienceEntry>> = emptyList(),

    @Help("Only include players who are in this dialogue id. Leave empty for any dialogue.")
    val dialogueId: String = "",

    override val inverted: Boolean = false,
) : AudienceFilterEntry, Invertible {

    override suspend fun display(): AudienceFilter = LuxDialogueAudienceFilter(ref(), dialogueId)
}
