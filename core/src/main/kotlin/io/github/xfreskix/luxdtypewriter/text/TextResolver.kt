package io.github.xfreskix.luxdtypewriter.text

import com.typewritermc.core.interaction.InteractionContext
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.extensions.placeholderapi.parsePlaceholders
import org.bukkit.entity.Player

/** Resolves Typewriter variables and PlaceholderAPI placeholders; formatting is left as is. */
object TextResolver {

    fun resolve(value: Var<String>, player: Player, context: InteractionContext?): String =
        resolve(value.get(player, context), player)

    fun resolve(raw: String, player: Player): String = raw.parsePlaceholders(player)

    fun stripFormatting(input: String): String = TextLayout.stripFormatting(input)
}
