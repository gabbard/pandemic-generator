package gabbard.org.pandemicgenerator

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP
import org.gabbard.pandemicgenerator.RuleSet
import org.gabbard.pandemicgenerator.STANDARD_PANDEMIC
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RuleSetRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        // Remove the file so tests are isolated
        context.deleteFile("custom_rule_sets.ser")
    }

    // ── load ──────────────────────────────────────────────────────────────────

    @Test
    fun loadReturnsEmptyListWhenNoFileSaved() {
        assertTrue(RuleSetRepository.load(context).isEmpty())
    }

    @Test
    fun loadReturnsEmptyListAfterCorruptedWrite() {
        // Write garbage bytes to the file so deserialization fails
        context.openFileOutput("custom_rule_sets.ser", Context.MODE_PRIVATE)
            .use { it.write(byteArrayOf(0, 1, 2, 3)) }
        assertTrue("corrupted file should return empty list", RuleSetRepository.load(context).isEmpty())
    }

    // ── save / load round-trip ────────────────────────────────────────────────

    @Test
    fun saveAndLoadRoundTripsEmptyList() {
        RuleSetRepository.save(context, emptyList())
        assertTrue(RuleSetRepository.load(context).isEmpty())
    }

    @Test
    fun saveAndLoadRoundTripsSingleRuleSet() {
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        val loaded = RuleSetRepository.load(context)
        assertEquals(1, loaded.size)
        assertEquals(NATIONAL_CHAMPIONSHIP, loaded[0])
    }

    @Test
    fun saveAndLoadRoundTripsMultipleRuleSets() {
        val ruleSets = listOf(NATIONAL_CHAMPIONSHIP, NATIONAL_CHAMPIONSHIP)
        RuleSetRepository.save(context, ruleSets)
        val loaded = RuleSetRepository.load(context)
        assertEquals(2, loaded.size)
        assertEquals(NATIONAL_CHAMPIONSHIP, loaded[0])
        assertEquals(NATIONAL_CHAMPIONSHIP, loaded[1])
    }

    @Test
    fun secondSaveOverwritesFirst() {
        RuleSetRepository.save(context, listOf(STANDARD_PANDEMIC))
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        val loaded = RuleSetRepository.load(context)
        assertEquals(1, loaded.size)
        assertEquals(NATIONAL_CHAMPIONSHIP, loaded[0])
    }

    @Test
    fun ruleSetNamesArePreservedAfterRoundTrip() {
        RuleSetRepository.save(context, listOf(STANDARD_PANDEMIC, NATIONAL_CHAMPIONSHIP))
        val names = RuleSetRepository.load(context).map(RuleSet::name)
        assertEquals(listOf("Standard Pandemic", "National Championship"), names)
    }
}
