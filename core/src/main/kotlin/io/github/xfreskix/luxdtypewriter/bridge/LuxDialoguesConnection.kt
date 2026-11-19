package io.github.xfreskix.luxdtypewriter.bridge

import com.typewritermc.core.extension.annotations.Singleton
import com.typewritermc.engine.paper.plugin
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import org.aselstudios.luxdialoguesapi.DialogueProvider
import org.aselstudios.luxdialoguesapi.LuxDialoguesAPI

private const val LUX_PLUGIN_NAME = "LuxDialogues"

/**
 * Finds the LuxDialogues provider. It is never cached: LuxDialogues registers it
 * only after ItemsAdder/Nexo/Oraxen load, which can be long after startup.
 */
@Singleton
class LuxDialoguesConnection {

    /** The provider, or `null` while LuxDialogues is not usable. */
    val provider: DialogueProvider? get() = resolve().provider

    val availability: LuxAvailability get() = resolve().availability

    private fun resolve(): Resolved {
        val provider = registeredProvider() ?: return Resolved.NOT_REGISTERED
        return if (supportedVersion()) Resolved(LuxAvailability.AVAILABLE, provider) else Resolved.OUTDATED
    }

    private class Resolved(val availability: LuxAvailability, val provider: DialogueProvider?) {
        companion object {
            val NOT_REGISTERED = Resolved(LuxAvailability.NOT_REGISTERED, null)
            val OUTDATED = Resolved(LuxAvailability.OUTDATED, null)
        }
    }

    /** The installed version cannot change while the server runs. */
    @Volatile
    private var versionSupported: Boolean? = null

    @Volatile
    private var missingSince = 0L

    /** `getProvider()` throws until registered, so a missing provider is retried once a second. */
    private fun registeredProvider(): DialogueProvider? {
        val now = System.currentTimeMillis()
        if (now - missingSince < RETRY_MILLIS) return null
        return runCatching { LuxDialoguesAPI.getProvider() }.getOrNull()
            .also { missingSince = if (it == null) now else 0L }
    }

    private fun supportedVersion(): Boolean {
        versionSupported?.let { return it }
        val version = plugin.server.pluginManager.getPlugin(LUX_PLUGIN_NAME)?.pluginMeta?.version
            ?: return true // A fork or renamed jar: trust the registered provider.
        val supported = isSupportedLuxVersion(version)
        if (!supported) {
            LuxLogger.warnOnce(
                key = "lux-version",
                message = "LuxDialogues $version is installed, but LuxdTypewriter needs " +
                    "$MINIMUM_LUX_VERSION or newer. Lux dialogues stop their sequence until " +
                    "LuxDialogues is updated.",
            )
        }
        versionSupported = supported
        return supported
    }

    private companion object {
        const val RETRY_MILLIS = 1_000L
    }
}
