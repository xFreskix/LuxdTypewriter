package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import kotlin.reflect.KClass

/** Published into the interaction context. */
enum class LuxDialogueContextKeys(override val klass: KClass<*>) : EntryContextKey {
    /** 1-based. */
    @KeyType(Int::class)
    SELECTED_ANSWER(Int::class),

    /** Without formatting. */
    @KeyType(String::class)
    SELECTED_ANSWER_TEXT(String::class),

    /** Last panel page seen, 1-based. */
    @KeyType(Int::class)
    LAST_PAGE(Int::class),
}
