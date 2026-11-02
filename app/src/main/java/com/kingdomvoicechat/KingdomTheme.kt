
package com.kingdomvoicechat

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

object KingdomTheme {

    private const val PREFS = "kingdom_theme_settings"
    private const val KEY_THEME = "selected_theme"
    private const val KEY_CARD = "selected_card_style"

    private data class Palette(
        val background: Int,
        val surface: Int,
        val card: Int,
        val cardLight: Int,
        val white: Int,
        val muted: Int,
        val accent: Int,
        val blue: Int,
        val purple: Int
    )

    private val palettes = mapOf(
        "Royal Blue" to Palette(
            0xFF080D20.toInt(),
            0xFF111A35.toInt(),
            0xFF172344.toInt(),
            0xFF202F55.toInt(),
            Color.WHITE,
            0xFF9BAAC8.toInt(),
            0xFF36D9F5.toInt(),
            0xFF397BFF.toInt(),
            0xFF895BFF.toInt()
        ),
        "Midnight Purple" to Palette(
            0xFF10091F.toInt(),
            0xFF211333.toInt(),
            0xFF2B1945.toInt(),
            0xFF3A2458.toInt(),
            Color.WHITE,
            0xFFC0ACD8.toInt(),
            0xFFD18BFF.toInt(),
            0xFF7652E8.toInt(),
            0xFFB04DDB.toInt()
        ),
        "Ocean Cyan" to Palette(
            0xFF061820.toInt(),
            0xFF0C2733.toInt(),
            0xFF123747.toInt(),
            0xFF1B4859.toInt(),
            Color.WHITE,
            0xFFA3C7D1.toInt(),
            0xFF39E5E5.toInt(),
            0xFF168EBD.toInt(),
            0xFF397BFF.toInt()
        ),
        "Dark Red" to Palette(
            0xFF1A090D.toInt(),
            0xFF2B1119.toInt(),
            0xFF3B1722.toInt(),
            0xFF51212E.toInt(),
            Color.WHITE,
            0xFFD0AAB2.toInt(),
            0xFFFF718C.toInt(),
            0xFFB82E50.toInt(),
            0xFF7D315D.toInt()
        ),
        "Emerald" to Palette(
            0xFF071811.toInt(),
            0xFF10291D.toInt(),
            0xFF173A29.toInt(),
            0xFF21513A.toInt(),
            Color.WHITE,
            0xFFA9CBB6.toInt(),
            0xFF55E69B.toInt(),
            0xFF168B65.toInt(),
            0xFF35B879.toInt()
        )
    )

    private var current = palettes.getValue("Royal Blue")

    var BACKGROUND = current.background
        private set
    var SURFACE = current.surface
        private set
    var CARD = current.card
        private set
    var CARD_LIGHT = current.cardLight
        private set
    var WHITE = current.white
        private set
    var MUTED = current.muted
        private set
    var CYAN = current.accent
        private set
    var BLUE = current.blue
        private set
    var PURPLE = current.purple
        private set

    fun selectedTheme(activity: Activity): String {
        return activity.getSharedPreferences(PREFS, 0)
            .getString(KEY_THEME, "Royal Blue") ?: "Royal Blue"
    }

    fun selectedCardStyle(activity: Activity): String {
        return activity.getSharedPreferences(PREFS, 0)
            .getString(KEY_CARD, "GLASS") ?: "GLASS"
    }

    fun saveTheme(activity: Activity, theme: String) {
        if (theme !in palettes) return

        activity.getSharedPreferences(PREFS, 0)
            .edit()
            .putString(KEY_THEME, theme)
            .apply()
    }

    fun saveCardStyle(activity: Activity, style: String) {
        if (style !in listOf("GLASS", "SOLID", "OUTLINED")) return

        activity.getSharedPreferences(PREFS, 0)
            .edit()
            .putString(KEY_CARD, style)
            .apply()
    }

    fun load(activity: Activity) {
        val name = selectedTheme(activity)
        current = palettes[name] ?: palettes.getValue("Royal Blue")

        BACKGROUND = current.background
        SURFACE = current.surface
        CARD = current.card
        CARD_LIGHT = current.cardLight
        WHITE = current.white
        MUTED = current.muted
        CYAN = current.accent
        BLUE = current.blue
        PURPLE = current.purple
    }

    fun applyTo(activity: Activity) {
        load(activity)
        activity.window.statusBarColor = BACKGROUND
        activity.window.navigationBarColor = SURFACE
        activity.window.decorView.setBackgroundColor(BACKGROUND)
    }

    fun gradient(
        first: Int = BLUE,
        second: Int = PURPLE,
        radius: Float = 22f
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(first, second)
        ).apply {
            cornerRadius = radius
        }
    }

    fun background(
        color: Int = CARD,
        radius: Float = 22f,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(2, strokeColor)
            }
        }
    }

    fun text(
        activity: Activity,
        value: String,
        size: Float = 14f,
        color: Int = WHITE,
        bold: Boolean = false
    ): TextView {
        return TextView(activity).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    fun card(activity: Activity): LinearLayout {
        val style = selectedCardStyle(activity)

        val cardBackground = when (style) {
            "SOLID" -> background(CARD_LIGHT, 22f)
            "OUTLINED" -> background(
                BACKGROUND,
                22f,
                CYAN
            )
            else -> background(
                CARD,
                22f,
                0xFF35476B.toInt()
            )
        }

        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 15, 16, 15)
            background = cardBackground
            elevation = if (style == "GLASS") 5f else 0f
        }
    }

    fun button(
        activity: Activity,
        title: String,
        color1: Int = BLUE,
        color2: Int = PURPLE
    ): TextView {
        return TextView(activity).apply {
            text = title
            textSize = 14f
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(18, 13, 18, 13)
            background = gradient(color1, color2, 18f)
            isClickable = true
            isFocusable = true
        }
    }

    fun heading(activity: Activity, title: String): TextView {
        return TextView(activity).apply {
            text = title
            textSize = 20f
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 12, 0, 10)
        }
    }

    fun page(activity: Activity): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 18, 16, 12)
            setBackgroundColor(BACKGROUND)
        }
    }

    fun row(activity: Activity): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    fun divider(activity: Activity): View {
        return View(activity).apply {
            setBackgroundColor(0xFF263657.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
        }
    }

    fun styleBottomNavigation(view: View) {
        view.setBackgroundColor(SURFACE)
        view.elevation = 14f
    }

    fun styleScreen(root: LinearLayout) {
        root.setBackgroundColor(BACKGROUND)
        root.setPadding(16, 14, 16, 12)
    }
}
