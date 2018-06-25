package org.gabbard.pandemicgenerator

import java.util.*

// currently hardcoded for national championship rules

val PLAYER_DRAW_PHASE = 0
val INFECT_PHASE = 1


fun main(args: Array<String>) {
    print("Enter random seed: ")
    val rng = Random(readLine()!!.toLong())
    val (initialState, initialInfections) = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
            .drawInitialEpidemicCards()

    print(initialState.hands.entries.joinToString(separator = "\n")
    { "${it.key}'s hand is ${it.value}" })
    print("\n")

    print("Initial infections: $initialInfections\n")
    val history = Stack<GameState>()
    history.push(initialState)

    var curState = initialState

    var phase = 0

    fun togglePhase() {
        phase = (phase + 1) % 2
    }

    fun undo() {
        print("Undoing one step\n")
        curState = history.pop()
        togglePhase()
    }

    fun executePhase() {
        when (phase) {
            PLAYER_DRAW_PHASE -> {
                val (cardsDrawn, stateAfterCardDraw) = curState.drawPlayerCards()
                curState = stateAfterCardDraw
                val epidemics = cardsDrawn.filterIsInstance<Epidemic>()
                print("Drew: $cardsDrawn\n\n")
                for (epidemic in epidemics) {
                    print("Executing $epidemic\n")
                    val (newCity, postEpidemicGameState) = curState.executeEpidemic(rng)
                    print("Epidemic infects $newCity\n\n")
                    curState = postEpidemicGameState
                }
            }
            INFECT_PHASE -> {
                val (newState, cards) = curState.infect()
                print("Infection cards drawn: $cards\n\n")
                curState = newState
            }
        }
        history.push(curState)
        togglePhase()
    }

    // TODO: player hands need to be migrated to a separate state object because we won't track
    // those, unlike the decks

    while (true) {
        val nextPhaseName = when (phase) {
            PLAYER_DRAW_PHASE -> "player draw phase"
            INFECT_PHASE -> "infection phase"
            else -> throw RuntimeException("Unknown phase")
        }
        print("Proceed to $nextPhaseName? (a to proceed, u to back up)")
        val command = readLine()!!
        when (command) {
            "a" -> executePhase()
            "u" -> undo()
            else -> print("Unknown command $command\n")
        }
    }
}