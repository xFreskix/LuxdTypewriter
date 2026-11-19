package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.entries.Ref
import io.github.xfreskix.luxdtypewriter.text.TextMetrics
import io.github.xfreskix.luxdtypewriter.util.LuxLogger

/** Style values with the stock fallback for anything left at 0 or blank. */
object StyleResolver {

    /** Used when a dialogue references no style. */
    val DEFAULT: LuxStyleEntry by lazy { LuxStyleEntry() }

    /** White in the colour picker, meaning "stock colour". */
    const val UNSET_WHITE = -1

    const val DEFAULT_HOLD_TICKS = StockStyle.MESSAGE_HOLD_TICKS

    /** [DEFAULT] when [ref] is empty or points to a deleted style. */
    fun resolve(ref: Ref<LuxStyleEntry>): LuxStyleEntry {
        if (!ref.isSet) return DEFAULT
        return ref.get() ?: DEFAULT.also {
            LuxLogger.warnOnce(
                key = "style-missing-${ref.id}",
                message = "A Lux dialogue references the style '${ref.id}', which does not exist. The stock " +
                    "LuxDialogues look was used instead — including its typing speed. Re-select the style " +
                    "in the Typewriter panel.",
            )
        }
    }

    fun typingTicksPerCharacter(style: LuxStyleEntry): Int =
        style.behaviour.typingSpeed.takeIf { it > 0 } ?: StockStyle.TYPING_SPEED

    /** In pixels. */
    fun lineWidth(style: LuxStyleEntry): Int =
        (style.behaviour.lineMaxLength.takeIf { it > 0 } ?: StockStyle.LINE_MAX_LENGTH) * TextMetrics.STANDARD_ADVANCE

    /** In pixels. */
    fun answerWidth(style: LuxStyleEntry): Int =
        style.behaviour.answerMaxLength.takeIf { it > 0 }?.let { it * TextMetrics.STANDARD_ADVANCE }
            ?: lineWidth(style)

    /** Style, else pack, else 5. */
    fun linesPerPage(style: LuxStyleEntry): Int =
        style.behaviour.dialogueLines.takeIf { it > 0 }
            ?: LuxPackLayout.dialogueLines
            ?: StockStyle.DIALOGUE_LINES

    /** Style, else pack, else 3. */
    fun answerRows(style: LuxStyleEntry): Int =
        style.behaviour.answerLines.takeIf { it > 0 }
            ?: LuxPackLayout.answerLines
            ?: StockStyle.ANSWER_LINES

    /** Dialogue text colour as `#rrggbb`. */
    fun dialogueTextColor(style: LuxStyleEntry): String {
        val argb = style.dialogueText.color.color
        return StockStyle.hex(if (argb == UNSET_WHITE) StockStyle.INK else argb)
    }

    fun moreAnswersText(style: LuxStyleEntry): String =
        style.moreAnswersText.ifBlank { StockStyle.MORE_ANSWERS_TEXT }

    fun backAnswersText(style: LuxStyleEntry): String =
        style.backAnswersText.ifBlank { StockStyle.BACK_ANSWERS_TEXT }
}
