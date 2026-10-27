
package com.kingdomvoicechat

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class OwnerPanelActivity : Activity() {

    private val bg = Color.rgb(12, 14, 35)
    private val card = Color.rgb(27, 31, 65)
    private val white = Color.WHITE
    private val cyan = Color.rgb(69, 226, 235)
    private val gold = Color.rgb(255, 190, 86)
    private val muted = Color.rgb(174, 181, 215)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)

        val page = LinearLayout(this)
        page.orientation = LinearLayout.VERTICAL
        page.setPadding(dp(18), dp(22), dp(18), dp(28))
        scroll.addView(page)

        page.addView(text("KINGDOM VOICE CHAT", 23f, cyan, true))
        page.addView(text("OWNER CONTROL PANEL", 17f, gold, true))

        val subtitle = text(
            "Manage app modules and administration",
            12f, muted, false
        )
        subtitle.setPadding(0, dp(8), 0, dp(18))
        page.addView(subtitle)

        section(page, "USER & SECURITY")
        addOption(page, "User Management")
        addOption(page, "Role Management & Permissions")
        addOption(page, "Password Centre")
        addOption(page, "Wallet Block / Unblock")
        addOption(page, "General Ban & User Activity")
        addOption(page, "Unique ID Management")

        section(page, "OFFICIAL STAFF")
        addOption(page, "Official Staff & Titles")
        addOption(page, "Badge Management")
        addOption(page, "Frames Management")
        addOption(page, "Official Titles & Privileges")

        section(page, "COINS & GIFTS")
        addOption(page, "Coin Seller Management")
        addOption(page, "Coins Purchase Settings")
        addOption(page, "Pearl Exchange Requests")
        addOption(page, "Gift Catalogue")
        addOption(page, "Gift Sending Settings")
        addOption(page, "Event Gifts & Birthday Gifts")
        addOption(page, "Mall Send / History")

        section(page, "AGENCIES & ROOMS")
        addOption(page, "BD Centre & Agencies")
        addOption(page, "Family Management")
        addOption(page, "Family Levels")
        addOption(page, "CP Relationships")
        addOption(page, "Room Themes & Approval")
        addOption(page, "Room Sending Settings")
        addOption(page, "VIP / SVIP Management")
        addOption(page, "User Levels")

        section(page, "ADMINISTRATION")
        addOption(page, "Broadcast Messages")
        addOption(page, "Complaints & Hearings")
        addOption(page, "Maintenance Settings")
        addOption(page, "Reports & Activity Logs")
        addOption(page, "Backup Settings")

        val note = text(
            "OWNER PANEL • UI PREVIEW",
            10f, muted, true
        )
        note.gravity = Gravity.CENTER
        note.setPadding(0, dp(24), 0, dp(8))
        page.addView(note)

        setContentView(scroll)
    }

    private fun section(page: LinearLayout, title: String) {
        val heading = text(title, 15f, cyan, true)
        heading.setPadding(0, dp(20), 0, dp(10))
        page.addView(heading)
    }

    private fun addOption(page: LinearLayout, title: String) {
        val item = TextView(this).apply {
            text = "⚙   $title     ›"
            textSize = 14f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(16), dp(12), dp(16))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(card, bg)
            ).apply {
                cornerRadius = dp(14).toFloat()
            }
        }

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(9)
        page.addView(item, params)

        item.setOnClickListener {
            Toast.makeText(
                this,
                "$title: Backend integration pending",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
