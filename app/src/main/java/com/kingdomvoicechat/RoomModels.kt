package com.kingdomvoicechat

data class RoomModel(
    val roomId: String = "",
    val roomName: String = "Singing Fun Room",
    val ownerUid: String = "",
    val ownerName: String = "",
    val category: String = "General",
    val onlineCount: Int = 0,
    val isOnline: Boolean = false,
    val seatCount: Int = 20,
    val themeColor: String = "#101B70",
    val backgroundType: String = "default",
    val backgroundUri: String = "",
    val isLocked: Boolean = false,
    val status: String = "active"
) {
    val ownerId: String
        get() = ownerUid
}

data class RoomSeat(
    val seatNumber: Int = 0,
    val userUid: String = "",
    val userName: String = "",
    val profileImage: String = "",
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val isOccupied: Boolean = false
)

enum class RoomRole {
    OWNER,
    SUPER_ADMIN,
    ADMIN,
    USER
}

data class RoomStaff(
    val userUid: String = "",
    val role: String = "USER",
    val canMute: Boolean = false,
    val canLockSeat: Boolean = false,
    val canRemoveUser: Boolean = false,
    val canManageAdmins: Boolean = false
)
