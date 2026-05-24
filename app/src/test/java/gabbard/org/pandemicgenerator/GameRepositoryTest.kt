package gabbard.org.pandemicgenerator

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.ALL_CITIES
import org.gabbard.pandemicgenerator.CityPlayerCard
import org.gabbard.pandemicgenerator.Deck
import org.gabbard.pandemicgenerator.InfectionCard
import org.gabbard.pandemicgenerator.InfectionRate
import org.gabbard.pandemicgenerator.Player
import org.gabbard.pandemicgenerator.Role
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GameRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        GameRepository.clear(context)
    }

    // ── exists ────────────────────────────────────────────────────────────────

    @Test
    fun existsReturnsFalseWhenNoGameSaved() {
        assertFalse(GameRepository.exists(context))
    }

    @Test
    fun existsReturnsTrueAfterSave() {
        GameRepository.save(context, makeSession())
        assertTrue(GameRepository.exists(context))
    }

    @Test
    fun existsReturnsFalseAfterClear() {
        GameRepository.save(context, makeSession())
        GameRepository.clear(context)
        assertFalse(GameRepository.exists(context))
    }

    // ── load ──────────────────────────────────────────────────────────────────

    @Test
    fun loadReturnsNullWhenNoGameSaved() {
        assertNull(GameRepository.load(context))
    }

    @Test
    fun loadReturnsNullAfterClear() {
        GameRepository.save(context, makeSession())
        GameRepository.clear(context)
        assertNull(GameRepository.load(context))
    }

    @Test
    fun loadReturnsNonNullAfterSave() {
        GameRepository.save(context, makeSession())
        assertNotNull(GameRepository.load(context))
    }

    // ── save/load round-trip ──────────────────────────────────────────────────

    @Test
    fun saveAndLoadRoundTripsTrackableState() {
        val session = makeSession(seed = 12345L)
        GameRepository.save(context, session)
        val loaded = GameRepository.load(context)!!
        assertEquals(session.trackableState, loaded.trackableState)
    }

    @Test
    fun saveAndLoadRoundTripsSeed() {
        val session = makeSession(seed = 99999L)
        GameRepository.save(context, session)
        val loaded = GameRepository.load(context)!!
        assertEquals(99999L, loaded.seed)
    }

    @Test
    fun secondSaveOverwritesFirst() {
        val first = makeSession(seed = 1L)
        val second = makeSession(seed = 2L)
        GameRepository.save(context, first)
        GameRepository.save(context, second)
        val loaded = GameRepository.load(context)!!
        assertEquals(2L, loaded.seed)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeSession(seed: Long = 42L): GameRepository.GameSession {
        val cities = ALL_CITIES.toList()
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        val state = TrackableState(
            curPlayer = 0,
            players = players,
            infectionDeck = Deck(cities.take(10).map { InfectionCard(it) }),
            infectionDiscardPile = cities.drop(10).map { InfectionCard(it) }.toSet(),
            playerDeck = Deck(cities.take(5).map { CityPlayerCard(it) }),
            infectionRate = InfectionRate.INITIAL,
            lastTransition = Transition.INFECT
        )
        return GameRepository.GameSession(trackableState = state, rng = Random(seed), seed = seed)
    }
}
