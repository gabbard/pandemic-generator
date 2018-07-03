package org.gabbard.pandemicgenerator

import java.io.Serializable
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

data class CityState(val infections: Map<Color, Int>) : Serializable {
    init {
        require(infections.values.all { it in 0..3 }) { "Illegal number of cubes: $infections" }
    }

    override fun toString(): String {
        return infections.toString()
    }
}

data class BoardState(val cityStates: Map<City, CityState>) : Serializable

data class TrackableState(val curPlayer: Int,
                          val players: List<Player>,
                          val infectionDeck: Deck<InfectionCard>,
                          val infectionDiscardPile: Set<InfectionCard>,
                          val playerDeck: Deck<PlayerCard>,
                          val infectionRate: InfectionRate,
                          val lastTransition: Transition) : Serializable {
    init {
        require(players.size in 2..4)
        require(curPlayer in 0..(players.size - 1))
        { "Number of players must in in [2, 4] but got ${players.size}" }
        val roles = players.map { it.role }.toSet()
        require(roles.size == players.size) { "Some players share roles: $players" }
        require(infectionDeck.cards.plus(infectionDiscardPile).map { it.city }.toSet()
                == ALL_CITIES)
    }

    sealed class TransitionResult(open val newGameState: TrackableState) {

        data class InfectionTransitionResult(override val newGameState: TrackableState,
                                             val infectedCities: List<City>)
            : TransitionResult(newGameState)

        data class DrawPlayerCardsTransitionResult(
                override val newGameState: TrackableState,
                val cardsDrawn: List<PlayerCard>,
                val epidemicsAndInfectedCities: List<Pair<Epidemic, City>>) : TransitionResult(newGameState) {
            init {
                require(cardsDrawn.containsAll(epidemicsAndInfectedCities.map { it.first }))
            }
        }
    }


    fun executeTransition(transition: Transition, rng: Random): TransitionResult {
        when (transition) {
            Transition.INFECT -> {
                val infectionResult = infect()
                return TransitionResult.InfectionTransitionResult(
                        infectionResult.newGameState.copy(lastTransition = transition),
                        infectionResult.infectedCities)
            }
            Transition.DRAW_PLAYER_CARDS -> {
                val (cardsDrawn, postDrawState) = drawPlayerCards()
                var curState = postDrawState
                val epidemics = cardsDrawn.filterIsInstance<Epidemic>()

                val epidemicsToCitiesInfected = mutableListOf<Pair<Epidemic, City>>()
                for (epidemic in epidemics) {
                    val (newCity, postEpidemicGameState) = curState.executeEpidemic(rng)
                    curState = postEpidemicGameState
                    epidemicsToCitiesInfected.add(Pair(epidemic, newCity))
                }
                return TransitionResult.DrawPlayerCardsTransitionResult(
                        curState.copy(lastTransition = transition), cardsDrawn,
                        epidemicsToCitiesInfected)
            }
        }
    }

    fun legalTransitions(): Set<Transition> {
        return when (lastTransition) {
            Transition.INFECT -> setOf(Transition.DRAW_PLAYER_CARDS)
            Transition.DRAW_PLAYER_CARDS -> setOf(Transition.INFECT)
        }
    }

    private data class DrawPlayerCardsResult(val drawnCards: List<PlayerCard>, val newState: TrackableState)

    private fun drawPlayerCards(): DrawPlayerCardsResult {
        val (drawnCards, newPlayerCardDeck) = playerDeck.draw(2)
        return DrawPlayerCardsResult(drawnCards, copy(playerDeck = newPlayerCardDeck))
    }

    private data class InfectionResult(val newGameState: TrackableState, val infectedCities: List<City>)

    private fun infect(): InfectionResult {
        val (infectionCards, newInfectionDeckState) =
                infectionDeck.draw(infectionRate.numInfectionCardsToDraw)

        return InfectionResult(copy(infectionDeck = newInfectionDeckState,
                infectionDiscardPile = infectionDiscardPile.union(infectionCards).toSet()),
                infectionCards.map { it.city }.toList())
    }

    private data class EpidemicExecutionResult(val newCity: City, val newGameState: TrackableState)

    private fun executeEpidemic(rng: Random): EpidemicExecutionResult {
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
    : Serializable

data class GameState(val trackableState: TrackableState, val untrackableState: UntrackableState)
    : Serializable {
    init {
        require(trackableState.players.toSet() == untrackableState.hands.keys)
        require(untrackableState.hands.values.all
        { it.size == initialHandSize(trackableState.players.size) })
        { "Illegal hand sizes: ${untrackableState.hands}" }
    }
}

enum class Transition(val humanName: String) {
    DRAW_PLAYER_CARDS("Draw Player Cards"),
    INFECT("Infect cities")
}