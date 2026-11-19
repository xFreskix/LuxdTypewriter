package io.github.xfreskix.luxdtypewriter.preview

import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.LuxAvailability
import io.github.xfreskix.luxdtypewriter.bridge.MINIMUM_LUX_VERSION
import io.github.xfreskix.luxdtypewriter.build.cannotPageAnswers
import io.github.xfreskix.luxdtypewriter.build.hasPagesAfterQuestion
import io.github.xfreskix.luxdtypewriter.citizens.CitizensStatus
import io.github.xfreskix.luxdtypewriter.citizens.CitizensSupport
import io.github.xfreskix.luxdtypewriter.entries.action.LuxRedirectDialogueEntry
import io.github.xfreskix.luxdtypewriter.entries.cinematic.LuxDialogueCinematicEntry
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxDialogueEntry
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxDialogueRefEntry
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxDialogueSource
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxMessageEntry
import io.github.xfreskix.luxdtypewriter.entries.dialogue.LuxOptionEntry
import io.github.xfreskix.luxdtypewriter.style.LuxStyleEntry
import io.github.xfreskix.luxdtypewriter.style.SoundKeys
import io.github.xfreskix.luxdtypewriter.style.StyleResolver
import com.typewritermc.engine.paper.entry.entries.DialogueEntry

/**
 * `/tw luxtw verify`: finds missing styles, unreachable pages, missing YAML
 * dialogues or pages and invalid sound keys.
 */
object ProjectVerifier {

    fun verify(): VerifyReport {
        val styles = Query.find<LuxStyleEntry>().toList()
        val references = Query.find<LuxDialogueRefEntry>().toList()
        val redirects = Query.find<LuxRedirectDialogueEntry>().toList()
        val cinematics = Query.find<LuxDialogueCinematicEntry>().toList()
        val dialogues: List<LuxDialogueSource> = buildList {
            addAll(Query.find<LuxDialogueEntry>())
            addAll(Query.find<LuxMessageEntry>())
            addAll(Query.find<LuxOptionEntry>())
        }

        val availability = LuxServices.bridge.availability
        val luxAvailable = availability == LuxAvailability.AVAILABLE
        val problems = buildList {
            addAll(availabilityProblems(availability))
            styles.forEach { addAll(styleProblems(it)) }
            dialogues.forEach { addAll(dialogueProblems(it)) }
            (dialogues + references).forEach { addAll(citizensSpeakerProblems(it)) }
            references.forEach { addAll(yamlProblems(it.name, it.id, it.dialogueId, it.page, luxAvailable)) }
            redirects.forEach { addAll(yamlProblems(it.name, it.id, it.dialogueId, it.page, luxAvailable)) }
            cinematics.forEach { addAll(missingStyleProblems(it.name, it.id, it.style)) }
        }
        val checked = styles.size + dialogues.size + references.size + redirects.size + cinematics.size
        return VerifyReport(checked, problems)
    }

    fun describe(report: VerifyReport): String {
        val header = "<gray>LuxdTypewriter checked <white>${report.checkedEntries}<gray> entries. " +
            "Citizens support: <white>${CitizensSupport.status.description}<gray>."
        if (report.problems.isEmpty()) return "$header\n<green>No problems found."
        val lines = report.problems.joinToString("\n") {
            "<gray>- <white>${escape(it.subject)}<gray> ${escape(it.message)}"
        }
        return "$header\n<yellow>${report.problems.size} problem(s) found:\n$lines"
    }

    private fun availabilityProblems(availability: LuxAvailability): List<VerifyProblem> = when (availability) {
        LuxAvailability.AVAILABLE -> emptyList()
        LuxAvailability.NOT_REGISTERED -> listOf(
            VerifyProblem(
                "LuxDialogues",
                "has not finished loading, so no Lux dialogue can open and YAML dialogues were not checked. " +
                    "It waits for its resource pack provider (ItemsAdder, Nexo, Oraxen...) before it registers.",
            ),
        )
        LuxAvailability.OUTDATED -> listOf(
            VerifyProblem(
                "LuxDialogues",
                "is older than $MINIMUM_LUX_VERSION, so Lux dialogues stop their sequence and YAML dialogues were not checked.",
            ),
        )
    }

    private fun styleProblems(style: LuxStyleEntry): List<VerifyProblem> =
        listOf(style.typingSound, style.selectionSound)
            .filter { it.isEnabled && !SoundKeys.isValid(it.key) }
            .map {
                VerifyProblem(
                    subject(style.name, style.id),
                    "has the sound key '${it.key}', which Minecraft cannot play. Keys are lowercase and dot separated.",
                )
            }

    private fun dialogueProblems(source: LuxDialogueSource): List<VerifyProblem> {
        val subject = subject(source.name, source.id)
        val pages = source.pages
        val questionIndex = pages.indexOfFirst { it.isQuestion }
        return buildList {
            addAll(missingStyleProblems(source.name, source.id, source.style))
            if (pages.isEmpty()) add(VerifyProblem(subject, "has no pages."))
            if (hasPagesAfterQuestion(pages.size, questionIndex)) {
                add(VerifyProblem(subject, "has pages after its question, and they are never shown."))
            }
            if (questionIndex != -1) {
                val answers = pages[questionIndex].answers.size
                if (cannotPageAnswers(answers, StyleResolver.answerRows(styleOf(source.style)))) {
                    val message = "offers $answers answers but shows one answer row, so only the first is used."
                    add(VerifyProblem(subject, message))
                }
            }
        }
    }

    private fun citizensSpeakerProblems(entry: DialogueEntry): List<VerifyProblem> {
        val status = CitizensSupport.status
        if (status == CitizensStatus.ACTIVE) return emptyList()
        val speaker = entry.speaker.get() ?: return emptyList()
        if (!CitizensSupport.isReferenceNpc(speaker.javaClass.name)) return emptyList()
        return listOf(
            VerifyProblem(
                subject(entry.name, entry.id),
                "speaks through a Citizens NPC, but Citizens support is ${status.description}.",
            ),
        )
    }

    /** YAML ids can only be checked while LuxDialogues is [luxAvailable]. */
    private fun yamlProblems(
        name: String,
        id: String,
        dialogueId: String,
        page: String,
        luxAvailable: Boolean,
    ): List<VerifyProblem> {
        val subject = subject(name, id)
        val bridge = LuxServices.bridge
        return when {
            dialogueId.isBlank() -> listOf(VerifyProblem(subject, "has no dialogue id."))
            !luxAvailable -> emptyList()
            !bridge.hasDialogue(dialogueId) ->
                listOf(VerifyProblem(subject, "points at LuxDialogues dialogue '$dialogueId', which does not exist."))
            page.isNotBlank() && !bridge.hasPage(dialogueId, page) ->
                listOf(VerifyProblem(subject, "points at page '$page', which dialogue '$dialogueId' does not have."))
            else -> emptyList()
        }
    }

    private fun missingStyleProblems(name: String, id: String, style: Ref<LuxStyleEntry>): List<VerifyProblem> {
        if (!style.isSet || style.get() != null) return emptyList()
        val message = "points at a style that no longer exists; the stock look is used."
        return listOf(VerifyProblem(subject(name, id), message))
    }

    private fun styleOf(ref: Ref<LuxStyleEntry>): LuxStyleEntry =
        if (ref.isSet) ref.get() ?: StyleResolver.DEFAULT else StyleResolver.DEFAULT

    private fun subject(name: String, id: String) = "'$name' ($id)"

    /** Entry names are user text. */
    private fun escape(text: String) = text.replace("<", "\\<")
}
