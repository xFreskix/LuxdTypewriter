package io.github.xfreskix.luxdtypewriter

import com.typewritermc.engine.paper.entry.AudienceManager
import io.github.xfreskix.luxdtypewriter.bridge.LuxBridge
import io.github.xfreskix.luxdtypewriter.build.LuxDialogueFactory
import io.github.xfreskix.luxdtypewriter.preview.PreviewSandbox
import io.github.xfreskix.luxdtypewriter.session.LuxSessionRegistry
import org.koin.java.KoinJavaComponent
import java.util.concurrent.ConcurrentHashMap

/**
 * Koin lookups for objects Typewriter creates itself (messengers, filters,
 * entries). Cached until [reset], which runs on every (re)load.
 */
object LuxServices {
    private val resolved = ConcurrentHashMap<Class<*>, Any>()

    val bridge: LuxBridge get() = resolve(LuxBridge::class.java)
    val registry: LuxSessionRegistry get() = resolve(LuxSessionRegistry::class.java)
    val factory: LuxDialogueFactory get() = resolve(LuxDialogueFactory::class.java)
    val sandbox: PreviewSandbox get() = resolve(PreviewSandbox::class.java)
    val audiences: AudienceManager get() = resolve(AudienceManager::class.java)

    fun reset() = resolved.clear()

    private fun <T : Any> resolve(type: Class<T>): T =
        type.cast(resolved.computeIfAbsent(type) { KoinJavaComponent.get<Any>(type) })
}
