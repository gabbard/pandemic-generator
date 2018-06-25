package org.gabbard.pandemicgenerator

import java.util.*

typealias InitialCubes = Map<City, Int>

enum class InfectionRate(val numInfectionCardsToDraw: Int) {
    INITIAL(2),
    STEP_TWO(2),
    STEP_THREE(2),
    STEP_FOUR(3),
    STEP_FIVE(3),
    STEP_SIX(4),
    STEP_SEVEN(4)
}

fun initialHandSize(numPlayers: Int): Int {
    return 6 - numPlayers
}

data class GameState(
        val curPlayer: Int,
        val players: List<Player>,
        val hands: Map<Player, Set<PlayerCard>>,
        val infectionDeck: Deck<InfectionCard>,
        val infectionDiscardPile: Set<InfectionCard>,
        val playerDeck: Deck<PlayerCard>,
        val infectionRate: InfectionRate) {
    init {
        require(players.size in 2..4)
        require(curPlayer in 0..(players.size - 1))
        { "Number of players must in in [2, 4] but got ${players.size}" }
        val roles = players.map { it.role }.toSet()
        require(roles.size == players.size) { "Some players share roles: $players" }
        require(infectionDeck.cards.plus(infectionDiscardPile).map { it.city }.toSet() == ALL_CITIES)
        require(players.toSet() == hands.keys)
        require(hands.values.all { it.size == initialHandSize(players.size) })
        { "Illegal hand sizes: $hands" }
        val cardsInHands = hands.values.flatten().toSet()
        /*val citiesOnPlayerCards = playerDeck.cards.plus(cardsInHands)
                .filterIsInstance<CityPlayerCard>().map { it.city }.toSet()
        require(citiesOnPlayerCards.toSet() == ALL_CITIES)
        {"Cities not on player cards: ${ALL_CITIES.minus(citiesOnPlayerCards)};" +
                "non-city player cards which claim to be: ${citiesOnPlayerCards.minus(ALL_CITIES)}"}*/
    }

    data class DrawPlayerCardsResult(val drawnCards: List<PlayerCard>, val newState: GameState)

    fun drawPlayerCards(): DrawPlayerCardsResult {
        val (drawnCards, newPlayerCardDeck) = playerDeck.draw(2)
        return DrawPlayerCardsResult(drawnCards, copy(playerDeck = newPlayerCardDeck))
    }

    fun drawInitialEpidemicCards(): Pair<GameState, InitialCubes> {
        val (initialInfectionCards, newInfectionDeckState) = infectionDeck.draw(9)
        val triples = initialInfectionCards.subList(0, 3).map { it.city to 3 }.toMap()
        val doubles = initialInfectionCards.subList(3, 6).map { it.city to 2 }.toMap()
        val singles = initialInfectionCards.subList(6, 9).map { it.city to 1 }.toMap()

        return Pair(copy(infectionDeck = newInfectionDeckState,
                infectionDiscardPile = initialInfectionCards.toSet()),
                singles.plus(doubles).plus(triples).toMap())
    }

    fun infect(): Pair<GameState, List<City>> {
        val (infectionCards, newInfectionDeckState) =
                infectionDeck.draw(infectionRate.numInfectionCardsToDraw)

        return Pair(copy(infectionDeck = newInfectionDeckState,
                infectionDiscardPile = infectionDiscardPile.union(infectionCards).toSet()),
                infectionCards.map { it.city }.toList())
    }

    data class EpidemicExecutionResult(val newCity: City, val newGameState: GameState)

    fun executeEpidemic(rng: Random): EpidemicExecutionResult {
        val newInfectionRate = InfectionRate.values()[infectionRate.ordinal + 1]
        val (infectionCardOffTheBottom, infectionDeckAfterNewCity) = infectionDeck.drawOneFromTheBottom()
        val newInfectionDeck = Deck(infectionDiscardPile.toList().plus(infectionCardOffTheBottom))
                .shuffled(rng).placeOnTopOf(infectionDeckAfterNewCity)
        return EpidemicExecutionResult(infectionCardOffTheBottom.city,
                copy(infectionRate = newInfectionRate, infectionDeck = newInfectionDeck,
                        infectionDiscardPile = setOf()))
    }
}
