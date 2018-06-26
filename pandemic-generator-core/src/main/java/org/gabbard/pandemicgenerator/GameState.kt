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

data class CityState(val infections: Map<Color, Int>) {
    init {
        require(infections.values.all { it in 0..3 }) { "Illegal number of cubes: $infections" }
    }
}

data class BoardState(val cityStates: Map<City, CityState>)

data class TrackableState(val curPlayer: Int,
                          val players: List<Player>,
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
        require(infectionDeck.cards.plus(infectionDiscardPile).map { it.city }.toSet()
                == ALL_CITIES)
    }

    data class DrawPlayerCardsResult(val drawnCards: List<PlayerCard>, val newState: TrackableState)

    fun drawPlayerCards(): DrawPlayerCardsResult {
        val (drawnCards, newPlayerCardDeck) = playerDeck.draw(2)
        return DrawPlayerCardsResult(drawnCards, copy(playerDeck = newPlayerCardDeck))
    }

    fun infect(): Pair<TrackableState, List<City>> {
        val (infectionCards, newInfectionDeckState) =
                infectionDeck.draw(infectionRate.numInfectionCardsToDraw)

        return Pair(copy(infectionDeck = newInfectionDeckState,
                infectionDiscardPile = infectionDiscardPile.union(infectionCards).toSet()),
                infectionCards.map { it.city }.toList())
    }

    data class EpidemicExecutionResult(val newCity: City, val newGameState: TrackableState)

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

data class UntrackableState(val board: BoardState, val hands: Map<Player, Set<PlayerCard>>)

data class GameState(val trackableState: TrackableState, val untrackableState: UntrackableState) {
    init {
        require(trackableState.players.toSet() == untrackableState.hands.keys)
        require(untrackableState.hands.values.all
        { it.size == initialHandSize(trackableState.players.size) })
        { "Illegal hand sizes: ${untrackableState.hands}" }
    }
}
