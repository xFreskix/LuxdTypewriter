package io.github.xfreskix.luxdtypewriter.preview

import com.typewritermc.core.extension.annotations.TypewriterCommand
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.command.dsl.*
import com.typewritermc.engine.paper.entry.entries.SpeakerEntry
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import com.typewritermc.engine.paper.utils.msg
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxDialogueSource
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.citizens.CitizensSupport
import io.github.xfreskix.luxdtypewriter.style.LuxPackLayout
import io.github.xfreskix.luxdtypewriter.text.TextResolver
import io.github.xfreskix.luxdtypewriter.util.LuxLogger
import kotlinx.coroutines.Dispatchers
import org.bukkit.entity.Player

/** `/tw luxtw`: preview, trace, verify and debug. */
@TypewriterCommand
fun CommandTree.luxdTypewriterCommand() = literal("luxtw") {
    withPermission("luxdtypewriter.command")
    executes {
        sender.msg(
            "<white>LuxdTypewriter commands:\n" +
                "<gray>/tw luxtw preview <entry> <white>- show a dialogue without running it\n" +
                "<gray>/tw luxtw verify <white>- check the project for problems\n" +
                "<gray>/tw luxtw debug <white>- show LuxDialogues, Citizens and your session\n" +
                "<gray>/tw luxtw trace <white>- log your dialogues to the console",
        )
    }

    literal("preview") {
        withPermission("luxdtypewriter.preview")
        entry<DialogueEntry>("dialogue", filter = { it is LuxDialogueSource }) { dialogue ->
            executePlayer { player ->
                val entry = dialogue()
                val source = entry as? LuxDialogueSource ?: run {
                    sender.msg("<red>That entry is not a LuxdTypewriter dialogue.")
                    return@executePlayer
                }
                sender.msg("<gray>Opening a preview of <white>${source.name}<gray>…")
                val speakerName = entry.speaker.get()?.let { resolveSpeakerName(it, player) }.orEmpty()
                val sandbox = LuxServices.sandbox
                Dispatchers.UntickedAsync.launch {
                    when (sandbox.preview(source, player, speakerName)) {
                        PreviewResult.OPENED -> Unit
                        PreviewResult.LUX_UNAVAILABLE -> sender.msg("<red>LuxDialogues is not available.")
                        PreviewResult.IN_CONVERSATION -> sender.msg("<red>Finish your current dialogue first.")
                        PreviewResult.COOLDOWN -> sender.msg("<red>LuxDialogues is on cooldown for you; try again.")
                        PreviewResult.FAILED -> sender.msg("<red>The preview could not be opened. Check the console.")
                    }
                }
            }
        }
    }

    literal("trace") {
        withPermission("luxdtypewriter.debug")
        executePlayer { player ->
            sender.msg(
                if (LuxLogger.toggleTrace(player.uniqueId)) {
                    "<green>Tracing on for you.<gray> Every stage of your own dialogues and clicks now reports " +
                        "to the console. Run this again to turn it off."
                } else {
                    "<yellow>Tracing off for you."
                },
            )
        }
    }

    literal("verify") {
        withPermission("luxdtypewriter.debug")
        executes { sender.msg(ProjectVerifier.describe(ProjectVerifier.verify())) }
    }

    literal("debug") {
        withPermission("luxdtypewriter.debug")
        executePlayer { player ->
            val registry = LuxServices.registry
            val session = registry.find(player)
            val report = buildString {
                append("<gray><st>${" ".repeat(30)}</st>\n")
                append("<white><b>LuxdTypewriter debug</b>\n")
                append("<gray>LuxDialogues: <white>${LuxServices.bridge.availability}\n")
                append("<gray>Citizens support: <white>${CitizensSupport.status.description}\n")
                append("<gray>Pack layout: <white>${LuxPackLayout.dialogueLines ?: "?"} text rows, ")
                append("${LuxPackLayout.answerLines ?: "?"} answer rows\n")
                append("<gray>Active sessions on server: <white>${registry.activeCount}\n")
                if (session == null) {
                    append("<gray>Your session: <yellow>none\n")
                } else {
                    append("<gray>Your session:\n")
                    append("  <gray>state: <white>${session.state}\n")
                    append("  <gray>dialogue: <white>${session.dialogueId}\n")
                    append("  <gray>page: <white>${session.currentPageId ?: "-"}\n")
                    append("  <gray>last answer: <white>${session.lastAnswerId ?: "-"}\n")
                }
                append("<gray><st>${" ".repeat(30)}</st>")
            }
            sender.msg(report)
        }
    }
}

private fun resolveSpeakerName(speaker: SpeakerEntry, player: Player): String =
    TextResolver.resolve(speaker.displayName.get(player), player)
