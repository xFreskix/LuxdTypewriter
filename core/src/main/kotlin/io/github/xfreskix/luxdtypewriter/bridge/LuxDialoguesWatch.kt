package io.github.xfreskix.luxdtypewriter.bridge

import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Reports whether LuxDialogues is ready and, when it registers late (it waits for
 * ItemsAdder/Nexo/Oraxen), when it connects. Only logs: every call re-checks the provider.
 */
@Singleton
class LuxDialoguesWatch(private val connection: LuxDialoguesConnection) {
    private var job: Job? = null

    fun start() {
        when (connection.availability) {
            LuxAvailability.AVAILABLE -> logConnected()
            LuxAvailability.OUTDATED -> Unit // Already reported by the connection.
            LuxAvailability.NOT_REGISTERED -> {
                LuxLogger.warn(
                    "LuxDialogues has not registered its provider yet. That is normal while its resource pack " +
                        "provider (ItemsAdder, Nexo, Oraxen...) is still loading; LuxdTypewriter connects on its " +
                        "own as soon as it is ready.",
                )
                job = Dispatchers.UntickedAsync.launch { waitForProvider() }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun waitForProvider() {
        val deadline = System.currentTimeMillis() + TIMEOUT.inWholeMilliseconds
        while (System.currentTimeMillis() < deadline) {
            delay(POLL)
            if (connection.availability == LuxAvailability.AVAILABLE) {
                LuxLogger.reset() // Drop the warnings from the waiting period.
                logConnected()
                return
            }
        }
        LuxLogger.warn(
            "LuxDialogues still has not registered its provider after ${TIMEOUT.inWholeMinutes} minutes. Check " +
                "LuxDialogues and its resource pack provider for startup errors; '/tw luxtw verify' shows the state.",
        )
    }

    private fun logConnected() = LuxLogger.info("Connected to LuxDialogues. Dialogue entries are ready.")

    private companion object {
        val POLL = 2.seconds
        val TIMEOUT = 5.minutes
    }
}
