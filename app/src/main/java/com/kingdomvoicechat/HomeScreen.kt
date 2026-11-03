package com.kingdomvoicechat

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class HomeScreen(
    private val context: Context,
    private val onCreateRoom: () -> Unit,
    private val onFindRoom: () -> Unit
) {
    fun build(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 30, 24, 20)
            setBackgroundColor(Color.rgb(4, 8, 38))
        }

        layout.addView(TextView(context).apply {
            text = "Welcome to Kingdom Voice Chat"
            textSize = 24f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
        })

        layout.addView(TextView(context).apply {
            text = "Sing • Chat • Make Friends"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 30)
        })

        layout.addView(TextView(context).apply {
            text = "Live Rooms"
            textSize = 20f
            setTextColor(Color.WHITE)
        })

        layout.addView(TextView(context).apply {
            text = "No live rooms available right now."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 15, 0, 20)
        })

        layout.addView(Button(context).apply {
            text = "Create Room"
            setOnClickListener { onCreateRoom() }
        })

        layout.addView(Button(context).apply {
            text = "Find a Room"
            setOnClickListener { onFindRoom() }
        })

        return layout
    }
}
