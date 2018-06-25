package org.gabbard.pandemicgenerator

import java.lang.Math.ceil
import java.util.*

data class RuleSet(val numPlayers: Int, val availableRoles: Set<Role>,
                   val cities: Set<City>, val availableEvents: Set<EventCard>,
                   val availableEpidemics: Set<Epidemic>,
                   val numEpidemicsToUse: Int, val numEventsToUse: Int) {
    fun setupGame(rng: Random): GameState {
        val roles = chooseDistinct(availableRoles, numPlayers, rng)
        val players = roles.map { Player(it) }.toList()

        val infectionDeck = Deck(cities.map { InfectionCard(it) }).shuffled(rng)

        val cityPlayerCards = cities.map { CityPlayerCard(it) }.toSet()
        val events = chooseDistinct(availableEvents, numEventsToUse, rng)

        var deckForPlayerHands = Deck(cityPlayerCards.union(events).toList()).shuffled(rng)
        val deckItems = mutableListOf<Pair<Player, Set<PlayerCard>>>()
        for (player in players) {
            val (cardsDrawn, newDeck) =
                    deckForPlayerHands.draw(initialHandSize(players.size))
            deckForPlayerHands = newDeck
            deckItems.add(player to cardsDrawn.toSet())
        }
        val playerHands = deckItems.toMap()

        val epidemics = chooseDistinct(availableEpidemics, numEpidemicsToUse, rng).toList()

        val cardsPerBatch = ceil(deckForPlayerHands.cards.size / epidemics.size.toDouble()).toInt()
        val batchesToEpidemics = deckForPlayerHands.cards.asSequence().batch(cardsPerBatch).toList()
                .zip(epidemics)
        val shuffledBatches = batchesToEpidemics.map { Deck(it.first.plus(it.second)).shuffled(rng) }
        val playerDeck = Deck(shuffledBatches
                .map { it.cards }.flatten())
        /*val playerDeck = Deck(deckForPlayerHands.cards.asSequence().batch(cardsPerBatch).toList()
                .zip(epidemics).map { Deck(it.first.plus(it.second)).shuffled(rng) }
                .map { it.cards }.flatten())*/

        return GameState(curPlayer = 0, players = players, hands = playerHands,
                infectionDeck = infectionDeck, playerDeck = playerDeck,
                infectionRate = InfectionRate.INITIAL, infectionDiscardPile = setOf())

    }
}

val NATIONAL_CHAMPIONSHIP_RULES = RuleSet(numPlayers = 2,
        availableRoles = COMPETITIVE_PLAY_ROLES,
        cities = ALL_CITIES, availableEvents = COMPETITIVE_PLAY_EVENTS,
        availableEpidemics = CHAMPIONSHIP_VIRULENT_STRAIN_EPIDEMICS, numEpidemicsToUse = 6,
        numEventsToUse = 5)

fun <T> chooseDistinct(items: Collection<T>, numItemsToChoose: Int, rng: Random): Set<T> {
    // lazy implementation is good enough for our needs
    require(items.size >= numItemsToChoose) {
        "Cannot choose $numItemsToChoose distinct " +
                "items from ${items.size} items"
    }
    return items.shuffled(rng).subList(0, numItemsToChoose).toSet()
}

// from https://stackoverflow.com/questions/34498368/kotlin-convert-large-list-to-sublist-of-set-partition-size
public fun <T> Sequence<T>.batch(n: Int): Sequence<List<T>> {
    return BatchingSequence(this, n)
}

private class BatchingSequence<T>(val source: Sequence<T>, val batchSize: Int) : Sequence<List<T>> {
    override fun iterator(): Iterator<List<T>> = object : AbstractIterator<List<T>>() {
        val iterate = if (batchSize > 0) source.iterator() else emptyList<T>().iterator()
        override fun computeNext() {
            if (iterate.hasNext()) setNext(iterate.asSequence().take(batchSize).toList())
            else done()
        }
    }
}