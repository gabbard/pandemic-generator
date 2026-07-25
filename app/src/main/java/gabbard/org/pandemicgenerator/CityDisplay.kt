package gabbard.org.pandemicgenerator

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.gabbard.pandemicgenerator.City
import org.gabbard.pandemicgenerator.CityPlayerCard
import org.gabbard.pandemicgenerator.Epidemic
import org.gabbard.pandemicgenerator.EventCard
import org.gabbard.pandemicgenerator.PlayerCard
import org.gabbard.pandemicgenerator.seededColorTiebreakOrder
import org.gabbard.pandemicgenerator.Color as GameColor

fun GameColor.toAndroidColor(): Int = when (this) {
    GameColor.BLUE   -> android.graphics.Color.rgb(25,  118, 210)  // Blue 700
    GameColor.YELLOW -> android.graphics.Color.rgb(255, 160,   0)  // Amber 700
    GameColor.BLACK  -> android.graphics.Color.rgb(66,   66,  66)  // Grey 800
    GameColor.RED    -> android.graphics.Color.rgb(211,  47,  47)  // Red 700
}

private fun LinearLayout.addDotRow(dotColor: Int, label: String, detail: String? = null) {
    val dp = context.resources.displayMetrics.density
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, (6 * dp).toInt(), 0, (6 * dp).toInt())
    }

    val dot = View(context).apply {
        layoutParams = LinearLayout.LayoutParams((18 * dp).toInt(), (18 * dp).toInt()).also {
            it.marginEnd = (12 * dp).toInt()
        }
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(dotColor)
        }
    }

    val nameView = TextView(context).apply {
        text = label
        textSize = 16f
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    row.addView(dot)
    row.addView(nameView)

    if (detail != null) {
        row.addView(TextView(context).apply {
            text = detail
            textSize = 13f
            setTextColor(android.graphics.Color.GRAY)
        })
    }

    addView(row)
}

fun LinearLayout.addCityRow(city: City, detail: String? = null) =
    addDotRow(city.color.toAndroidColor(), city.name, detail)

fun LinearLayout.addPlayerCardRow(card: PlayerCard) = when (card) {
    is CityPlayerCard -> addCityRow(card.city)
    is EventCard      -> addDotRow(android.graphics.Color.rgb(245, 124, 0), card.name)  // Orange 700
    is Epidemic       -> addDotRow(android.graphics.Color.rgb(56, 142, 60), card.userString)  // Green 700
}

const val TURN_DIVIDER_TAG = "turn_divider"

fun LinearLayout.addDivider() {
    val dp = context.resources.displayMetrics.density
    addView(
        View(context).apply {
            tag = TURN_DIVIDER_TAG
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * dp).toInt()
            ).also { it.topMargin = (16 * dp).toInt() }
            setBackgroundColor(android.graphics.Color.LTGRAY)
        }
    )
}

fun LinearLayout.addSectionHeader(text: String) {
    val dp = context.resources.displayMetrics.density
    addView(TextView(context).apply {
        this.text = text
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        setTextColor(android.graphics.Color.GRAY)
        setPadding(0, (14 * dp).toInt(), 0, (4 * dp).toInt())
    })
}

fun LinearLayout.addTextRow(text: String) {
    val dp = context.resources.displayMetrics.density
    addView(TextView(context).apply {
        this.text = text
        textSize = 13f
        setPadding(0, (2 * dp).toInt(), 0, (2 * dp).toInt())
    })
}

/**
 * Explains, for the first Virulent Strain epidemic drawn in a game, how to determine which
 * disease color becomes virulent. Cube counts are compared only after the city drawn from the
 * bottom of the infection deck (shown just above this note) has had its cubes placed, and ties
 * are broken using a color ranking seeded from this game's seed so it's reproducible.
 */
fun LinearLayout.addFirstEpidemicVirulentStrainExplanation(seed: Long) {
    addSectionHeader("Virulent Strain determination")
    addTextRow(
        "This is the first Epidemic, so it determines which disease becomes virulent for " +
            "the rest of the game. Count cubes on the board AFTER placing cubes for the city " +
            "just infected from the bottom of the infection deck above. The color with the " +
            "most cubes becomes the virulent strain."
    )
    val tiebreakOrder = seededColorTiebreakOrder(seed).joinToString(" > ") { it.name }
    addTextRow("If there is a tie, use this seeded color ranking to break it:")
    addTextRow("Using randomly seeded color ranking: $tiebreakOrder")
}
