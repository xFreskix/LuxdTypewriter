package io.github.xfreskix.luxdtypewriter.style

/** A yes/no setting that can defer to the next level up. */
enum class LuxOverride(val value: Boolean?) {
    INHERIT(null),
    ON(true),
    OFF(false),
}
