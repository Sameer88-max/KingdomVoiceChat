package com.kingdomvoicechat

data class UserLevel(
    val level: Int = 0,
    val xp: Int = 0
) {
    companion object {
        fun newUser(): UserLevel {
            return UserLevel(level = 0, xp = 0)
        }
    }
}
