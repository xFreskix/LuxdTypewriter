package io.github.xfreskix.luxdtypewriter.bridge

import java.util.UUID

/** LuxDialogues events as plain ids, so only `bridge` touches the event classes. */
interface DialogueEventSink {
    fun onStarted(playerId: UUID, dialogueId: String)
    fun onPageChanged(playerId: UUID, dialogueId: String, pageId: String)
    fun onAnswered(playerId: UUID, dialogueId: String, answerId: String)
    fun onRedirected(playerId: UUID, fromDialogueId: String?, toDialogueId: String)
    fun onStopped(playerId: UUID, dialogueId: String)
}
