package org.gabbard.pandemicgenerator

import com.google.common.collect.HashMultiset
import java.util.*


const val NUM_CARDS_FOR_PLAYER_HANDS = 2 * 4


fun main(args: Array<String>) {
    val numSamples = 10000
    val rng = Random(0)

    val ruleSet = NATIONAL_CHAMPIONSHIP_RULES
    // epidemics are not accounted for in the deck for making player hands, so we offset
    // the number of cards remove for player hands by the number of epidemic to balance it out
    // this result in stacks with different content than the real game, but the same boundaries
    val numCardsToRemoveBeforeStacks = NUM_CARDS_FOR_PLAYER_HANDS - ruleSet.numEpidemicsToUse
    val deckAfterPlayerHands =
            ruleSet.createDeckForPlayerHands(rng).draw(numCardsToRemoveBeforeStacks).second
    val stacks =
            deckAfterPlayerHands.splitAsEvenlyAsPossible(ruleSet.numEpidemicsToUse).toMutableList()

    val stackIndexToFirstCardIndex =
            MutableList(ruleSet.numEpidemicsToUse) { _ -> HashMultiset.create<Int>() }

    // two cards are drawn each turn. turns are 1-indexed for non-CS users :-)
    fun turnForCard(cardNum: Int) = cardNum / 2 + 1

    (1..numSamples).forEach {
        stacks.shuffle(rng)
        var cardsSoFar = 0
        stacks.forEachIndexed { index, stack ->
            stackIndexToFirstCardIndex[index].add(turnForCard(cardsSoFar))
            cardsSoFar += stack.size
        }
    }

    stackIndexToFirstCardIndex.withIndex().forEach {
        print("\nStack ${it.index + 1}: " +
                it.value.entrySet().sortedBy { it.element }
                        .joinToString(", ") {
                            "${it.element} = ${it.count / numSamples.toDouble()}"
                        })
    }
}