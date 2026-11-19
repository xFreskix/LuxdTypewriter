package io.github.xfreskix.luxdtypewriter.citizens

import com.typewritermc.engine.paper.plugin
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents

/**
 * Optional Citizens support (a soft dependency). Citizens is not declared as a
 * dependency, so this object must only touch Citizens classes after [enable]
 * checked that they exist.
 */
object CitizensSupport {
    private const val CITIZENS_PLUGIN = "Citizens"
    private const val REFERENCE_NPC_CLASS = "com.typewritermc.citizens.entries.entity.ReferenceNpcEntry"

    @Volatile
    private var listener: CitizensDialogueListener? = null

    @Volatile
    var status: CitizensStatus = CitizensStatus.NOT_INSTALLED
        private set

    /** Turns the support on when Citizens and Typewriter's Citizens extension are present. */
    fun enable() {
        if (listener != null) return
        status = when {
            plugin.server.pluginManager.getPlugin(CITIZENS_PLUGIN)?.isEnabled != true -> CitizensStatus.NOT_INSTALLED
            !isClassPresent(REFERENCE_NPC_CLASS) -> CitizensStatus.EXTENSION_MISSING
            else -> start()
        }
        when (status) {
            CitizensStatus.ACTIVE -> LuxLogger.info("Citizens support is active.")
            CitizensStatus.EXTENSION_MISSING -> LuxLogger.warn(
                "Citizens is installed, but Typewriter's Citizens extension is not, so Citizens NPCs cannot " +
                    "speak in Lux dialogues. Install it to turn Citizens support on.",
            )
            CitizensStatus.NOT_INSTALLED, CitizensStatus.FAILED -> Unit
        }
    }

    /**
     * Stops listening. [resumeNpcs] resumes the NPCs it paused and must run on the
     * main thread; it is skipped while the server stops.
     */
    fun disable(resumeNpcs: Boolean) {
        listener?.let {
            it.unregister()
            if (resumeNpcs) it.releaseAll()
        }
        listener = null
        status = CitizensStatus.NOT_INSTALLED
    }

    /** Whether [className] is the class of a Typewriter `reference_npc` entry. */
    fun isReferenceNpc(className: String): Boolean = className == REFERENCE_NPC_CLASS

    private fun start(): CitizensStatus = try {
        val created = CitizensDialogueListener()
        plugin.registerEvents(created)
        listener = created
        CitizensStatus.ACTIVE
    } catch (failure: Throwable) {
        // A Citizens build without the expected API disables only this feature.
        LuxLogger.error("Citizens support failed to start and was turned off.", failure)
        CitizensStatus.FAILED
    }

    private fun isClassPresent(name: String): Boolean =
        runCatching { Class.forName(name, false, javaClass.classLoader) }.isSuccess
}
