package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.extension.annotations.Colored
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.MultiLine
import com.typewritermc.core.extension.annotations.Placeholder
import com.typewritermc.engine.paper.entry.entries.Var
import io.github.xfreskix.luxdtypewriter.style.LuxOverride

data class LuxPage(
    @MultiLine
    @Placeholder
    @Colored
    @Help("What the speaker says. Long lines wrap to fit the box.")
    val lines: List<Var<String>> = emptyList(),

    @Placeholder
    @Colored
    @Help("Hint shown while the text types out. Leave empty to use the style's default.")
    val typingLine: String = "",

    @Placeholder
    @Colored
    @Help("Hint shown once the text has finished typing. Leave empty to use the style's default.")
    val steadyLine: String = "",

    @Help("Ticks to wait after typing before moving on. 0 waits for the player.")
    val autoAdvanceTicks: Int = 0,

    @Help("Block closing the dialogue on this page. INHERIT follows the style.")
    val preventExit: LuxOverride = LuxOverride.INHERIT,

    @Help("Block skipping the typing on this page. INHERIT follows the style.")
    val preventSkip: LuxOverride = LuxOverride.INHERIT,

    @Help("Choices on this page. Any number; extra ones get a 'More options' answer.")
    val answers: List<LuxAnswer> = emptyList(),
) {
    val isQuestion: Boolean get() = answers.isNotEmpty()
}
