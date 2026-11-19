package io.github.xfreskix.luxdtypewriter.util

import com.typewritermc.engine.paper.logger
import io.github.xfreskix.luxdtypewriter.bridge.MINIMUM_LUX_VERSION
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

/** Prefixed logging. Lasting problems go through [warnOnce] so they are logged once, not per tick. */
object LuxLogger {
    private const val PREFIX = "[LuxdTypewriter] "

    private val emittedWarnings = ConcurrentHashMap.newKeySet<String>()

    /** Players traced with `/tw luxtw trace`. */
    private val tracedPlayers = ConcurrentHashMap.newKeySet<UUID>()

    /** Returns whether tracing is now on. */
    fun toggleTrace(playerId: UUID): Boolean {
        if (tracedPlayers.remove(playerId)) return false
        tracedPlayers.add(playerId)
        return true
    }

    fun isTracing(playerId: UUID): Boolean = playerId in tracedPlayers

    /** The message is only built while [player] is traced. */
    inline fun trace(player: Player, message: () -> String) {
        if (isTracing(player.uniqueId)) traceLine(player, message())
    }

    /** Used by the inline [trace]. */
    fun traceLine(player: Player, message: String) =
        logger.info("$PREFIX[trace ${player.name}] $message")

    fun info(message: String) = logger.info(PREFIX + message)

    fun warn(message: String) = logger.warning(PREFIX + message)

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable == null) logger.severe(PREFIX + message)
        else logger.log(Level.SEVERE, PREFIX + message, throwable)
    }

    /** Logs [message] only the first time [key] is seen. */
    fun warnOnce(key: String, message: String) {
        if (emittedWarnings.add(key)) warn(message)
    }

    fun warnLuxUnavailable() = warnOnce(
        key = "provider-missing",
        message = "LuxDialogues is not available yet, so Lux dialogues stop their sequence and do " +
            "nothing else. Install and enable LuxDialogues $MINIMUM_LUX_VERSION or newer. If it is " +
            "installed, check that it finished loading: LuxDialogues registers itself only after " +
            "its resource pack provider (ItemsAdder, Nexo, Oraxen...) has loaded its data.",
    )

    /** Lets a reload report a still-broken setup again. */
    fun reset() = emittedWarnings.clear()
}
