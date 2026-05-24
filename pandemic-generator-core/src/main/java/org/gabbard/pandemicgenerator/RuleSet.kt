package org.gabbard.pandemicgenerator

import java.io.Serializable

data class Difficulty(val name: String, val numEpidemics: Int) : Serializable

data class GameOptions(val numPlayers: Int, val difficulty: Difficulty) : Serializable

data class RuleSet(
    val name: String,
    val availableRoles: Set<Role>,
    val availableEpidemics: Set<Epidemic>,
    val availableEvents: Set<EventCard>,
    val numEventsToUse: Int,
    val availableDifficulties: List<Difficulty>,
    val allowedPlayerCounts: List<Int>,
    val cities: Set<City> = ALL_CITIES
) : Serializable {

    fun buildGameRules(options: GameOptions): GameRules {
        require(options.numPlayers in allowedPlayerCounts) {
            "Player count ${options.numPlayers} not allowed. Allowed: $allowedPlayerCounts"
        }
        require(options.difficulty in availableDifficulties) {
            "Difficulty ${options.difficulty.name} not available."
        }
        return GameRules(
            numPlayers = options.numPlayers,
            availableRoles = availableRoles,
            cities = cities,
            availableEvents = availableEvents,
            availableEpidemics = availableEpidemics,
            numEpidemicsToUse = options.difficulty.numEpidemics,
            numEventsToUse = numEventsToUse
        )
    }
}

val NATIONAL_CHAMPIONSHIP_DIFFICULTY = Difficulty("Championship", 6)

val NATIONAL_CHAMPIONSHIP = RuleSet(
    name = "National Championship",
    availableRoles = COMPETITIVE_PLAY_ROLES,
    availableEpidemics = CHAMPIONSHIP_VIRULENT_STRAIN_EPIDEMICS,
    availableEvents = COMPETITIVE_PLAY_EVENTS,
    numEventsToUse = 5,
    availableDifficulties = listOf(NATIONAL_CHAMPIONSHIP_DIFFICULTY),
    allowedPlayerCounts = listOf(2)
)

val STANDARD_PANDEMIC = RuleSet(
    name = "Standard Pandemic",
    availableRoles = BASE_ROLES.union(SECOND_EDITION_ROLES),
    availableEpidemics = STANDARD_PANDEMIC_EPIDEMICS,
    availableEvents = STANDARD_PANDEMIC_EVENTS,
    numEventsToUse = 5,
    availableDifficulties = listOf(
        Difficulty("Introductory", 4),
        Difficulty("Normal", 5),
        Difficulty("Heroic", 6)
    ),
    allowedPlayerCounts = listOf(2, 3, 4)
)

val BUILT_IN_RULE_SETS: List<RuleSet> = listOf(STANDARD_PANDEMIC, NATIONAL_CHAMPIONSHIP)

// backward-compatible name used by CLI and tests
val NATIONAL_CHAMPIONSHIP_RULES: GameRules = NATIONAL_CHAMPIONSHIP.buildGameRules(
    GameOptions(numPlayers = 2, difficulty = NATIONAL_CHAMPIONSHIP_DIFFICULTY)
)
