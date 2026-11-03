package com.kingdomvoicechat

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MessagesScreen(
    private val context: Context
) {
    fun build(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(4, 8, 38))
        }

        layout.addView(TextView(context).apply {
            text = "Messages"
            textSize = 25f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
        })

        layout.addView(TextView(context).apply {
            text = "No conversations yet."
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        })

        return layout
    }
}
