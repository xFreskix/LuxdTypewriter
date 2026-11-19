package io.github.xfreskix.luxdtypewriter.bridge

/** The first `start-page` entry, if it names an existing page. */
internal fun unconditionalStartPage(dialogueId: String, startPage: List<String>?, pageIds: Collection<String>): String {
    val first = startPage?.firstOrNull()
    check(first != null && first in pageIds) {
        "LuxDialogues dialogue '$dialogueId' cannot replace an open dialogue without an explicit page. " +
            "Only LuxDialogues can evaluate its start-page, and it does so only when no dialogue is open. " +
            "start-page: $startPage, pages: $pageIds. Give the entry an explicit page."
    }
    return first
}
