package io.github.xfreskix.luxdtypewriter.entries.cinematic

import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.entries.Segment

/** Frames during which [text] is shown. */
data class LuxDialogueSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,

    @MultiLine
    @Placeholder
    @Colored
    @Help("The dialogue shown while this segment is active.")
    val text: String = "",
) : Segment
