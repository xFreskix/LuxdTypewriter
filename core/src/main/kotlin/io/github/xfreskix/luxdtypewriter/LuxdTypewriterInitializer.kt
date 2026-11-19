package io.github.xfreskix.luxdtypewriter

import com.typewritermc.core.extension.Initializable
import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.core.utils.switchContext
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.Sync
import io.github.xfreskix.luxdtypewriter.bridge.LuxBridge
import io.github.xfreskix.luxdtypewriter.bridge.LuxDialoguesEventRelay
import io.github.xfreskix.luxdtypewriter.bridge.LuxDialoguesTracker
import io.github.xfreskix.luxdtypewriter.bridge.LuxDialoguesWatch
import io.github.xfreskix.luxdtypewriter.citizens.CitizensSupport
import io.github.xfreskix.luxdtypewriter.session.LuxEventListener
import io.github.xfreskix.luxdtypewriter.session.LuxInteractionListener
import io.github.xfreskix.luxdtypewriter.session.LuxSession
import io.github.xfreskix.luxdtypewriter.session.LuxSessionRegistry
import io.github.xfreskix.luxdtypewriter.style.LuxPackLayout
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Extension entry point. [shutdown] also runs on every `/typewriter reload`. */
@Singleton
object LuxdTypewriterInitializer : Initializable, KoinComponent {

    private val bridge: LuxBridge by inject()
    private val tracker: LuxDialoguesTracker by inject()
    private val relay: LuxDialoguesEventRelay by inject()
    private val watch: LuxDialoguesWatch by inject()
    private val registry: LuxSessionRegistry by inject()
    private val listener: LuxEventListener by inject()
    private val interactionListener: LuxInteractionListener by inject()

    override suspend fun initialize() {
        LuxServices.reset()
        LuxPackLayout.reset()
        plugin.registerEvents(listener)
        plugin.registerEvents(relay)
        plugin.registerEvents(interactionListener)
        tracker.start()
        CitizensSupport.enable()
        watch.start()
    }

    override suspend fun shutdown() {
        watch.stop()
        // Unregister first so closing sessions cannot race incoming events.
        listener.unregister()
        relay.unregister()
        interactionListener.unregister()

        val stopping = plugin.server.isStopping
        if (stopping) CitizensSupport.disable(resumeNpcs = false)
        else Dispatchers.Sync.switchContext { CitizensSupport.disable(resumeNpcs = true) }

        val sessions = registry.activeSessions()
        if (!stopping) closeWindows(sessions)
        registry.closeAll()
        tracker.stop()
        if (sessions.isNotEmpty()) LuxLogger.info("Closed ${sessions.size} active dialogue(s) before unloading.")

        LuxLogger.reset()
        LuxLogger.info("Shut down.")
    }

    private suspend fun closeWindows(sessions: List<LuxSession>) {
        for (session in sessions) {
            val player = plugin.server.getPlayer(session.playerId) ?: continue
            bridge.clearIfShowing(player, session.dialogueId)
        }
    }
}
