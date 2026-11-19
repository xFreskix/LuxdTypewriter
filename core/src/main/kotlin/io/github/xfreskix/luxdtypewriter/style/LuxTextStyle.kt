package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Negative
import com.typewritermc.engine.paper.utils.Color

/** White means the stock colour. */
data class LuxTextStyle(
    val color: Color = Color.WHITE,
    @Negative
    @Help("Horizontal offset in pixels.")
    val offset: Int = 0,
)
