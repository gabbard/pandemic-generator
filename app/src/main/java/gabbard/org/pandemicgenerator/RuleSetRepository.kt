package gabbard.org.pandemicgenerator

import android.content.Context
import org.gabbard.pandemicgenerator.RuleSet
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object RuleSetRepository {
    private const val FILENAME = "custom_rule_sets.ser"

    fun load(context: Context): List<RuleSet> = try {
        context.openFileInput(FILENAME).use { fis ->
            ObjectInputStream(fis).use { ois ->
                @Suppress("UNCHECKED_CAST")
                ois.readObject() as List<RuleSet>
            }
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun save(context: Context, ruleSets: List<RuleSet>) {
        context.openFileOutput(FILENAME, Context.MODE_PRIVATE).use { fos ->
            ObjectOutputStream(fos).use { it.writeObject(ruleSets) }
        }
    }
}
