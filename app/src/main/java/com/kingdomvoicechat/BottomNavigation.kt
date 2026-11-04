
package com.kingdomvoicechat

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class BottomNavigation(
    private val context: Context,
    private val currentTab: String,
    private val onTabSelected: (String) -> Unit
) {

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    fun build(): LinearLayout {

        val nav = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                dp(8),
                dp(6),
                dp(8),
                dp(30)
            )
            minimumHeight = dp(90)
            setBackgroundColor(Color.rgb(8, 15, 54))
        }

        val items = listOf(
            "♙" to "Profile",
            "✉" to "Messages",
            "🎙" to "Rooms",
            "⌂" to "Home"
        )

        items.forEach { item ->

            val active = currentTab == item.second

            val color = if (active) {
                Color.CYAN
            } else {
                Color.LTGRAY
            }

            val cell = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(
                    dp(4),
                    dp(5),
                    dp(4),
                    dp(5)
                )

                isClickable = true
                isFocusable = true

                setOnClickListener {
                    onTabSelected(item.second)
                }
            }

            cell.addView(
                TextView(context).apply {
                    text = item.first
                    textSize = 23f
                    setTextColor(color)
                    gravity = Gravity.CENTER
                }
            )

            cell.addView(
                TextView(context).apply {
                    text = item.second
                    textSize = 11f
                    setTextColor(color)
                    gravity = Gravity.CENTER
                }
            )

            nav.addView(
                cell,
                LinearLayout.LayoutParams(
                    0,
                    dp(54),
                    1f
                )
            )
        }

        return nav
    }
}
