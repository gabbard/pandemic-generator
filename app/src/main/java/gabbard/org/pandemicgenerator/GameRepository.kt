package gabbard.org.pandemicgenerator

import android.content.Context
import org.gabbard.pandemicgenerator.TrackableState
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.Random

object GameRepository {
    private const val FILENAME = "game_session.ser"

    data class GameSession(
        val trackableState: TrackableState,
        val rng: Random,
        val seed: Long
    ) : Serializable

    fun save(context: Context, session: GameSession) {
        context.openFileOutput(FILENAME, Context.MODE_PRIVATE).use { fos ->
            ObjectOutputStream(fos).use { it.writeObject(session) }
        }
    }

    fun load(context: Context): GameSession? = try {
        context.openFileInput(FILENAME).use { fis ->
            ObjectInputStream(fis).use { ois ->
                @Suppress("UNCHECKED_CAST")
                ois.readObject() as GameSession
            }
        }
    } catch (e: Exception) {
        null
    }

    fun clear(context: Context) {
        context.deleteFile(FILENAME)
    }

    fun exists(context: Context): Boolean = context.fileList().contains(FILENAME)
}
