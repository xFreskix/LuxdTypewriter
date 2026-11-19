package io.github.xfreskix.luxdtypewriter.bridge

import com.typewritermc.engine.paper.utils.Color
import io.github.xfreskix.luxdtypewriter.build.AnswerBlueprint
import io.github.xfreskix.luxdtypewriter.build.DialogueBlueprint
import io.github.xfreskix.luxdtypewriter.build.PageBlueprint
import io.github.xfreskix.luxdtypewriter.style.LuxImageLayer
import io.github.xfreskix.luxdtypewriter.style.LuxSound
import io.github.xfreskix.luxdtypewriter.style.LuxStyleEntry
import io.github.xfreskix.luxdtypewriter.style.SoundKeys
import io.github.xfreskix.luxdtypewriter.style.StockStyle
import io.github.xfreskix.luxdtypewriter.style.StyleResolver
import io.github.xfreskix.luxdtypewriter.text.TextResolver
import org.aselstudios.luxdialoguesapi.Builders.Answer
import org.aselstudios.luxdialoguesapi.Builders.Dialogue
import org.aselstudios.luxdialoguesapi.Builders.Page

// Converts blueprints into LuxDialogues builder objects.

/** A sound that plays nothing; LuxDialogues always plays one. */
private const val SILENT_KEY = "minecraft:intentionally_empty"

/** Timer action that follows the page's `goto` instead of closing. */
private const val FOLLOW_GOTO_ON_TIMER = "@triggerGoto"

internal fun DialogueBlueprint.toLuxDialogue(): Dialogue {
    val builder = Dialogue.Builder()
        .setDialogueID(id)
        // Typewriter drives the conversation; LuxDialogues only renders it.
        .setExternallyDriven(true)
        .applyStyle(style)
        .applySpeakerName(style, speakerName)
    pages.forEach { builder.addPage(it.toLuxPage()) }
    return builder.build()
}

private fun PageBlueprint.toLuxPage(): Page {
    val builder = Page.Builder().setID(id)
    lines.forEach { builder.addLine(it) }
    builder.setTypingInfoLine(typingLine)
    builder.setSteadyInfoLine(steadyLine)
    if (timerTicks > 0) builder.setTimer(timerTicks)
    if (followGotoOnTimer) builder.addTimerAction(FOLLOW_GOTO_ON_TIMER)
    // No goto on the last page: an empty list does not close the window cleanly.
    nextPageId?.let { builder.setGoTo(listOf(it)) }
    onShown?.let { callback -> builder.addPreCallback { callback() } }
    onFinished?.let { callback -> builder.addPostCallback { callback() } }
    preventExit?.let { builder.setPreventExit(it) }
    preventSkip?.let { builder.setPreventSkip(it) }
    answers.forEach { builder.addAnswer(it.toLuxAnswer()) }
    return builder.build()
}

private fun AnswerBlueprint.toLuxAnswer(): Answer {
    val builder = Answer.Builder()
        .setAnswerID(id)
        .setAnswerText(text)
    replyMessages.forEach { builder.addReplyMessage(it) }
    // Real answers have no goto: they end the dialogue and Typewriter branches.
    // An empty list would break LuxDialogues' answer callback.
    goToPageId?.let { builder.setGoTo(listOf(it)) }
    onChosen?.let { callback -> builder.addCallback { callback() } }
    return builder.build()
}

private fun Dialogue.Builder.applyStyle(style: LuxStyleEntry): Dialogue.Builder = apply {
    applyLayer(style.background, StockStyle.BACKGROUND_IMAGE, StockStyle.PARCHMENT, StockStyle.BACKGROUND_OFFSET) {
            image, tint, offset ->
        setDialogueBackgroundImage(image, tint, offset)
    }
    applyLayer(
        style.answerBackground, StockStyle.ANSWER_BACKGROUND_IMAGE, StockStyle.PARCHMENT,
        StockStyle.ANSWER_BACKGROUND_OFFSET,
    ) { image, tint, offset -> setAnswerBackgroundImage(image, tint, offset) }
    applyLayer(style.arrow, StockStyle.ARROW_IMAGE, StockStyle.LIME, StockStyle.ARROW_OFFSET) { image, tint, offset ->
        setArrowImage(image, tint, offset)
    }

    style.character.takeIf { it.image.isNotBlank() }?.let {
        setCharacterImage(it.image, it.color.toLuxHex(), it.offset)
    }

    val plate = style.namePlate
    if (plate.startImage.isBlank() && plate.midImage.isBlank() && plate.endImage.isBlank()) {
        setNameImage(
            StockStyle.NAME_START_IMAGE, StockStyle.NAME_MID_IMAGE, StockStyle.NAME_END_IMAGE,
            StockStyle.hex(StockStyle.PARCHMENT), StockStyle.NAME_PLATE_OFFSET,
        )
    } else {
        val tint = plate.color.orStock(StockStyle.PARCHMENT)
        setNameImage(plate.startImage, plate.midImage, plate.endImage, tint, plate.offset)
    }

    setFogImage(style.fog.image.ifBlank { StockStyle.FOG_IMAGE }, style.fog.color.orStock(StockStyle.BLACK))

    style.customImages.filter { it.image.isNotBlank() }.forEach {
        addCustomImage(it.image, it.offset, it.color.toLuxHex())
    }

    setDialogueText(
        style.dialogueText.color.orStock(StockStyle.INK),
        style.dialogueText.offset.takeIf { it != 0 } ?: StockStyle.DIALOGUE_TEXT_OFFSET,
    )
    setAnswerText(
        style.answerText.color.orStock(StockStyle.INK),
        style.answerText.offset.takeIf { it != 0 } ?: StockStyle.ANSWER_TEXT_OFFSET,
        style.answerText.selectedColor.orStock(StockStyle.INK),
    )
    setInformationText(style.informationText.color.orStock(StockStyle.INK), style.informationText.offset)

    applySound(style.typingSound) { key, source, volume, pitch -> setTypingSound(key, source, volume, pitch) }
    applySound(style.selectionSound) { key, source, volume, pitch ->
        setSelectionSound(key, source, volume, pitch)
    }

    setDialogueSpeed(StyleResolver.typingTicksPerCharacter(style))
    val behaviour = style.behaviour
    setRange(behaviour.range.takeIf { it > 0.0 } ?: StockStyle.RANGE)
    setEffect(behaviour.effect.apiValue)
    setAnswerNumbers(behaviour.answerNumbers)
    setPreventExit(behaviour.preventExit)
    setPreventSkip(behaviour.preventSkip)
    setNpcFocus(behaviour.npcFocus)
    behaviour.typingPreventExit.value?.let { setTypingPreventExit(it) }
    behaviour.steadyPreventExit.value?.let { setSteadyPreventExit(it) }
}

/** The name plate is sized from the raw string, so formatting is stripped. */
private fun Dialogue.Builder.applySpeakerName(style: LuxStyleEntry, displayName: String): Dialogue.Builder =
    setCharacterNameText(
        TextResolver.stripFormatting(displayName),
        style.characterNameText.color.orStock(StockStyle.INK),
        style.characterNameText.offset,
    )

/** A blank image means the stock layer. */
private inline fun applyLayer(
    layer: LuxImageLayer,
    stockImage: String,
    stockTint: Int,
    stockOffset: Int,
    setter: (String, String, Int) -> Unit,
) {
    if (layer.image.isBlank()) setter(stockImage, StockStyle.hex(stockTint), stockOffset)
    else setter(layer.image, layer.color.toLuxHex(), layer.offset)
}

private inline fun applySound(sound: LuxSound, setter: (String, String, Double, Double) -> Unit) {
    if (!SoundKeys.isPlayable(sound)) setter(SILENT_KEY, "MASTER", 0.0, 1.0)
    else setter(sound.key, sound.source.name, sound.volume, sound.pitch)
}

private fun Color.orStock(stockArgb: Int): String =
    if (color == StyleResolver.UNSET_WHITE) StockStyle.hex(stockArgb) else toLuxHex()

private fun Color.toLuxHex(): String = StockStyle.hex(color)
