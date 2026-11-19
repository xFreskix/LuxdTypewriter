package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Negative
import com.typewritermc.engine.paper.utils.Color

/** The plate behind the speaker's name, in three pieces. */
data class LuxNamePlate(
    @Help("Left cap image id.")
    val startImage: String = StockStyle.NAME_START_IMAGE,
    @Help("Repeating middle image id.")
    val midImage: String = StockStyle.NAME_MID_IMAGE,
    @Help("Right cap image id.")
    val endImage: String = StockStyle.NAME_END_IMAGE,
    @Help("Tint applied to the whole name plate.")
    val color: Color = Color.WHITE,
    @Negative
    @Help("Horizontal offset in pixels.")
    val offset: Int = StockStyle.NAME_PLATE_OFFSET,
)
