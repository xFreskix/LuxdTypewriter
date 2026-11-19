package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.entries.Ref
import com.typewritermc.engine.paper.entry.entries.DialogueEntry
import io.github.xfreskix.luxdtypewriter.style.LuxStyleEntry

/** Common shape of `lux_dialogue`, `lux_message` and `lux_option`. */
interface LuxDialogueSource : DialogueEntry {
    val style: Ref<LuxStyleEntry>
    val pages: List<LuxPage>
}
