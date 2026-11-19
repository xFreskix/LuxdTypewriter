package io.github.xfreskix.luxdtypewriter.style

import com.typewritermc.engine.paper.plugin
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * Text and answer rows of the LuxDialogues pack, read once from its `lines.yml`.
 * Used by styles that leave `dialogueLines`/`answerLines` at 0; `null` if unreadable.
 */
object LuxPackLayout {

    @Volatile
    private var cached: Layout? = null

    val dialogueLines: Int? get() = layout().dialogueLines

    val answerLines: Int? get() = layout().answerLines

    /** Lets `/typewriter reload` pick up an edited pack. */
    fun reset() {
        cached = null
    }

    private fun layout(): Layout = cached ?: load().also { cached = it }

    private fun load(): Layout {
        val folder = plugin.server.pluginManager.getPlugin("LuxDialogues")?.dataFolder ?: return Layout.UNKNOWN
        val file = findLinesFile(folder) ?: return Layout.UNKNOWN
        return runCatching { parse(YamlConfiguration.loadConfiguration(file)) }
            .onSuccess { layout ->
                if (layout != Layout.UNKNOWN) {
                    LuxLogger.info(
                        "Read the LuxDialogues pack layout from ${file.relativeTo(folder)}: " +
                            "${layout.dialogueLines ?: "?"} dialogue rows, ${layout.answerLines ?: "?"} answer rows.",
                    )
                }
            }
            .getOrElse { failure ->
                LuxLogger.warnOnce("lines-yml", "Could not read ${file.path}: $failure. Using the stock layout.")
                Layout.UNKNOWN
            }
    }

    private fun findLinesFile(folder: File): File? {
        File(folder, LINES_FILE).takeIf(File::isFile)?.let { return it }
        return folder.walkTopDown().maxDepth(MAX_SEARCH_DEPTH)
            .firstOrNull { it.isFile && it.name.equals(LINES_FILE, ignoreCase = true) }
    }

    internal fun parse(config: ConfigurationSection): Layout = Layout(
        dialogueLines = rowCount(config, DIALOGUE_LINES_KEY),
        answerLines = rowCount(config, ANSWER_LINES_KEY),
    )

    /** `<key>.count`, else the number of row sections under `<key>`. */
    private fun rowCount(config: ConfigurationSection, key: String): Int? {
        val section = config.getKeys(false).firstOrNull { it.equals(key, ignoreCase = true) }
            ?.let(config::getConfigurationSection) ?: return null
        val countKey = section.getKeys(false).firstOrNull { it.equals("count", ignoreCase = true) }
        val count = countKey?.let(section::getInt) ?: section.getKeys(false).count(section::isConfigurationSection)
        return count.takeIf { it > 0 }
    }

    internal data class Layout(val dialogueLines: Int?, val answerLines: Int?) {
        companion object {
            val UNKNOWN = Layout(null, null)
        }
    }

    private const val LINES_FILE = "lines.yml"
    private const val DIALOGUE_LINES_KEY = "Dialogue-Lines"
    private const val ANSWER_LINES_KEY = "Answer-Lines"
    private const val MAX_SEARCH_DEPTH = 4
}
