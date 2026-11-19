package io.github.xfreskix.luxdtypewriter.session

/** Session callbacks, called from event threads: keep them quick. */
interface LuxSessionListener {
    fun onStarted(session: LuxSession) {}

    fun onPageChanged(session: LuxSession, pageId: String) {}

    fun onAnswered(session: LuxSession, answerId: String) {}

    /** @param select `true` for a right-click. */
    fun onClick(session: LuxSession, select: Boolean) {}

    /** Called once per session. [completed] is `true` for a natural ending. */
    fun onEnded(session: LuxSession, completed: Boolean)
}
