package io.github.xfreskix.luxdtypewriter.citizens

import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import java.lang.reflect.Method

/**
 * Pauses and resumes Citizens NPCs in place: the path, target and waypoints are
 * kept, unlike cancelling the navigation, which waypoints restart at once.
 */
internal object CitizensNpcControl {

    // Renamed in Citizens 2.0.41 (Feb 2026), so it is looked up by name.
    private val CONTROLLER_GETTERS = listOf("getDefaultBehaviorController", "getDefaultGoalController")

    private var controllerGetter: Method? = null
    private var controllerLookupDone = false

    fun pause(npcId: Int) = setPaused(npcId, true)

    fun resume(npcId: Int) = setPaused(npcId, false)

    private fun setPaused(npcId: Int, paused: Boolean) {
        val npc = CitizensAPI.getNPCRegistry().getById(npcId) ?: return
        npc.navigator.setPaused(paused)
        setControllerPaused(npc, paused)
    }

    /** Pauses the goals (wander, waypoints...) that would otherwise pick a new target. */
    private fun setControllerPaused(npc: NPC, paused: Boolean) {
        val getter = controllerGetter(npc) ?: return
        runCatching {
            val controller = getter.invoke(npc) ?: return
            controller.javaClass.getMethod("setPaused", Boolean::class.javaPrimitiveType).invoke(controller, paused)
        }.onFailure { LuxLogger.warnOnce("citizens-controller", "Could not pause the goals of Citizens NPCs: $it") }
    }

    private fun controllerGetter(npc: NPC): Method? {
        if (!controllerLookupDone) {
            controllerGetter = CONTROLLER_GETTERS.firstNotNullOfOrNull { name ->
                runCatching { npc.javaClass.getMethod(name) }.getOrNull()
            }
            controllerLookupDone = true
        }
        return controllerGetter
    }
}
