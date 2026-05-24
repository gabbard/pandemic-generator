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
    is EventCard      -> addDotRow(android.graphics.Color.rgb(56, 142, 60), card.name)  // Green 700
    is Epidemic       -> addDotRow(android.graphics.Color.rgb(183, 28, 28), card.userString)  // Red 900
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
