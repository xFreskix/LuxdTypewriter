package io.github.xfreskix.luxdtypewriter.entries.dialogue

import com.typewritermc.core.entries.Entry

/** `'name' (id)` for log lines. */
internal val Entry.label: String get() = "'$name' ($id)"
