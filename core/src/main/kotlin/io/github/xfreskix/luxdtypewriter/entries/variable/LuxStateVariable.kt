package io.github.xfreskix.luxdtypewriter.entries.variable

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.exceptions.ContextDataNotFoundException
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.GenericConstraint
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.VariableData
import com.typewritermc.engine.paper.entry.entries.VarContext
import com.typewritermc.engine.paper.entry.entries.VariableEntry
import com.typewritermc.engine.paper.entry.entries.cast
import com.typewritermc.engine.paper.entry.entries.getData
import io.github.xfreskix.luxdtypewriter.LuxServices
import io.github.xfreskix.luxdtypewriter.bridge.DialogueView

/** The player's live LuxDialogues state as text. The field is chosen per usage. */
@Entry("lux_state_variable", "The player's live LuxDialogues state as text", Colors.GREEN, "mdi:variable")
@GenericConstraint(String::class)
@VariableData(LuxStateVariableData::class)
class LuxStateVariable(
    override val id: String = "",
    override val name: String = "",
) : VariableEntry {

    override fun <T : Any> get(context: VarContext<T>): T {
        val data = context.getData<LuxStateVariableData>()
            ?: throw ContextDataNotFoundException(context.klass, context.data, id)

        val value = stateValue(LuxServices.bridge.view(context.player), data.field) ?: data.default
        return context.cast(value)
    }

    private fun stateValue(view: DialogueView, field: LuxStateField): String? {
        if (view !is DialogueView.Open) return null
        return when (field) {
            LuxStateField.DIALOGUE_ID -> view.dialogueId
            LuxStateField.PAGE_ID -> view.pageId
            LuxStateField.SELECTED_ANSWER -> view.selectedAnswer.takeIf { it > 0 }?.toString()
        }
    }
}

private data class LuxStateVariableData(
    @Help("Which piece of LuxDialogues state to display.")
    val field: LuxStateField = LuxStateField.DIALOGUE_ID,

    @Help("Text shown when the state is unavailable, e.g. the player is not in a dialogue.")
    val default: String = "",
)
