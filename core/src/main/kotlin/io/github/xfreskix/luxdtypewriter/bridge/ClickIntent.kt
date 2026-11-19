package io.github.xfreskix.luxdtypewriter.bridge

/** What a click on the speaker does. */
internal enum class ClickIntent { NONE, SKIP, TURN, CONFIRM }

/** @param select `true` for a right-click, the only button that confirms answers. */
internal fun clickIntent(view: DialogueView, select: Boolean): ClickIntent = when {
    view !is DialogueView.Open || view.pageId == null -> ClickIntent.NONE
    view.typing -> ClickIntent.SKIP
    !view.pageHasAnswers -> ClickIntent.TURN
    select -> ClickIntent.CONFIRM
    else -> ClickIntent.NONE
}
