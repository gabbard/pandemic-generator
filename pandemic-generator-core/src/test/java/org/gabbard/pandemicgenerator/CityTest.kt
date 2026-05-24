package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CityTest {

    @Test
    fun allCitiesContainsExactly48Cities() {
        assertEquals(48, ALL_CITIES.size)
    }

    @Test
    fun eachColorHasExactlyTwelveCities() {
        for (color in Color.entries) {
            assertEquals("$color should have 12 cities", 12, ALL_CITIES.count { it.color == color })
        }
    }

    @Test
    fun cityNamesAreUniqueAcrossAllColors() {
        val names = ALL_CITIES.map { it.name }
        assertEquals("city names must be unique", names.size, names.toSet().size)
    }

    @Test
    fun allBlueCitiesHaveBlueColor() {
        assertTrue(BLUE_CITIES.all { it.color == Color.BLUE })
    }

    @Test
    fun allYellowCitiesHaveYellowColor() {
        assertTrue(YELLOW_CITIES.all { it.color == Color.YELLOW })
    }

    @Test
    fun allBlackCitiesHaveBlackColor() {
        assertTrue(BLACK_CITIES.all { it.color == Color.BLACK })
    }

    @Test
    fun allRedCitiesHaveRedColor() {
        assertTrue(RED_CITIES.all { it.color == Color.RED })
    }

    @Test
    fun colorGroupsAreDisjoint() {
        val allGroups = listOf(BLUE_CITIES, YELLOW_CITIES, BLACK_CITIES, RED_CITIES)
        for (i in allGroups.indices) {
            for (j in allGroups.indices) {
                if (i != j) {
                    assertTrue(
                        "Color groups $i and $j should be disjoint",
                        allGroups[i].intersect(allGroups[j]).isEmpty()
                    )
                }
            }
        }
    }

    @Test
    fun allCitiesIsUnionOfColorGroups() {
        val union = BLUE_CITIES.union(YELLOW_CITIES).union(BLACK_CITIES).union(RED_CITIES)
        assertEquals(union, ALL_CITIES)
    }
}
