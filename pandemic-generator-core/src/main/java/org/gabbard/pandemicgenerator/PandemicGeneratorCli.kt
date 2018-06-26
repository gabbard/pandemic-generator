package org.gabbard.pandemicgenerator

import java.util.*

// currently hardcoded for national championship rules

// TODO: switch to Guava bimap
val commandToTransition = mapOf("i" to Transition.INFECT, "p" to Transition.DRAW_PLAYER_CARDS)
val transitionToCommand = mapOf(Transition.INFECT to "i", Transition.DRAW_PLAYER_CARDS to "p")

fun main(args: Array<String>) {
    print("Enter random seed: ")
    val rng = Random(readLine()!!.toLong())
    val initialState = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)

    print(initialState.untrackableState.hands.entries.joinToString(separator = "\n")
    { "${it.key}'s hand is ${it.value}" })
    print("\n")

    print("Initial board state: ${initialState.untrackableState.board}\n")
    val history = Stack<TrackableState>()

    var curState = initialState.trackableState
    history.push(curState)
    transitionToCommand
    fun undo() {
        print("Undoing one step\n")
        curState = history.pop()
    }

    while (true) {
        print("Available commands: " + curState.legalTransitions()
                .map { "${it.humanName} ${transitionToCommand[it]}" }
                .joinToString(separator = "; ") + "> ")
        val command = readLine()!!
        print("\n")
        val transition = commandToTransition[command]
        if (transition != null) {
            val transitionResult = curState.executeTransition(transition, rng)
            curState = transitionResult.newGameState
            print("transitionResult.message\n")
        }
    }
}