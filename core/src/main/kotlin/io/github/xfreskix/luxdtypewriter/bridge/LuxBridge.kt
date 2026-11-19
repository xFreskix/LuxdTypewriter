package io.github.xfreskix.luxdtypewriter.bridge

import io.github.xfreskix.luxdtypewriter.build.DialogueBlueprint
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Everything LuxdTypewriter asks of LuxDialogues. Safe to call while
 * LuxDialogues is missing: it returns a neutral value and warns once.
 */
interface LuxBridge {
    val availability: LuxAvailability

    val isAvailable: Boolean get() = availability == LuxAvailability.AVAILABLE

    /** Shows [dialogue] at [pageId], replacing an open dialogue in place to avoid a flicker. */
    suspend fun open(player: Player, dialogue: DialogueBlueprint, pageId: String): OpenResult

    /** Shows the YAML dialogue [dialogueId], at [pageId] or at its own start page. */
    suspend fun openById(player: Player, dialogueId: String, pageId: String? = null): OpenResult

    /** Closes whatever dialogue [player] has open. */
    suspend fun clear(player: Player): Boolean

    /** Closes [player]'s dialogue only if it is still [dialogueId]. */
    suspend fun clearIfShowing(player: Player, dialogueId: String): Boolean

    /** Advances the dialogue as if the player clicked. */
    suspend fun advance(player: Player): Boolean

    /**
     * Advances only if [dialogueId] still shows [pageId] in the [typing] state and
     * [guard] holds, so a late click never skips an unseen page.
     */
    suspend fun interactIfUnchanged(
        player: Player,
        dialogueId: String,
        pageId: String,
        typing: Boolean,
        guard: () -> Boolean,
    ): Boolean

    /** Turns [player] to face [target] while [dialogueId] is open. */
    suspend fun focusIfShowing(player: Player, dialogueId: String, target: () -> Location?): Boolean

    /** Last known view of [player]; safe from any thread. */
    fun view(player: Player): DialogueView

    /** Reads [player]'s view from LuxDialogues now. */
    suspend fun refreshView(player: Player): DialogueView

    /** Whether LuxDialogues has a YAML dialogue called [dialogueId]. */
    fun hasDialogue(dialogueId: String): Boolean

    /** Whether the YAML dialogue [dialogueId] has a page called [pageId]. */
    fun hasPage(dialogueId: String, pageId: String): Boolean
}
