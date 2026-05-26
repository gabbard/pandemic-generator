package org.gabbard.pandemicgenerator

import java.io.Serializable

data class Role(val name: String) : Serializable {
    override fun toString(): String = name
}

val BASE_ROLES = setOf("Dispatcher", "Researcher", "Medic", "Operations Expert", "Scientist")
        .map { Role(it) }.toSet()
val SECOND_EDITION_ROLES = setOf("Contingency Planner", "Quarantine Specialist")
        .map { Role(it) }.toSet()
val ON_THE_BRINK_ROLES = setOf("Containment Specialist", "Field Operative", "Archivist",
        "Generalist", "Epidemiologist").map { Role(it) }.toSet()
val COMPETITIVE_PLAY_ROLES = BASE_ROLES.union(ON_THE_BRINK_ROLES).union(SECOND_EDITION_ROLES)
        .toSet()
// Troubleshooter requires viewing the top N infection cards, which is not yet implemented.
val INFECTION_DECK_INTERACTION_ROLES: Set<Role> = setOf(Role("Troubleshooter"))
val ALL_ROLES = BASE_ROLES.union(SECOND_EDITION_ROLES).union(ON_THE_BRINK_ROLES).union(INFECTION_DECK_INTERACTION_ROLES)
val MULTI_BOARD_COMPATIBLE_ROLES: Set<Role> = ALL_ROLES - INFECTION_DECK_INTERACTION_ROLES