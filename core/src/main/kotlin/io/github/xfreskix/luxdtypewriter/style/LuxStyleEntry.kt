package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.StaticEntry
import com.typewritermc.engine.paper.utils.Color

/** Reusable look of LuxDialogues windows, e.g. one per kind of NPC. */
@Tags("lux_style")
@Entry("lux_style", "Reusable look and feel for LuxDialogues windows", Colors.MEDIUM_PURPLE, "mdi:palette")
class LuxStyleEntry(
    override val id: String = "",
    override val name: String = "",

    @Help("The main dialogue box behind the text.")
    val background: LuxImageLayer = LuxImageLayer(
        StockStyle.BACKGROUND_IMAGE, Color(StockStyle.PARCHMENT), StockStyle.BACKGROUND_OFFSET,
    ),

    @Help("The box drawn behind the list of answers.")
    val answerBackground: LuxImageLayer = LuxImageLayer(
        StockStyle.ANSWER_BACKGROUND_IMAGE, Color(StockStyle.PARCHMENT), StockStyle.ANSWER_BACKGROUND_OFFSET,
    ),

    @Help("Portrait of the speaker. Hidden unless an image is set.")
    val character: LuxImageLayer = LuxImageLayer("", Color.WHITE, StockStyle.CHARACTER_OFFSET),

    @Help("Pointer drawn next to the highlighted answer.")
    val arrow: LuxImageLayer = LuxImageLayer(StockStyle.ARROW_IMAGE, Color(StockStyle.LIME), StockStyle.ARROW_OFFSET),

    @Help("The plate behind the speaker's name.")
    val namePlate: LuxNamePlate = LuxNamePlate(),

    @Help("Overlay that dims the world behind the dialogue.")
    val fog: LuxImageLayer = LuxImageLayer(StockStyle.FOG_IMAGE, Color(StockStyle.BLACK), 0),

    @Help("Colour and position of the dialogue text itself.")
    val dialogueText: LuxTextStyle = LuxTextStyle(Color(StockStyle.INK), StockStyle.DIALOGUE_TEXT_OFFSET),

    @Help("Colour and position of the answer list.")
    val answerText: LuxAnswerTextStyle = LuxAnswerTextStyle(
        Color(StockStyle.INK), StockStyle.ANSWER_TEXT_OFFSET, Color(StockStyle.INK),
    ),

    @Help("Colour and position of the speaker's name.")
    val characterNameText: LuxTextStyle = LuxTextStyle(Color(StockStyle.INK), 0),

    @Help("Colour and position of the hint line, e.g. 'Right click to continue'.")
    val informationText: LuxTextStyle = LuxTextStyle(Color(StockStyle.INK), 0),

    @Help("Played once per typed character.")
    val typingSound: LuxSound = LuxSound(StockStyle.TYPING_SOUND),

    @Help("Played when the player selects an answer.")
    val selectionSound: LuxSound = LuxSound(StockStyle.SELECTION_SOUND),

    @Help("Typing speed, movement restriction and the exit/skip rules.")
    val behaviour: LuxBehaviour = LuxBehaviour(),

    @Help("Extra decorative images layered above the background and below the text.")
    val customImages: List<LuxImageLayer> = emptyList(),

    @Default("\"Right click to skip\"")
    @Help("Hint shown while the text is still typing. Applied to pages that do not set their own.")
    val defaultTypingLine: String = "Right click to skip",

    @Default("\"Right click to continue\"")
    @Help("Hint shown once the text has finished typing. Applied to pages that do not set their own.")
    val defaultSteadyLine: String = "Right click to continue",

    @Default("\"${StockStyle.MORE_ANSWERS_TEXT}\"")
    @Help("Answer that shows the next set of answers. Empty uses 'More options'.")
    val moreAnswersText: String = StockStyle.MORE_ANSWERS_TEXT,

    @Default("\"${StockStyle.BACK_ANSWERS_TEXT}\"")
    @Help("Answer that returns to the first set. Empty uses 'Back'.")
    val backAnswersText: String = StockStyle.BACK_ANSWERS_TEXT,
) : StaticEntry
