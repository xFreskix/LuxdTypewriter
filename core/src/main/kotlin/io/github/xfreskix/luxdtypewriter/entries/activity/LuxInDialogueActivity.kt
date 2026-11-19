package io.github.xfreskix.luxdtypewriter.entries.activity

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.dialogue.speakersInDialogue
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entity.SingleChildActivity
import com.typewritermc.engine.paper.entry.entries.EntityActivityEntry
import io.github.xfreskix.luxdtypewriter.LuxServices
import java.time.Duration

/** Only decides which child runs; [SingleChildActivity] keeps their state. */
class LuxInDialogueActivity(
    private val idleDuration: Duration,
    private val talkingActivity: Ref<out EntityActivityEntry>,
    private val idleActivity: Ref<out EntityActivityEntry>,
    startLocation: PositionProperty,
) : SingleChildActivity<ActivityContext>(startLocation) {

    // Runs every tick for every NPC using it: keep it allocation free.
    override fun currentChild(context: ActivityContext): Ref<out EntityActivityEntry> =
        if (context.viewers.any { isTalkingTo(it, context) && isMakingProgress(it) }) talkingActivity else idleActivity

    /** Same signal as the built-in activity, so native dialogues count too. */
    private fun isTalkingTo(viewer: org.bukkit.entity.Player, context: ActivityContext): Boolean {
        val speakers = viewer.speakersInDialogue
        if (speakers.isEmpty()) return false
        val definition = context.instanceRef.get()?.definition
        return speakers.any { it == definition || it == context.instanceRef }
    }

    /** Without a Lux session the player is in a native dialogue, which handles its own idling. */
    private fun isMakingProgress(viewer: org.bukkit.entity.Player): Boolean {
        if (idleDuration.isZero || idleDuration.isNegative) return true

        val session = LuxServices.registry.find(viewer) ?: return true

        val idleMillis = System.currentTimeMillis() - session.lastProgressAt
        return idleMillis < idleDuration.toMillis()
    }
}
