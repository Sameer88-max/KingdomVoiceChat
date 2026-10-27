
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

class MainActivity : Activity() {

    private val bg = Color.rgb(12, 14, 35)
    private val card = Color.rgb(27, 31, 65)
    private val white = Color.WHITE
    private val muted = Color.rgb(174, 181, 215)
    private val cyan = Color.rgb(69, 226, 235)
    private val purple = Color.rgb(150, 102, 255)
    private val pink = Color.rgb(255, 105, 175)
    private val gold = Color.rgb(255, 190, 86)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)

        val page = LinearLayout(this)
        page.orientation = LinearLayout.VERTICAL
        page.setPadding(dp(18), dp(20), dp(18), dp(24))
        scroll.addView(page)

        // Header
        val header = LinearLayout(this)
        header.gravity = Gravity.CENTER_VERTICAL

        val logo = text("♛", 30f, cyan, true)
        logo.gravity = Gravity.CENTER
        header.addView(logo, LinearLayout.LayoutParams(dp(48), dp(48)))

        val brand = LinearLayout(this)
        brand.orientation = LinearLayout.VERTICAL
        brand.addView(text("KINGDOM", 22f, white, true))
        brand.addView(text("VOICE • CHAT • CONNECT", 10f, cyan, true))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        header.addView(text("🔔", 23f, white, false))
        page.addView(header)

        // Hero banner
        val hero = LinearLayout(this)
        hero.orientation = LinearLayout.VERTICAL
        hero.setPadding(dp(20), dp(22), dp(20), dp(20))
        hero.background = gradient(
            intArrayOf(Color.rgb(49, 39, 111), Color.rgb(16, 100, 125)),
            24
        )

        hero.addView(text("YOUR WORLD. YOUR VOICE.", 11f, cyan, true))
        val heading = text("Find your people.", 29f, white, true)
        heading.setPadding(0, dp(12), 0, dp(8))
        hero.addView(heading)
        hero.addView(
            text("Join rooms, meet friends and share your moments.",
                14f, white, false)
        )

        val explore = tile("🎙", "Explore Voice Rooms", cyan) {
            notice("Voice rooms need server integration.")
        }
        val ep = LinearLayout.LayoutParams(-1, -2)
        ep.topMargin = dp(18)
        hero.addView(explore, ep)

        val hp = LinearLayout.LayoutParams(-1, -2)
        hp.topMargin = dp(22)
        page.addView(hero, hp)

        section(page, "Your Space", "QUICK ACCESS")

        addGrid(
            page,
            Pair("🎙", "Voice Rooms"),
            Pair("🏠", "Sub Rooms")
        )
        addGrid(
            page,
            Pair("🎁", "Gifts"),
            Pair("💬", "Chat Messages")
        )
        addGrid(
            page,
            Pair("💎", "Wallet"),
            Pair("👤", "My Profile")
        )
        addGrid(
            page,
            Pair("👥", "Followers"),
            Pair("⭐", "Levels")
        )

        section(page, "My Account", "SETTINGS")

        addRow(page, "🖼", "Profile & DP", "Name, photo and user details", purple)
        addRow(page, "🌍", "Country, Age & Gender", "Manage profile information", cyan)
        addRow(page, "🔐", "Update Password", "Account security", gold)
        addRow(page, "⚙", "Settings", "Privacy and preferences", purple)
        addRow(page, "❓", "Help & Customer Contact", "Support and assistance", cyan)
        addRow(page, "🚪", "Sign Out", "Sign out of this device", pink)
        addRow(page, "🗑", "Delete Account", "Account deletion request", pink)

        val footer = text(
            "KINGDOM VOICE CHAT • HOME PREVIEW",
            10f, muted, true
        )
        footer.gravity = Gravity.CENTER
        footer.setPadding(0, dp(24), 0, dp(4))
        page.addView(footer)

        setContentView(scroll)
    }

    private fun section(
        page: LinearLayout,
        title: String,
        tag: String
    ) {
        val row = LinearLayout(this)
        row.gravity = Gravity.CENTER_VERTICAL

        row.addView(text(title, 19f, white, true),
            LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(text(tag, 9f, cyan, true))

        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(24)
        p.bottomMargin = dp(12)
        page.addView(row, p)
    }

    private fun addGrid(
        page: LinearLayout,
        left: Pair<String, String>,
        right: Pair<String, String>
    ) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL

        row.addView(
            tile(left.first, left.second, purple) {
                openOption(left.second)
            },
            LinearLayout.LayoutParams(0, dp(105), 1f)
        )

        val gap = LinearLayout.LayoutParams(0, dp(105), 1f)
        gap.leftMargin = dp(12)
        row.addView(
            tile(right.first, right.second, cyan) {
                openOption(right.second)
            },
            gap
        )

        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(12)
        page.addView(row, p)
    }

    private fun tile(
        icon: String,
        title: String,
        accent: Int,
        action: () -> Unit
    ): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.gravity = Gravity.CENTER_VERTICAL
        box.setPadding(dp(14), dp(12), dp(10), dp(12))
        box.background = gradient(intArrayOf(card, bg), 18)

        box.addView(text(icon, 24f, accent, true))
        val name = text(title, 14f, white, true)
        name.setPadding(0, dp(7), 0, 0)
        box.addView(name)

        box.setOnClickListener { action() }
        return box
    }

    private fun addRow(
        page: LinearLayout,
        icon: String,
        title: String,
        subtitle: String,
        accent: Int
    ) {
        val row = LinearLayout(this)
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(14), dp(14), dp(12), dp(14))
        row.background = gradient(intArrayOf(card, bg), 16)

        val symbol = text(icon, 23f, accent, true)
        symbol.gravity = Gravity.CENTER
        row.addView(symbol, LinearLayout.LayoutParams(dp(42), dp(42)))

        val details = LinearLayout(this)
        details.orientation = LinearLayout.VERTICAL
        details.setPadding(dp(12), 0, 0, 0)
        details.addView(text(title, 14f, white, true))
        details.addView(text(subtitle, 11f, muted, false))

        row.addView(details, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(text("›", 27f, cyan, false))

        row.setOnClickListener { openOption(title) }

        val p = LinearLayout.LayoutParams(-1, -2)
        p.bottomMargin = dp(10)
        page.addView(row, p)
    }

    private fun openOption(name: String) {
        when (name) {
            "Wallet" -> notice("Wallet: Coins, Pearls and seller withdrawal.")
            "Gifts" -> notice("Gift catalogue and sending system comes next.")
            "Voice Rooms", "Sub Rooms" ->
                notice("Live audio requires voice-server integration.")
            "My Profile", "Profile & DP" ->
                notice("Profile editing will be connected next.")
            "Sign Out" ->
                notice("Sign out will work after login is implemented.")
            "Delete Account" ->
                notice("Secure account deletion needs confirmation.")
            else -> notice("$name selected. Feature setup comes next.")
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

    private fun gradient(
        colors: IntArray,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            colors
        ).apply {
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun notice(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
