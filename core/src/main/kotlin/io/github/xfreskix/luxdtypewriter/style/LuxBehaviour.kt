package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.extension.annotations.Help
import io.github.xfreskix.luxdtypewriter.text.TextFormat

data class LuxBehaviour(
    @Help("Ticks per typed character. 1 is the fastest.")
    val typingSpeed: Int = StockStyle.TYPING_SPEED,
    @Help("Blocks the player may walk away before the dialogue closes. 0 uses 5.")
    val range: Double = StockStyle.RANGE,
    @Help("LuxDialogues movement restriction. Prefer NONE with a Typewriter interaction bound.")
    val effect: LuxMovementEffect = LuxMovementEffect.NONE,
    @Help("Show a number in front of every answer.")
    val answerNumbers: Boolean = true,
    @Help("Stop the player from closing the dialogue manually.")
    val preventExit: Boolean = false,
    @Help("Stop the player from skipping the typing animation.")
    val preventSkip: Boolean = false,
    @Help("Block closing while the text types. INHERIT follows the page and preventExit.")
    val typingPreventExit: LuxOverride = LuxOverride.INHERIT,
    @Help("Block closing once the text is typed. INHERIT follows the page and preventExit.")
    val steadyPreventExit: LuxOverride = LuxOverride.INHERIT,
    @Help("Turn the player towards the speaking NPC when the dialogue opens.")
    val npcFocus: Boolean = false,
    @Help("Render every dialogue line in ꜱᴍᴀʟʟ ᴄᴀᴘꜱ automatically. MiniMessage tags are left intact.")
    val smallCaps: Boolean = false,
    @Help("Characters per dialogue line before it wraps. 0 uses 34.")
    val lineMaxLength: Int = StockStyle.LINE_MAX_LENGTH,
    @Help("Rows of the dialogue box; more text continues on the next page. 0 reads the pack's lines.yml.")
    val dialogueLines: Int = 0,
    @Help("Answers shown at once; more are paged with 'More options'. 0 reads the pack's lines.yml.")
    val answerLines: Int = 0,
    @Help("Characters per answer before it is shortened. 0 uses lineMaxLength.")
    val answerMaxLength: Int = 0,
    @Help("Ticks a single message stays after typing. 0 uses 60, -1 waits for a click.")
    val messageHoldTicks: Int = StockStyle.MESSAGE_HOLD_TICKS,
    @Help(
        "TYPING_SAFE keeps colours without tags showing while typing (no bold or italic). " +
            "MINI_MESSAGE sends text as written. PLAIN strips formatting.",
    )
    val textFormat: TextFormat = TextFormat.TYPING_SAFE,
)
