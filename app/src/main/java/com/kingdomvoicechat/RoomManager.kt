package com.kingdomvoicechat

import android.app.Activity
import android.content.Intent
import android.widget.Toast

object RoomManager {

    fun createRoom(activity: Activity, roomID: String) {
        openRoom(activity, roomID, true)
    }

    fun joinRoom(activity: Activity, roomID: String) {
        openRoom(activity, roomID, false)
    }

    private fun openRoom(
        activity: Activity,
        roomID: String,
        isHost: Boolean
    ) {
        val cleanRoomID = roomID.trim()

        if (cleanRoomID.isBlank()) {
            Toast.makeText(activity, "Please enter a valid Room ID", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(activity, RoomActivity::class.java).apply {
            putExtra("roomID", cleanRoomID)
            putExtra("host", isHost)
        }

        activity.startActivity(intent)
    }
}
