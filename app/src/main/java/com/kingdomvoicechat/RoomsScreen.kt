package com.kingdomvoicechat

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class RoomsScreen(
    private val context: Context,
    private val rooms: List<RoomModel>,
    private val onCreateRoom: () -> Unit,
    private val onSearch: () -> Unit,
    private val onJoinRoom: (RoomModel) -> Unit
) {
    fun build(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 25, 20, 20)
            setBackgroundColor(Color.rgb(4, 8, 38))
        }

        layout.addView(TextView(context).apply {
            text = "Voice Rooms"
            textSize = 24f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
        })

        layout.addView(Button(context).apply {
            text = "Search User ID / Room ID"
            setOnClickListener { onSearch() }
        })

        layout.addView(Button(context).apply {
            text = "＋ Create Room"
            setOnClickListener { onCreateRoom() }
        })

        if (rooms.isEmpty()) {
            layout.addView(TextView(context).apply {
                text = "No rooms available.\nCreate a room to get started."
                textSize = 15f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(0, 35, 0, 20)
            })
        } else {
            rooms.filter { it.isOnline }.forEach { room ->
                val button = Button(context).apply {
                    text = "${room.roomName} • ID: ${room.roomId} • ${room.onlineCount} Online"
                    setOnClickListener { onJoinRoom(room) }
                }
                layout.addView(button)
            }
        }

        return layout
    }
}
