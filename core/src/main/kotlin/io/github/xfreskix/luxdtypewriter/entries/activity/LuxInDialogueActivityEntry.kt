package io.github.xfreskix.luxdtypewriter.entries.activity

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.engine.paper.entry.entity.ActivityContext
import com.typewritermc.engine.paper.entry.entity.EntityActivity
import com.typewritermc.engine.paper.entry.entity.PositionProperty
import com.typewritermc.engine.paper.entry.entries.EntityActivityEntry
import com.typewritermc.engine.paper.entry.entries.GenericEntityActivityEntry
import java.time.Duration

/**
 * Holds an NPC while a player talks to it and resumes its route afterwards.
 *
 * The built-in `in_dialogue_activity` releases the NPC after ~30 s without an entry
 * change, and a whole Lux conversation is one entry. Here every page change and
 * answer counts as progress.
 */
@Entry(
    "lux_in_dialogue_activity",
    "Pause an NPC while a player talks to it through LuxDialogues",
    Colors.PALATINATE_BLUE,
    "mdi:account-voice",
)
class LuxInDialogueActivityEntry(
    override val id: String = "",
    override val name: String = "",

    @Default("30000")
    @Help("Time without a page change or answer before the NPC is released. 0 holds it until the dialogue ends.")
    val idleDuration: Duration = Duration.ofSeconds(30),

    @Help("Activity used while the player is actively talking to this NPC.")
    val talkingActivity: Ref<out EntityActivityEntry> = emptyRef(),

    @Help("Activity used the rest of the time, e.g. the NPC's normal patrol.")
    val idleActivity: Ref<out EntityActivityEntry> = emptyRef(),
) : GenericEntityActivityEntry {

    override fun create(
        context: ActivityContext,
        currentLocation: PositionProperty,
    ): EntityActivity<ActivityContext> = LuxInDialogueActivity(
        idleDuration = idleDuration,
        talkingActivity = talkingActivity,
        idleActivity = idleActivity,
        startLocation = currentLocation,
    )
}
