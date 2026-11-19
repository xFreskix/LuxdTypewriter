package io.github.xfreskix.luxdtypewriter.bridge

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/** Fired on the main thread when a player enters, leaves or moves inside any Lux dialogue. */
class LuxDialogueViewChangedEvent(
    player: Player,
    val previous: DialogueView,
    val current: DialogueView,
) : PlayerEvent(player) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}
