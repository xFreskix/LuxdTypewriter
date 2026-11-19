package io.github.xfreskix.luxdtypewriter.style

/** Values of the stock LuxDialogues pack: style defaults and fallbacks. */
object StockStyle {
    const val BACKGROUND_IMAGE = "dialogue-background"
    const val ANSWER_BACKGROUND_IMAGE = "answer-background"
    const val ARROW_IMAGE = "hand"
    const val NAME_START_IMAGE = "name-start"
    const val NAME_MID_IMAGE = "name-mid"
    const val NAME_END_IMAGE = "name-end"
    const val FOG_IMAGE = "fog"

    // ARGB colours
    const val PARCHMENT = 0xFFF8FFE0.toInt()
    const val INK = 0xFF4F4A3E.toInt()
    const val LIME = 0xFFCDFF29.toInt()
    const val BLACK = 0xFF000000.toInt()

    const val BACKGROUND_OFFSET = 0
    const val ANSWER_BACKGROUND_OFFSET = 140
    const val ARROW_OFFSET = -7
    const val CHARACTER_OFFSET = -16
    const val NAME_PLATE_OFFSET = 20
    const val DIALOGUE_TEXT_OFFSET = 10
    const val ANSWER_TEXT_OFFSET = 13

    const val TYPING_SOUND = "luxdialogues:luxdialogues.sounds.typing"
    const val SELECTION_SOUND = "luxdialogues:luxdialogues.sounds.selection"

    const val TYPING_SPEED = 1
    const val RANGE = 5.0
    const val LINE_MAX_LENGTH = 34
    const val MESSAGE_HOLD_TICKS = 60
    const val ANSWER_LINES = 3
    const val DIALOGUE_LINES = 5
    const val MORE_ANSWERS_TEXT = "More options"
    const val BACK_ANSWERS_TEXT = "Back"

    /** ARGB → `#rrggbb`. */
    fun hex(argb: Int): String = "#%06x".format(argb and 0xFFFFFF)
}
