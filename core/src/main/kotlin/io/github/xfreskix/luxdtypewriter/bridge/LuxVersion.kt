package io.github.xfreskix.luxdtypewriter.bridge

/** Oldest LuxDialogues with every API call this extension uses. */
const val MINIMUM_LUX_VERSION = "4.1.3"

private val LEADING_VERSION = Regex("""^\d+(\.\d+)*""")

/** Whether [version] (`4.1.3`, `4.2.0-SNAPSHOT`...) is at least [MINIMUM_LUX_VERSION]. */
internal fun isSupportedLuxVersion(version: String): Boolean {
    val found = LEADING_VERSION.find(version.trim())?.value?.split('.')?.map(String::toInt) ?: return false
    val minimum = MINIMUM_LUX_VERSION.split('.').map(String::toInt)
    for (index in minimum.indices) {
        val part = found.getOrElse(index) { 0 }
        if (part != minimum[index]) return part > minimum[index]
    }
    return true
}
