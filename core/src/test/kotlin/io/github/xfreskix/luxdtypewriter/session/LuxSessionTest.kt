package io.github.xfreskix.luxdtypewriter.session

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/** The session state machine, which keeps triggers from firing twice. */
class LuxSessionTest {

    private class RecordingListener : LuxSessionListener {
        val endedCalls = AtomicInteger(0)
        var lastCompleted: Boolean? = null
        override fun onEnded(session: LuxSession, completed: Boolean) {
            endedCalls.incrementAndGet()
            lastCompleted = completed
        }
    }

    private fun session(policy: CompletionPolicy, listener: LuxSessionListener) =
        LuxSession(UUID.randomUUID(), "tw_test_1", policy, listener)

    @Test
    fun `a natural ending reports completed`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.EXPLICIT, listener)

        session.markStarted()
        session.markCompleting()
        session.end()

        assertEquals(1, listener.endedCalls.get())
        assertEquals(true, listener.lastCompleted)
    }

    @Test
    fun `an abort reports not completed`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.EXPLICIT, listener)

        session.markStarted()
        session.markAborting()
        session.end()

        assertEquals(false, listener.lastCompleted)
    }

    @Test
    fun `end is idempotent`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.EXPLICIT, listener)

        session.markStarted()
        session.markCompleting()

        assertTrue(session.end())
        assertFalse(session.end())
        assertFalse(session.end())

        assertEquals(1, listener.endedCalls.get(), "onEnded must fire exactly once")
    }

    @Test
    fun `abort cannot downgrade a completed session`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.EXPLICIT, listener)

        session.markStarted()
        session.markCompleting()
        // A teardown arriving after the ending must not turn it into an abort.
        session.markAborting()
        session.end()

        assertEquals(true, listener.lastCompleted)
    }

    @Test
    fun `explicit policy treats a bare stop as not completed`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.EXPLICIT, listener)

        session.markStarted()
        // No markCompleting: the window just closed.
        session.end()

        assertEquals(false, listener.lastCompleted)
    }

    @Test
    fun `natural-stop policy treats an unrequested stop as completed`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.ON_NATURAL_STOP, listener)

        session.markStarted()
        session.end()

        assertEquals(true, listener.lastCompleted, "a YAML dialogue closing on its own means finished")
    }

    @Test
    fun `natural-stop policy treats a requested stop as not completed`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.ON_NATURAL_STOP, listener)

        session.markStarted()
        session.markCloseRequested()
        session.end()

        assertEquals(false, listener.lastCompleted, "we closed it, so it was not finished by the player")
    }

    @Test
    fun `progress timestamp advances on page change`() {
        val session = session(CompletionPolicy.EXPLICIT, RecordingListener())
        session.markStarted()
        val before = session.lastProgressAt
        Thread.sleep(5)
        session.markPageChanged("p2")
        assertTrue(session.lastProgressAt >= before)
        assertEquals("p2", session.currentPageId)
    }

    @Test
    fun `concurrent end calls still fire onEnded once`() {
        val listener = RecordingListener()
        val session = session(CompletionPolicy.EXPLICIT, listener)
        session.markStarted()
        session.markCompleting()

        val threads = 32
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        repeat(threads) {
            pool.submit {
                start.await()
                session.end()
            }
        }
        start.countDown()
        pool.shutdown()
        pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)

        assertEquals(1, listener.endedCalls.get(), "32 threads racing to end must still fire once")
    }

    @Test
    fun `a yaml session follows a redirect between yaml dialogues`() {
        val session = session(CompletionPolicy.ON_NATURAL_STOP, RecordingListener())
        assertEquals(RedirectOutcome.FOLLOW, session.markRedirected("tw_test_1", "shop"))
        assertEquals("shop", session.dialogueId)
    }

    @Test
    fun `a generated session aborts when redirected away`() {
        val session = session(CompletionPolicy.EXPLICIT, RecordingListener())
        assertEquals(RedirectOutcome.ABORT, session.markRedirected("tw_test_1", "shop"))
        assertEquals("tw_test_1", session.dialogueId)
    }

    @Test
    fun `a redirect that does not leave the session is ignored`() {
        val session = session(CompletionPolicy.ON_NATURAL_STOP, RecordingListener())
        assertEquals(RedirectOutcome.IGNORE, session.markRedirected("other", "shop"))
        assertEquals(RedirectOutcome.IGNORE, session.markRedirected("tw_test_1", "tw_test_1"))
    }
}
