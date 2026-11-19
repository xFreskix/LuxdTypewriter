package io.github.xfreskix.luxdtypewriter.bridge

enum class LuxAvailability {
    AVAILABLE,

    /** No provider yet; LuxDialogues registers it after its pack provider loads. */
    NOT_REGISTERED,

    /** Older than [MINIMUM_LUX_VERSION]. */
    OUTDATED,
}
