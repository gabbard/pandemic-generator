package org.gabbard.pandemicgenerator

import java.io.Serializable
import java.util.Random

enum class Color {
    BLUE, YELLOW, BLACK, RED
}

/**
 * Returns a deterministic ordering of all four [Color] values, seeded from [seed].
 *
 * This is used to break ties when determining which disease becomes "virulent" under the
 * Virulent Strain challenge (see [Epidemic]): if multiple colors are tied for the most cubes
 * on the board when the first epidemic is resolved, the color that sorts earliest in this list
 * wins. Because the ordering is derived solely from the game's seed, it is reproducible for a
 * given game without needing to be stored anywhere.
 */
fun seededColorTiebreakOrder(seed: Long): List<Color> {
    return Color.entries.shuffled(Random(seed))
}

data class City(val name: String, val color: Color) : Serializable {
    override fun toString(): String {
        return name
    }
}

val BLUE_CITIES = setOf("San Francisco", "Toronto", "Chicago", "Washington",
        "Atlanta", "New York", "Madrid", "Essen", "London", "Paris", "Milan", "St. Petersburg")
        .map { City(it, Color.BLUE) }.toSet()
val YELLOW_CITIES = setOf("Mexico City", "Miami", "Bogota", "Lima", "Santiago", "Sao Paulo",
        "Bueno Aires", "Lagos", "Kinshasa", "Johannesburg", "Khartoum", "Los Angeles")
        .map { City(it, Color.YELLOW) }.toSet()
val BLACK_CITIES = setOf("Moscow", "Istanbul", "Algiers", "Cairo", "Delhi", "Mumbai", "Chennai",
        "Tehran", "Riyadh", "Karachi", "Kolkata", "Baghdad")
        .map { City(it, Color.BLACK) }.toSet()
val RED_CITIES = setOf("Bangkok", "Shanghai", "Beijing", "Tokyo", "Osaka", "Manila",
        "Ho Chi Minh City", "Sydney", "Taipei", "Jakarta", "Seoul", "Hong Kong")
        .map { City(it, Color.RED) }.toSet()
val ALL_CITIES = BLACK_CITIES.union(RED_CITIES).union(YELLOW_CITIES).union(BLUE_CITIES).toSet()