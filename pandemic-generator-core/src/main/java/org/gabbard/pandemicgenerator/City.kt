package org.gabbard.pandemicgenerator

import java.io.Serializable

enum class Color {
    BLUE, YELLOW, BLACK, RED
}

data class City(val name: String, val color: Color) : Serializable

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