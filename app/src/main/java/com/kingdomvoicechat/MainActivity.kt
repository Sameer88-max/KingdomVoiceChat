
package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : Activity() {

    private val bg = Color.rgb(12, 14, 35)
    private val card = Color.rgb(27, 31, 65)
    private val white = Color.WHITE
    private val muted = Color.rgb(174, 181, 215)
    private val cyan = Color.rgb(69, 226, 235)
    private val purple = Color.rgb(150, 102, 255)
    private val pink = Color.rgb(255, 105, 175)
    private val gold = Color.rgb(255, 190, 86)

    private lateinit var page: LinearLayout
    private var currentFeature: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showHome()
    }

    override fun onBackPressed() {
        if (currentFeature != null) {
            showHome()
        } else {
            super.onBackPressed()
        }
    }

    private fun showHome() {
        currentFeature = null

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.isFillViewport = true

        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(24))
        }

        scroll.addView(page)

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val logo = text("♛", 30f, cyan, true)
        logo.gravity = Gravity.CENTER
        header.addView(logo, LinearLayout.LayoutParams(dp(48), dp(48)))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(text("KINGDOM", 22f, white, true))
            addView(text("VOICE • CHAT • CONNECT", 10f, cyan, true))
        }

        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        val bell = text("🔔", 23f, white, false)
        bell.setOnClickListener { showFeature("Notifications") }
        header.addView(bell)
        page.addView(header)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(20))
            background = gradient(
                intArrayOf(Color.rgb(49, 39, 111), Color.rgb(16, 100, 125)),
                24
            )
        }

        hero.addView(text("YOUR WORLD. YOUR VOICE.", 11f, cyan, true))

        val heading = text("Find your people.", 29f, white, true)
        heading.setPadding(0, dp(12), 0, dp(8))
        hero.addView(heading)

        hero.addView(
            text(
                "Join rooms, meet friends and share your moments.",
                14f, white, false
            )
        )

        val explore = tile("🎙", "Explore Voice Rooms", cyan) {
            openRooms()
        }

        val exploreParams = LinearLayout.LayoutParams(-1, dp(85))
        exploreParams.topMargin = dp(18)
        hero.addView(explore, exploreParams)

        val heroParams = LinearLayout.LayoutParams(-1, -2)
        heroParams.topMargin = dp(22)
        page.addView(hero, heroParams)

        val ownerPanel = tile("👑", "OWNER PANEL", gold) {
            try {
                startActivity(Intent(this, OwnerPanelActivity::class.java))
            } catch (e: Exception) {
                notice("Owner Panel screen could not be opened.")
            }
        }

        val ownerParams = LinearLayout.LayoutParams(-1, dp(85))
        ownerParams.topMargin = dp(18)
        page.addView(ownerPanel, ownerParams)

        section(page, "Your Space", "QUICK ACCESS")

        addGrid(page, "🎙", "Voice Rooms", "🏠", "Sub Rooms")
        addGrid(page, "🎁", "Gifts", "💬", "Chat Messages")
        addGrid(page, "💎", "Wallet", "👤", "My Profile")
        addGrid(page, "👥", "Followers", "⭐", "Levels")

        section(page, "My Account", "SETTINGS")

        addRow(page, "🖼", "Profile & DP",
            "Name, photo and user details", purple)

        addRow(page, "🌍", "Country, Age & Gender",
            "Manage profile information", cyan)

        addRow(page, "🔐", "Update Password",
            "Account security", gold)

        addRow(page, "⚙", "Settings",
            "Privacy and preferences", purple)

        addRow(page, "❓", "Help & Customer Contact",
            "Support and assistance", cyan)

        addRow(page, "🚪", "Sign Out",
            "Sign out of this device", pink)

        addRow(page, "🗑", "Delete Account",
            "Request account deletion", pink)

        val footer = text("KINGDOM VOICE CHAT", 10f, muted, true)
        footer.gravity = Gravity.CENTER
        footer.setPadding(0, dp(24), 0, dp(4))
        page.addView(footer)

        setContentView(scroll)
    }

    private fun showFeature(name: String) {
        currentFeature = name

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.isFillViewport = true

        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }

        scroll.addView(screen)

        val back = tile("‹", "BACK TO HOME", cyan) {
            showHome()
        }

        screen.addView(back, LinearLayout.LayoutParams(-1, dp(62)))

        val title = text(name, 27f, white, true)
        title.setPadding(0, dp(28), 0, dp(8))
        screen.addView(title)

        screen.addView(text("KINGDOM VOICE CHAT", 12f, cyan, true))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = gradient(intArrayOf(card, bg), 20)
        }

        val description = when (name) {
            "Sub Rooms" -> "Explore and manage sub-room options."
            "Gifts" -> "Gift catalogue and gift sending will be connected to the secure wallet."
            "Chat Messages" -> "Private text messages, conversation list and friend chat will be connected here."
            "Wallet" -> "Your coin balance, Pearls, transaction history and seller requests will appear here."
            "My Profile", "Profile & DP" -> "Your profile name, photo, user ID and account details will be managed here."
            "Followers" -> "Your followers and following lists will appear here."
            "Levels" -> "Your activity level and level progress will appear here."
            "Country, Age & Gender" -> "Your profile information will be managed here."
            "Update Password" -> "Use the secure password update process for your account."
            "Settings" -> "Privacy, notifications and app preferences will be configured here."
            "Help & Customer Contact" -> "Customer support and complaint submission will be connected here."
            "Notifications" -> "Your app notifications will appear here."
            "Delete Account" -> "Account deletion requires identity checks and explicit confirmation."
            else -> "This section is ready for its next implementation phase."
        }

        info.addView(text("FEATURE CENTRE", 12f, cyan, true))

        val detail = text(description, 15f, white, false)
        detail.setPadding(0, dp(12), 0, dp(12))
        info.addView(detail)

        info.addView(
            text(
                "Status: Screen navigation ready. Feature/backend integration pending.",
                12f, gold, false
            )
        )

        val infoParams = LinearLayout.LayoutParams(-1, -2)
        infoParams.topMargin = dp(22)
        screen.addView(info, infoParams)

        when (name) {
            "My Profile", "Profile & DP" -> {
                addFeatureButton(screen, "EDIT PROFILE") {
                    notice("Profile editor is not connected yet.")
                }
            }

            "Update Password" -> {
                addFeatureButton(screen, "UPDATE PASSWORD") {
                    notice("Secure password update screen is next.")
                }
            }

            "Help & Customer Contact" -> {
                addFeatureButton(screen, "CONTACT SUPPORT") {
                    notice("Customer support backend is not connected yet.")
                }
            }

            "Delete Account" -> {
                addFeatureButton(screen, "REQUEST ACCOUNT DELETION") {
                    AlertDialog.Builder(this)
                        .setTitle("Delete account")
                        .setMessage(
                            "Account deletion is not enabled yet. No account has been deleted."
                        )
                        .setNegativeButton("CANCEL", null)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }

        setContentView(scroll)
    }

    private fun addFeatureButton(
        screen: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = tile("›", label, cyan, action)
        val params = LinearLayout.LayoutParams(-1, dp(70))
        params.topMargin = dp(14)
        screen.addView(button, params)
    }

    // CREATE ROOM / JOIN ROOM
    private fun openRooms() {
        val options = arrayOf("Create New Room", "Join Existing Room")

        AlertDialog.Builder(this)
            .setTitle("KINGDOM VOICE ROOMS")
            .setItems(options) { _, which ->
                if (which == 0) {
                    val roomID = "KVC_" +
                        System.currentTimeMillis().toString().takeLast(8)

                    AlertDialog.Builder(this)
                        .setTitle("Your Room ID")
                        .setMessage(
                            "Room ID: $roomID\nShare this ID with others."
                        )
                        .setNegativeButton("CANCEL", null)
                        .setPositiveButton("CREATE ROOM") { _, _ ->
                            RoomManager.createRoom(this, roomID)
                        }
                        .show()
                } else {
                    val input = EditText(this).apply {
                        hint = "Enter Room ID"
                        setSingleLine(true)
                    }

                    AlertDialog.Builder(this)
                        .setTitle("Join Existing Room")
                        .setView(input)
                        .setNegativeButton("CANCEL", null)
                        .setPositiveButton("JOIN") { _, _ ->
                            RoomManager.joinRoom(
                                this,
                                input.text.toString()
                            )
                        }
                        .show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun openOption(name: String) {
        when (name) {
            "Voice Rooms" -> openRooms()

            "Owner Panel" -> {
                try {
                    startActivity(Intent(this, OwnerPanelActivity::class.java))
                } catch (e: Exception) {
                    notice("Owner Panel screen could not be opened.")
                }
            }

            "Sign Out" -> confirmSignOut()
            else -> showFeature(name)
        }
    }

    private fun confirmSignOut() {
        AlertDialog.Builder(this)
            .setTitle("Sign out?")
            .setMessage("Do you want to sign out of Kingdom Voice Chat on this device?")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SIGN OUT") { _, _ ->
                try {
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, AuthActivity::class.java)
                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    notice("Sign out failed. Please try again.")
                }
            }
            .show()
    }

    private fun section(
        target: LinearLayout,
        title: String,
        tag: String
    ) {
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            text(title, 19f, white, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(text(tag, 9f, cyan, true))

        val params = LinearLayout.LayoutParams(-1, -2)
        params.topMargin = dp(24)
        params.bottomMargin = dp(12)
        target.addView(row, params)
    }

    private fun addGrid(
        target: LinearLayout,
        leftIcon: String,
        leftTitle: String,
        rightIcon: String,
        rightTitle: String
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(
            tile(leftIcon, leftTitle, purple) {
                openOption(leftTitle)
            },
            LinearLayout.LayoutParams(0, dp(105), 1f)
        )

        val rightParams = LinearLayout.LayoutParams(0, dp(105), 1f)
        rightParams.leftMargin = dp(12)

        row.addView(
            tile(rightIcon, rightTitle, cyan) {
                openOption(rightTitle)
            },
            rightParams
        )

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(12)
        target.addView(row, params)
    }

    private fun tile(
        icon: String,
        title: String,
        accent: Int,
        action: () -> Unit
    ): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(10), dp(12))
            background = gradient(intArrayOf(card, bg), 18)
            isClickable = true
            isFocusable = true
        }

        box.addView(text(icon, 24f, accent, true))

        val name = text(title, 14f, white, true)
        name.setPadding(0, dp(7), 0, 0)
        box.addView(name)

        box.setOnClickListener { action() }
        return box
    }

    private fun addRow(
        target: LinearLayout,
        icon: String,
        title: String,
        subtitle: String,
        accent: Int
    ) {
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(12), dp(14))
            background = gradient(intArrayOf(card, bg), 16)
            isClickable = true
            isFocusable = true
        }

        val symbol = text(icon, 23f, accent, true)
        symbol.gravity = Gravity.CENTER
        row.addView(symbol, LinearLayout.LayoutParams(dp(42), dp(42)))

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            addView(text(title, 14f, white, true))
            addView(text(subtitle, 11f, muted, false))
        }

        row.addView(details, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(text("›", 27f, cyan, false))

        row.setOnClickListener { openOption(title) }

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(10)
        target.addView(row, params)
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
