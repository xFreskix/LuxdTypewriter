package io.github.xfreskix.luxdtypewriter.entries.event

import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.interaction.EntryContextKey
import kotlin.reflect.KClass

/** Published by [OnLuxAnswerEntry]; the text is stripped of formatting. */
enum class LuxAnswerEventContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(String::class)
    DIALOGUE_ID(String::class),

    @KeyType(String::class)
    ANSWER_ID(String::class),

    @KeyType(String::class)
    ANSWER_TEXT(String::class),
}
