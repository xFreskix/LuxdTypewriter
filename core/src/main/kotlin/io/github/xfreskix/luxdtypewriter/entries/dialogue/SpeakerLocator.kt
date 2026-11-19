package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.engine.paper.entry.dialogue.speakersInDialogue
import com.typewritermc.engine.paper.entry.entity.AudienceEntityDisplay
import com.typewritermc.engine.paper.utils.toBukkitLocation
import io.github.xfreskix.luxdtypewriter.LuxServices
import org.bukkit.Location
import org.bukkit.entity.Player

/** Eye height of a player-sized NPC. */
private const val SPEAKER_EYE_HEIGHT = 1.62

/** Eye position of the closest visible NPC speaking in [player]'s dialogue. */
internal fun speakerEyeLocation(player: Player): Location? {
    val speakerIds = player.speakersInDialogue.map { it.id }
    if (speakerIds.isEmpty()) return null
    val playerId = player.uniqueId
    return LuxServices.audiences.findDisplays(AudienceEntityDisplay::class)
        .filter { it.canView(playerId) && it.isSpawnedIn(playerId) && it.speaksIn(speakerIds) }
        .mapNotNull { it.position(playerId) }
        .map { it.toBukkitLocation() }
        .filter { it.world == player.world }
        .minByOrNull { it.distanceSquared(player.location) }
        ?.add(0.0, SPEAKER_EYE_HEIGHT, 0.0)
}

private fun AudienceEntityDisplay.speaksIn(speakerIds: List<String>): Boolean {
    val instance = instanceEntryRef.get() ?: return false
    return instance.id in speakerIds || instance.definition.id in speakerIds
}
