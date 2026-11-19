package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.core.extension.annotations.Help

/** A blank [key] means silence. */
data class LuxSound(
    @Help("Sound key such as 'block.cherry_wood.hit' (not 'BLOCK_CHERRY_WOOD_HIT'). Empty is silent.")
    val key: String = "",
    val source: LuxSoundSource = LuxSoundSource.MASTER,
    @Help("Volume. 1.0 is the normal level.")
    val volume: Double = 1.0,
    @Help("Pitch from 0.5 to 2.0. 1.0 is normal.")
    val pitch: Double = 1.0,
) {
    val isEnabled: Boolean get() = key.isNotBlank()
}
