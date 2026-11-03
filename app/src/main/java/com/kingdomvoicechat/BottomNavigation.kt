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
    fun build(): LinearLayout {
        val nav = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.rgb(8, 15, 54))
        }

        val items = listOf(
            "♙" to "Profile",
            "✉" to "Messages",
            "🎙" to "Rooms",
            "⌂" to "Home"
        )

        items.forEach { item ->
            val cell = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(4, 12, 4, 12)
                setOnClickListener {
                    onTabSelected(item.second)
                }
            }

            val active = currentTab == item.second
            val color = if (active) {
                Color.CYAN
            } else {
                Color.LTGRAY
            }

            cell.addView(TextView(context).apply {
                text = item.first
                textSize = 23f
                setTextColor(color)
                gravity = Gravity.CENTER
            })

            cell.addView(TextView(context).apply {
                text = item.second
                textSize = 11f
                setTextColor(color)
                gravity = Gravity.CENTER
            })

            nav.addView(
                cell,
                LinearLayout.LayoutParams(0, 65, 1f)
            )
        }

        return nav
    }
}
