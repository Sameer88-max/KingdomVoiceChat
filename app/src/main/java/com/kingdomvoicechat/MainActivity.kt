// app/src/main/java/com/kingdomvoicechat/KingdomTheme.kt

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

    const val BACKGROUND = 0xFF080D20.toInt()
    const val SURFACE = 0xFF111A35.toInt()
    const val CARD = 0xFF172344.toInt()
    const val CARD_LIGHT = 0xFF202F55.toInt()
    const val WHITE = 0xFFFFFFFF.toInt()
    const val MUTED = 0xFF9BAAC8.toInt()
    const val CYAN = 0xFF36D9F5.toInt()
    const val BLUE = 0xFF397BFF.toInt()
    const val PURPLE = 0xFF895BFF.toInt()
    const val PINK = 0xFFFF5FA2.toInt()
    const val GREEN = 0xFF39D99A.toInt()

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
                setStroke(1, strokeColor)
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
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 15, 16, 15)
            background = KingdomTheme.background(
                CARD, 24f, 0xFF263B68.toInt()
            )
            elevation = 5f
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
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).apply {
                topMargin = 12
                bottomMargin = 12
            }
        }
    }

    fun applyTo(activity: Activity) {
        activity.window.statusBarColor = BACKGROUND
        activity.window.navigationBarColor = BACKGROUND
        activity.window.decorView.setBackgroundColor(BACKGROUND)
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

/*
MAIN ACTIVITY INTEGRATION:

1. MainActivity.onCreate() ke andar:
   KingdomTheme.applyTo(this)

2. Bottom navigation view banane ke baad:
   KingdomTheme.styleBottomNavigation(bottomNavigationView)

3. Har screen ke root LinearLayout par:
   KingdomTheme.styleScreen(root)

Example:
val root = KingdomTheme.page(this)
root.addView(KingdomTheme.heading(this, "KINGDOM"))
setContentView(root)
*/
