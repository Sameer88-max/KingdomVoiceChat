package com.kingdomvoicechat

data class RoomModel(
    val roomId: String,
    val roomName: String,
    val ownerId: String,
    val ownerName: String,
    val category: String = "General",
    val onlineCount: Int = 0,
    val isOnline: Boolean = false
)
