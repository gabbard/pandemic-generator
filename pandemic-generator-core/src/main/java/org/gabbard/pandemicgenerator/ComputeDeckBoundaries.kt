package org.gabbard.pandemicgenerator

import com.google.common.collect.HashMultiset
import java.util.*


const val NUM_CARDS_FOR_PLAYER_HANDS = 2 * 4


fun main(args: Array<String>) {
    val numSamples = 10000
    val rng = Random(0)

    val ruleSet = NATIONAL_CHAMPIONSHIP_RULES
    val deckAfterPlayerHands =
            ruleSet.createDeckForPlayerHands(rng).draw(NUM_CARDS_FOR_PLAYER_HANDS).second
    val stacks =
            deckAfterPlayerHands.splitAsEvenlyAsPossible(ruleSet.numEpidemicsToUse).toMutableList()

    val stackIndexToFirstCardIndex =
            MutableList(ruleSet.numEpidemicsToUse) { _ -> HashMultiset.create<Int>() }

    (1..numSamples).forEach {
        stacks.shuffle(rng)
        var cardsSoFar = 0
        stacks.forEachIndexed { index, stack ->
            stackIndexToFirstCardIndex[index].add(cardsSoFar)
            cardsSoFar += stack.size
        }
    }

    stackIndexToFirstCardIndex.withIndex().forEach {
        print("\nStack ${it.index}: " +
                it.value.entrySet().sortedBy { it.element }
                        .joinToString(", ") {
                            "${it.element} = ${it.count / numSamples.toDouble()}"
                        })
    }
}