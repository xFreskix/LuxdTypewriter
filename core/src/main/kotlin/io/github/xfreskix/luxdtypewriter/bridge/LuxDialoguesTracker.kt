package io.github.xfreskix.luxdtypewriter.bridge

import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.engine.paper.plugin
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import org.aselstudios.luxdialoguesapi.DialogueProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Polls what LuxDialogues shows each player and fires [LuxDialogueViewChangedEvent]
 * on changes; LuxDialogues has no events for typing, selection or range closes.
 *
 * Players in a dialogue are read every tick, everyone else once a second.
 */
@Singleton
class LuxDialoguesTracker(
    private val connection: LuxDialoguesConnection,
) : Listener {

    private val views = ConcurrentHashMap<UUID, DialogueView>()

    /** Player → last tick to keep reading them, for dialogues not reported yet. */
    private val watchedUntil = ConcurrentHashMap<UUID, Long>()
    private var task: BukkitTask? = null
    @Volatile
    private var tick = 0L

    fun start() {
        plugin.registerEvents(this)
        task = plugin.server.scheduler.runTaskTimer(plugin, Runnable { pollTick() }, 1L, 1L)
    }

    fun stop() {
        task?.cancel()
        task = null
        unregister()
        views.clear()
        watchedUntil.clear()
    }

    /** Last known view of [player]; safe from any thread. */
    fun view(player: Player): DialogueView = views[player.uniqueId] ?: DialogueView.None

    /** Reads [player]'s view now. Main thread only. */
    fun refresh(player: Player): DialogueView = refresh(player, connection.provider)

    /** Reads [player] every tick for a moment, around a dialogue opening. */
    fun watch(player: Player) {
        watchedUntil[player.uniqueId] = tick + WATCH_GRACE_TICKS
    }

    /** Announces the close, so listeners such as the Citizens support can release the player. */
    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        watchedUntil.remove(event.player.uniqueId)
        val previous = views.remove(event.player.uniqueId) ?: return
        plugin.server.pluginManager.callEvent(LuxDialogueViewChangedEvent(event.player, previous, DialogueView.None))
    }

    private fun pollTick() {
        val provider = connection.provider
        val now = tick++
        val fullSweep = now % FULL_SWEEP_INTERVAL == 0L
        watchedUntil.values.removeIf { it < now }
        for (player in plugin.server.onlinePlayers) {
            val id = player.uniqueId
            if (fullSweep || views.containsKey(id) || watchedUntil.containsKey(id)) refresh(player, provider)
        }
    }

    private fun refresh(player: Player, provider: DialogueProvider?): DialogueView {
        val previous = view(player)
        val current = read(player, provider)
        if (current == DialogueView.None) views.remove(player.uniqueId) else views[player.uniqueId] = current
        if (hasMoved(previous, current)) {
            plugin.server.pluginManager.callEvent(LuxDialogueViewChangedEvent(player, previous, current))
        }
        return current
    }

    private fun read(player: Player, provider: DialogueProvider?): DialogueView {
        if (provider == null) return DialogueView.None
        return runCatching { readOpen(provider, player) }.getOrElse { failure ->
            LuxLogger.warnOnce("view-failed", "LuxDialogues threw while reading what a player is shown: $failure")
            DialogueView.Unknown
        }
    }

    private fun readOpen(provider: DialogueProvider, player: Player): DialogueView {
        val dialogue = provider.getDialogue(player) ?: return DialogueView.None
        val page = provider.getCurrentPage(player)
        return DialogueView.Open(
            dialogueId = dialogue.dialogueID,
            pageId = page?.id,
            pageHasAnswers = page?.answers?.isNotEmpty() == true,
            typing = provider.isTyping(player) == true,
            selectedAnswer = provider.getSelectedAnswer(player) ?: 0,
        )
    }

    private companion object {
        const val FULL_SWEEP_INTERVAL = 20L

        const val WATCH_GRACE_TICKS = 10L
    }
}
