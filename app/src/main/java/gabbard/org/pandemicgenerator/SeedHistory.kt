package gabbard.org.pandemicgenerator

import android.content.Context

/**
 * Stores the most recently used random seeds so players can replay a past setup or share a
 * seed with a friend without having to write it down. Backed by [android.content.SharedPreferences],
 * mirroring the persistence approach used elsewhere in the app (see [GameRepository]).
 */
object SeedHistory {
    private const val PREFS_NAME = "seed_history"
    private const val KEY_SEEDS = "recent_seeds"
    private const val SEPARATOR = ","

    // A reasonable midpoint of the "store the last 10-20 seeds" request.
    private const val MAX_ENTRIES = 15

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the recorded seeds, most-recently-used first. */
    fun recentSeeds(context: Context): List<Long> =
        prefs(context).getString(KEY_SEEDS, null)
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.toLongOrNull() }
            ?: emptyList()

    /**
     * Records [seed] as the most recently used seed. If [seed] is already present in the
     * history, it is moved to the front rather than stored as a duplicate. The history is
     * capped at [MAX_ENTRIES] entries, dropping the oldest seeds first.
     */
    fun record(context: Context, seed: Long) {
        val updated = (listOf(seed) + recentSeeds(context).filter { it != seed }).take(MAX_ENTRIES)
        prefs(context).edit()
            .putString(KEY_SEEDS, updated.joinToString(SEPARATOR))
            .apply()
    }

    /** Clears the seed history. Exposed primarily for tests. */
    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_SEEDS).apply()
    }
}
