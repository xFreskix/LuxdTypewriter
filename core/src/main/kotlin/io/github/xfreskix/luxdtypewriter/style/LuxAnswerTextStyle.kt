package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Negative
import com.typewritermc.engine.paper.utils.Color

/** White means the stock colour. */
data class LuxAnswerTextStyle(
    @Help("Colour of answers that are not selected.")
    val color: Color = Color.WHITE,
    @Negative
    @Help("Horizontal offset in pixels.")
    val offset: Int = 0,
    @Help("Colour of the answer the player currently has highlighted.")
    val selectedColor: Color = Color.WHITE,
)
