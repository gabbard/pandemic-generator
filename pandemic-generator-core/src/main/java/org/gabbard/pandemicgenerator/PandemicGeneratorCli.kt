package org.gabbard.pandemicgenerator

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Paths
import java.util.*

// currently hardcoded for national championship rules

// TODO: switch to Guava bimap
val commandToTransition = mapOf("i" to Transition.INFECT, "p" to Transition.DRAW_PLAYER_CARDS)
val transitionToCommand = mapOf(Transition.INFECT to "i", Transition.DRAW_PLAYER_CARDS to "p")


fun messageForTransitionResult(result: TrackableState.TransitionResult): String {
    return when (result) {
        is TrackableState.TransitionResult.Success.InfectionTransitionResult ->
            "The following cities were infected: ${result.infectedCities}"
        is TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult -> {
            val msg = StringBuilder()
            msg.append("Drew: ${result.cardsDrawn}\n\n")
            for ((epidemic, infectedCity) in result.epidemicsAndInfectedCities) {
                msg.append("$epidemic infects $infectedCity\n\n")
            }
            msg.toString()
        }
        is TrackableState.TransitionResult.PlayerDeckExhausted ->
            "GAME OVER: The player deck ran out of cards. You lost!"
    }
}

fun messageForGameEvent(event: GameEvent): String = when (event) {
    is GameEvent.InfectionEvent -> "Infected cities: ${event.infectedCities}"
    is GameEvent.DrawPlayerCardsEvent -> {
        val msg = StringBuilder("Drew: ${event.cardsDrawn}")
        for ((epidemic, infectedCity) in event.epidemicsAndInfectedCities) {
            msg.append("; $epidemic infects $infectedCity")
        }
        msg.toString()
    }
}
fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    print("Enter random seed: ")
    val rng = Random(readLine()!!.toLong())
    val initialState = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)

    print(initialState.untrackableState.hands.entries.joinToString(separator = "\n")
    { "${it.key}'s hand is ${it.value}" })
    print("\n\n\n")

    print("Initial board state: ${initialState.untrackableState.board}\n")
    val history = ArrayDeque<TrackableState>()

    fun save() {
        print("What file to save to?")
        val destination = Paths.get(readLine()!!.trim())
        ObjectOutputStream(FileOutputStream(destination.toFile())).use { it.writeObject(history) }
    }

    fun load() {
        print("What file to load from?")
        val source = Paths.get(readLine()!!.trim())
        history.clear()
        val savedHistory = ObjectInputStream(FileInputStream(source.toFile()))
                .use {
                    @Suppress("UNCHECKED_CAST")
                    it.readObject() as Deque<TrackableState>
                }
        history.addAll(savedHistory)
    }

    fun showLog() {
        val log = history.peek().eventLog
        if (log.isEmpty()) {
            print("No events yet.\n")
        } else {
            log.forEachIndexed { i, event -> print("${i + 1}. ${messageForGameEvent(event)}\n") }
        }
    }

    val specialCommands = mapOf("s" to ::save, "l" to ::load, "g" to ::showLog)

    history.push(initialState.trackableState)
    transitionToCommand
    fun undo() {
        print("Undoing one step\n")
        history.pop()
    }

    while (true) {
        val curState = history.peek()
        print("Available commands: " + curState.legalTransitions()
                .map { "${it.humanName} ${transitionToCommand[it]}" }
                .joinToString(separator = "; ") + "> ")
        val command = readLine()!!
        print("\n")
        val specialCommand = specialCommands[command]

        if (specialCommand != null) {
            specialCommand()
        } else {
            val transition = commandToTransition[command]
            if (transition != null) {
                val transitionResult = curState.executeTransition(transition, rng)
                print("${messageForTransitionResult(transitionResult)}\n")
                if (transitionResult is TrackableState.TransitionResult.PlayerDeckExhausted) {
                    break
                }
                history.push((transitionResult as TrackableState.TransitionResult.Success).newGameState)
            }
        }
    }
}