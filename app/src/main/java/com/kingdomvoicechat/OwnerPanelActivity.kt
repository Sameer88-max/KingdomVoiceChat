package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class OwnerPanelActivity : Activity() {

    private val bg = Color.rgb(12, 14, 35)
    private val card = Color.rgb(27, 31, 65)
    private val white = Color.WHITE
    private val muted = Color.rgb(174, 181, 215)
    private val cyan = Color.rgb(69, 226, 235)
    private val purple = Color.rgb(150, 102, 255)
    private val pink = Color.rgb(255, 105, 175)
    private val gold = Color.rgb(255, 190, 86)

    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        showPanel()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun showPanel() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(28))
        }

        scroll.addView(content)

        // Header
        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = makeText("‹", 34f, cyan, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { finish() }
        }

        header.addView(back, LinearLayout.LayoutParams(dp(45), dp(50)))

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(makeText("KINGDOM OWNER", 20f, white, true))
            addView(makeText("CONTROL CENTRE", 11f, cyan, true))
        }

        header.addView(
            titleBox,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        header.addView(makeText("👑", 28f, gold, true))
        content.addView(header)

        // Hero panel
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(22))
            background = gradient(
                intArrayOf(
                    Color.rgb(74, 42, 148),
                    Color.rgb(15, 104, 130)
                ),
                24
            )
        }

        hero.addView(makeText("👑  OWNER DASHBOARD", 12f, cyan, true))

        val heroTitle = makeText(
            "Manage Your Kingdom",
            27f, white, true
        )
        heroTitle.setPadding(0, dp(12), 0, dp(8))
        hero.addView(heroTitle)

        hero.addView(
            makeText(
                "Manage staff, review activity and configure app features.",
                13f, white, false
            )
        )

        val heroParams = LinearLayout.LayoutParams(-1, -2)
        heroParams.topMargin = dp(22)
        content.addView(hero, heroParams)

        // Notice about demo status
        val notice = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = gradient(
                intArrayOf(Color.rgb(69, 51, 25), Color.rgb(38, 30, 47)),
                16
            )
        }

        notice.addView(makeText("⚠ DEMO PANEL", 12f, gold, true))

        val noticeText = makeText(
            "These controls currently open demo information only. " +
                "They do not change live users, permissions, payments or server settings.",
            12f, white, false
        )
        noticeText.setPadding(0, dp(8), 0, 0)
        notice.addView(noticeText)

        val noticeParams = LinearLayout.LayoutParams(-1, -2)
        noticeParams.topMargin = dp(14)
        content.addView(notice, noticeParams)

        // Overview cards
        addSection("Overview", "DASHBOARD")

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        stats.addView(
            statCard("16", "Demo Seats", cyan),
            LinearLayout.LayoutParams(0, dp(95), 1f)
        )

        val secondStat = LinearLayout.LayoutParams(0, dp(95), 1f)
        secondStat.leftMargin = dp(10)

        stats.addView(
            statCard("12", "Role Types", purple),
            secondStat
        )

        val statsParams = LinearLayout.LayoutParams(-1, -2)
        statsParams.bottomMargin = dp(6)
        content.addView(stats, statsParams)

        // User and staff management
        addSection("People & Staff", "MANAGEMENT")

        addOption(
            "👥",
            "User Management",
            "View user management options",
            cyan
        )

        addOption(
            "🛡",
            "Staff & Role Management",
            "Official staff, admins and role descriptions",
            purple
        )

        addOption(
            "🏢",
            "Agency & BD Management",
            "Agency leaders, BD and merchant options",
            gold
        )

        addOption(
            "🎧",
            "Customer Service",
            "Support and customer assistance options",
            cyan
        )

        addOption(
            "⚖",
            "Complaints & Hearings",
            "Complaint review and hearing workflow",
            pink
        )

        // App management
        addSection("App Management", "CONFIGURATION")

        addOption(
            "🎙",
            "Voice Room Settings",
            "Room seats, room controls and room rules",
            cyan
        )

        addOption(
            "🎁",
            "Gift Management",
            "Gift catalogue and gift configuration",
            pink
        )

        addOption(
            "💎",
            "Coins & Pearls",
            "Currency rules and transaction review options",
            gold
        )

        addOption(
            "💰",
            "Seller Management",
            "Coin sellers and seller approval workflow",
            purple
        )

        addOption(
            "📜",
            "Policies & Rules",
            "Community policies and app rules",
            cyan
        )

        addOption(
            "🔔",
            "Notifications",
            "Notification configuration options",
            gold
        )

        addOption(
            "🔧",
            "App Configuration",
            "General settings and maintenance options",
            purple
        )

        // Security
        addSection("Security & Reports", "CONTROL")

        addOption(
            "🔐",
            "Security Settings",
            "Security configuration and access review",
            pink
        )

        addOption(
            "📊",
            "Reports & Activity",
            "Reports and activity review options",
            cyan
        )

        addOption(
            "💾",
            "Backup & Data",
            "Backup configuration information",
            gold
        )

        // Footer
        val footer = makeText(
            "KINGDOM VOICE CHAT • OWNER PANEL",
            10f, muted, true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(8))
        }

        content.addView(footer)
        setContentView(scroll)
    }

    private fun addSection(title: String, tag: String) {
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            makeText(title, 19f, white, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(makeText(tag, 9f, cyan, true))

        val params = LinearLayout.LayoutParams(-1, -2)
        params.topMargin = dp(24)
        params.bottomMargin = dp(12)

        content.addView(row, params)
    }

    private fun statCard(
        number: String,
        label: String,
        accent: Int
    ): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(12))
            background = gradient(intArrayOf(card, bg), 18)
        }

        box.addView(makeText(number, 25f, accent, true))

        val labelText = makeText(label, 11f, muted, true)
        labelText.gravity = Gravity.CENTER
        labelText.setPadding(0, dp(5), 0, 0)
        box.addView(labelText)

        return box
    }

    private fun addOption(
        icon: String,
        title: String,
        subtitle: String,
        accent: Int
    ) {
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(13), dp(12), dp(13))
            background = gradient(intArrayOf(card, bg), 16)
            isClickable = true
            isFocusable = true
        }

        val iconView = makeText(icon, 24f, accent, true).apply {
            gravity = Gravity.CENTER
        }

        row.addView(
            iconView,
            LinearLayout.LayoutParams(dp(42), dp(42))
        )

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(8), 0)
            addView(makeText(title, 14f, white, true))
            addView(makeText(subtitle, 11f, muted, false))
        }

        row.addView(
            details,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(makeText("›", 27f, cyan, false))

        row.setOnClickListener {
            showOptionDetails(title, subtitle)
        }

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(10)
        content.addView(row, params)
    }

    private fun showOptionDetails(title: String, description: String) {
        val extra = when (title) {
            "Staff & Role Management" ->
                "Role types include Official Employee, Complain Officer, " +
                    "Salary Provider Officer, Manager, Super Admin, Admin, " +
                    "Coin Seller, Merchant, BD and Agency Agent."

            "Coins & Pearls" ->
                "Your planned system uses 1,000 gift coins = 500 Pearls. " +
                    "Live balances and transactions are not connected yet."

            "Seller Management" ->
                "The planned seller workflow includes coin sales, Pearl exchange " +
                    "and authorized approval. No live transactions are processed here."

            "Voice Room Settings" ->
                "Room configuration is currently a demo. Changes are not applied " +
                    "to active rooms."

            "Security Settings" ->
                "Real access control must be enforced by a trusted backend. " +
                    "This screen alone cannot secure Owner permissions."

            "Backup & Data" ->
                "Automated server backups are not connected yet."

            "Complaints & Hearings" ->
                "Complaint records and hearing decisions are not connected to a database yet."

            "Reports & Activity" ->
                "Live user, wallet and staff reports are not available yet."

            else ->
                "This section is part of the Owner Panel interface. " +
                    "Database and server integration is still required."
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("$description\n\n$extra")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun makeText(
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
}
