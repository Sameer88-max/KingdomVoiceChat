
package com.kingdomvoicechat

object RoomPermissions {

    fun canManageRoom(role: RoomRole): Boolean {
        return role == RoomRole.OWNER ||
               role == RoomRole.SUPER_ADMIN
    }

    fun canManageStaff(role: RoomRole): Boolean {
        return role == RoomRole.OWNER
    }

    fun canMute(
        role: RoomRole,
        staff: RoomStaff?
    ): Boolean {
        return role == RoomRole.OWNER ||
               role == RoomRole.SUPER_ADMIN ||
               (role == RoomRole.ADMIN &&
                staff?.canMute == true)
    }

    fun canLockSeat(
        role: RoomRole,
        staff: RoomStaff?
    ): Boolean {
        return role == RoomRole.OWNER ||
               role == RoomRole.SUPER_ADMIN ||
               (role == RoomRole.ADMIN &&
                staff?.canLockSeat == true)
    }

    fun canRemoveUser(
        role: RoomRole,
        staff: RoomStaff?
    ): Boolean {
        return role == RoomRole.OWNER ||
               role == RoomRole.SUPER_ADMIN ||
               (role == RoomRole.ADMIN &&
                staff?.canRemoveUser == true)
    }

    fun canSitOnHostSeat(role: RoomRole): Boolean {
        return role == RoomRole.OWNER ||
               role == RoomRole.SUPER_ADMIN ||
               role == RoomRole.ADMIN
    }
}
