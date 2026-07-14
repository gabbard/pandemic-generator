package gabbard.org.pandemicgenerator

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SeedHistoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        SeedHistory.clear(context)
    }

    // ── recentSeeds ───────────────────────────────────────────────────────────

    @Test
    fun recentSeedsIsEmptyByDefault() {
        assertTrue(SeedHistory.recentSeeds(context).isEmpty())
    }

    @Test
    fun recordAddsSeedToHistory() {
        SeedHistory.record(context, 42L)
        assertEquals(listOf(42L), SeedHistory.recentSeeds(context))
    }

    @Test
    fun mostRecentlyRecordedSeedIsFirst() {
        SeedHistory.record(context, 1L)
        SeedHistory.record(context, 2L)
        SeedHistory.record(context, 3L)
        assertEquals(listOf(3L, 2L, 1L), SeedHistory.recentSeeds(context))
    }

    @Test
    fun reRecordingASeedMovesItToFrontWithoutDuplicating() {
        SeedHistory.record(context, 1L)
        SeedHistory.record(context, 2L)
        SeedHistory.record(context, 3L)
        SeedHistory.record(context, 1L)
        assertEquals(listOf(1L, 3L, 2L), SeedHistory.recentSeeds(context))
    }

    @Test
    fun historyIsCappedAtFifteenEntries() {
        (1L..20L).forEach { SeedHistory.record(context, it) }
        val recent = SeedHistory.recentSeeds(context)
        assertEquals(15, recent.size)
        // Most recently recorded seeds (20 down to 6) should be kept, oldest dropped.
        assertEquals((20L downTo 6L).toList(), recent)
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    fun clearRemovesAllHistory() {
        SeedHistory.record(context, 1L)
        SeedHistory.clear(context)
        assertTrue(SeedHistory.recentSeeds(context).isEmpty())
    }
}
