package org.gabbard.pandemicgenerator

import java.util.*

data class RuleSet(val numPlayers: Int, val availableRoles: Set<Role>,
                   val cities: Set<City>, val availableEvents: Set<EventCard>,
                   val availableEpidemics: Set<Epidemic>,
                   val numEpidemicsToUse: Int, val numEventsToUse: Int) {
    fun setupGame(rng: Random): GameState {
        val roles = chooseDistinct(availableRoles, numPlayers, rng)
        val players = roles.map { Player(it) }.toList()

        val infectionDeck = Deck(cities.map { InfectionCard(it) }).shuffled(rng)

        var deckForPlayerHands = createDeckForPlayerHands(rng)
        val deckItems = mutableListOf<Pair<Player, Set<PlayerCard>>>()
        for (player in players) {
            val (cardsDrawn, newDeck) =
                    deckForPlayerHands.draw(initialHandSize(players.size))
            deckForPlayerHands = newDeck
            deckItems.add(player to cardsDrawn.toSet())
        }
        val playerHands = deckItems.toMap()

        val epidemics = chooseDistinct(availableEpidemics, numEpidemicsToUse, rng).toList()

        val playerDeck = Deck(
                deckForPlayerHands.splitAsEvenlyAsPossible(epidemics.size)
                        // add an epidemic to each stack and shuffle it
                .zip(epidemics).map { Deck(it.first.plus(it.second)).shuffled(rng) }
                .map { it.cards }.flatten())

        // initial epidemic cards
        val (initialInfectionCards, newInfectionDeckState) = infectionDeck.draw(9)
        val initialBoardState = BoardState(
                initialInfectionCards.subList(0, 3).map { it to 3 }
                        .plus(initialInfectionCards.subList(3, 6).map { it to 2 })
                        .plus(initialInfectionCards.subList(6, 9).map { it to 1 })
                        .map { it.first.city to CityState(mapOf(it.first.city.color to it.second)) }
                        .toMap())

        return GameState(
                trackableState = TrackableState(curPlayer = 0, players = players,
                        infectionDeck = newInfectionDeckState, playerDeck = playerDeck,
                        infectionRate = InfectionRate.INITIAL,
                        infectionDiscardPile = initialInfectionCards.toSet(),
                        lastTransition = Transition.INFECT),
                untrackableState = UntrackableState(hands = playerHands,
                        board = initialBoardState))
    }

    // exposed for ComputeDeckBoundaries
    fun createDeckForPlayerHands(rng: Random): Deck<PlayerCard> {
        val cityPlayerCards = cities.map { CityPlayerCard(it) }.toSet()
        val events = chooseDistinct(availableEvents, numEventsToUse, rng)

        var deckForPlayerHands = Deck(cityPlayerCards.union(events).toList()).shuffled(rng)
        return deckForPlayerHands
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

