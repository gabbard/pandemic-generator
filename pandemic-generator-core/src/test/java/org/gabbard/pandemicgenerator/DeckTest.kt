package org.gabbard.pandemicgenerator

import org.junit.Assert.*
import org.junit.Test
import java.util.Random

class DeckTest {

    private fun deck(vararg names: String) = Deck(names.map { InfectionCard(City(it, Color.BLUE)) })

    @Test
    fun drawTakesFromTop() {
        val (drawn, remaining) = deck("A", "B", "C").draw(2)
        assertEquals(listOf("A", "B"), drawn.map { it.city.name })
        assertEquals(listOf("C"), remaining.cards.map { it.city.name })
    }

    @Test
    fun drawEntireDeck() {
        val (drawn, remaining) = deck("A", "B").draw(2)
        assertEquals(2, drawn.size)
        assertTrue(remaining.cards.isEmpty())
    }

    @Test
    fun drawOneFromBottomReturnsLastCard() {
        val (card, remaining) = deck("A", "B", "C").drawOneFromTheBottom()
        assertEquals("C", card.city.name)
        assertEquals(listOf("A", "B"), remaining.cards.map { it.city.name })
    }

    @Test
    fun placeOnTopOf() {
        val top = deck("A", "B")
        val bottom = deck("C", "D")
        val combined = top.placeOnTopOf(bottom)
        assertEquals(listOf("A", "B", "C", "D"), combined.cards.map { it.city.name })
    }

    @Test
    fun shuffledPreservesSize() {
        val d = deck("A", "B", "C", "D", "E")
        val shuffled = d.shuffled(Random(42))
        assertEquals(5, shuffled.cards.size)
        assertEquals(d.cards.toSet(), shuffled.cards.toSet())
    }

    @Test
    fun splitAsEvenlyAsPossibleUsesRoundRobin() {
        // 6 cards into 3 stacks: card 0→stack0, 1→stack1, 2→stack2, 3→stack0, 4→stack1, 5→stack2
        val stacks = deck("A", "B", "C", "D", "E", "F").splitAsEvenlyAsPossible(3)
        assertEquals(3, stacks.size)
        assertEquals(listOf("A", "D"), stacks[0].map { it.city.name })
        assertEquals(listOf("B", "E"), stacks[1].map { it.city.name })
        assertEquals(listOf("C", "F"), stacks[2].map { it.city.name })
    }

    @Test
    fun splitHandlesUnevenDivision() {
        // 7 cards into 3 stacks: stacks 0 and 1 get 3 cards, stack 2 gets 2 (but note:
        // round-robin means stack0=[0,3,6], stack1=[1,4], stack2=[2,5])
        // Actually: card6 % 3 == 0, so stack0 gets 3, stacks 1 and 2 get 2 each
        val stacks = deck("A","B","C","D","E","F","G").splitAsEvenlyAsPossible(3)
        assertEquals(3, stacks.size)
        val totalCards = stacks.sumOf { it.size }
        assertEquals(7, totalCards)
        // sizes differ by at most 1
        val sizes = stacks.map { it.size }
        assertTrue(sizes.max() - sizes.min() <= 1)
    }
}
