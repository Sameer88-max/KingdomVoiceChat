
package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class OwnerPanelActivity : Activity() {

    private val bgColor = Color.rgb(13, 10, 32)
    private val cardColor = Color.rgb(31, 24, 63)
    private val accentColor = Color.rgb(0, 220, 255)
    private val purpleColor = Color.rgb(130, 75, 255)
    private val textColor = Color.WHITE
    private val mutedColor = Color.rgb(190, 184, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(20, 28, 20, 24)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(48, 20, 100),
                    Color.rgb(17, 49, 104)
                )
            ).apply {
                cornerRadius = dp(18).toFloat()
            }
        }

        header.addView(label("👑 KINGDOM VOICE CHAT", 15, accentColor, true))
        header.addView(label("OWNER CONTROL PANEL", 25, textColor, true))
        header.addView(
            label(
                "Dashboard • Management • Settings",
                13,
                mutedColor,
                false
            )
        )

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 24)
        }

        content.addView(header, marginParams(bottom = 18))

        val warning = label(
            "⚠ DEMO PANEL — Real owner security and database actions are not connected yet.",
            13,
            Color.rgb(255, 220, 120),
            true
        ).apply {
            setPadding(14, 14, 14, 14)
            background = rounded(Color.rgb(64, 43, 25), 12)
        }
        content.addView(warning, marginParams(bottom = 20))

        content.addView(sectionTitle("OWNER DASHBOARD"))

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        stats.addView(
            statCard("TOTAL USERS", "—", "Live data pending"),
            LinearLayout.LayoutParams(0, dp(108), 1f).apply {
                marginEnd = dp(6)
            }
        )
        stats.addView(
            statCard("ACTIVE ROOMS", "—", "Live data pending"),
            LinearLayout.LayoutParams(0, dp(108), 1f).apply {
                marginStart = dp(6)
            }
        )
        content.addView(stats, marginParams(bottom = 22))

        content.addView(sectionTitle("MANAGEMENT & CONTROLS"))

        val options = listOf(
            "👥  User Management" to
                "View users, account status, and user reports.",
            "🛡️  Staff & Role Management" to
                "Manage staff roles and permissions.",
            "🏢  Agency & BD Management" to
                "Agency registration, BD management, and reports.",
            "🎧  Customer Service" to
                "Customer support requests and service staff.",
            "⚖️  Complaints & Hearings" to
                "Review complaints, evidence, and hearing records.",
            "🎙️  Voice Room Settings" to
                "Room configuration, microphone seats, and moderation options.",
            "🎁  Gift Management" to
                "Configure gift names, coin prices, and gift availability.",
            "💰  Coins & Pearls" to
                "Review coin and pearl settings. Real transactions are not connected.",
            "🪙  Seller Management" to
                "Coin seller profiles, recharge records, and seller reviews.",
            "📜  Policies & Rules" to
                "Manage app policies and community guidelines.",
            "🔔  Notifications" to
                "Prepare announcements and notification settings.",
            "⚙️  App Configuration" to
                "App name, theme, language, and general settings.",
            "🔐  Security Settings" to
                "Security configuration and access-control planning.",
            "📊  Reports & Activity" to
                "Reports, activity history, and administrative review.",
            "💾  Backup & Data" to
                "Backup configuration and data-management options."
        )

        options.forEach { (title, description) ->
            val item = TextView(this).apply {
                text = title
                textSize = 16f
                setTextColor(textColor)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 17, 16, 17)
                background = rounded(cardColor, 14)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    showFeatureDialog(title, description)
                }
            }

            content.addView(item, marginParams(bottom = 10))
        }

        val backButton = TextView(this).apply {
            text = "←  BACK TO HOME"
            textSize = 15f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.BLACK)
            setPadding(16, 16, 16, 16)
            background = rounded(accentColor, 14)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                finish()
            }
        }

        content.addView(backButton, marginParams(top = 14, bottom = 12))

        content.addView(
            label(
                "Kingdom Voice Chat • Owner Dashboard",
                12,
                mutedColor,
                false
            ).apply {
                gravity = Gravity.CENTER
                setPadding(8, 12, 8, 8)
            }
        )

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun statCard(
        title: String,
        value: String,
        subtitle: String
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
            background = rounded(cardColor, 14)

            addView(label(title, 11, accentColor, true))
            addView(label(value, 25, textColor, true))
            addView(label(subtitle, 10, mutedColor, false))
        }
    }

    private fun sectionTitle(title: String): TextView {
        return label(title, 14, accentColor, true).apply {
            setPadding(2, 8, 2, 12)
        }
    }

    private fun label(
        value: String,
        size: Int,
        color: Int,
        bold: Boolean
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun showFeatureDialog(title: String, description: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(
                "$description\n\n" +
                    "This section is currently a demo. " +
                    "Database integration and secure owner permissions " +
                    "must be implemented before it can perform real actions."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun rounded(color: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }
    }

    private fun marginParams(
        top: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
