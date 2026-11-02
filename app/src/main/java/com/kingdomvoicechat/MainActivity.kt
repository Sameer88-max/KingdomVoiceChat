
package com.kingdomvoicechat

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var navigation: LinearLayout

    private var selectedTab = "Home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KingdomTheme.applyTo(this)
        buildLayout()
        showTab("Home")
    }

    private fun buildLayout() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(KingdomTheme.BACKGROUND)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 18, 18, 14)
            setBackgroundColor(KingdomTheme.SURFACE)
        }

        val logo = TextView(this).apply {
            text = "KINGDOM"
            textSize = 21f
            setTextColor(KingdomTheme.CYAN)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        header.addView(
            logo,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val settings = KingdomTheme.button(this, "⚙ Settings")
        settings.setOnClickListener {
            startActivity(
                Intent(this, ThemeSettingsActivity::class.java)
            )
        }
        header.addView(settings)

        root.addView(header)

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 24)
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(0, 0, 1f)
        )

        navigation = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(4, 10, 4, 10)
            setBackgroundColor(KingdomTheme.SURFACE)
        }

        listOf("Home", "Rooms", "Messages", "Profile").forEach { tab ->
            val item = TextView(this).apply {
                text = tab
                gravity = Gravity.CENTER
                textSize = 12f
                setPadding(4, 12, 4, 12)
                setOnClickListener { showTab(tab) }
            }

            navigation.addView(
                item,
                LinearLayout.LayoutParams(0, -2, 1f)
            )
        }

        KingdomTheme.styleBottomNavigation(navigation)
        root.addView(navigation)

        setContentView(root)
    }

    private fun showTab(tab: String) {
        selectedTab = tab
        content.removeAllViews()

        when (tab) {
            "Home" -> showHome()
            "Rooms" -> showRooms()
            "Messages" -> showMessages()
            "Profile" -> showProfile()
        }

        updateNavigation()
    }

    private fun updateNavigation() {
        for (i in 0 until navigation.childCount) {
            val item = navigation.getChildAt(i) as TextView
            val active = item.text.toString() == selectedTab

            item.setTextColor(
                if (active) KingdomTheme.CYAN
                else KingdomTheme.MUTED
            )
            item.textSize = if (active) 13f else 12f
            item.setTypeface(
                null,
                if (active) android.graphics.Typeface.BOLD
                else android.graphics.Typeface.NORMAL
            )
        }
    }

    private fun showHome() {
        content.addView(
            KingdomTheme.heading(this, "Welcome to KINGDOM")
        )

        addCard(
            "Your Voice. Your Kingdom.",
            "Connect with people, join voice rooms and enjoy live conversations."
        )

        content.addView(
            KingdomTheme.heading(this, "Discover")
        )

        addActionCard(
            "🎙 Voice Rooms",
            "Explore available voice rooms"
        ) {
            showTab("Rooms")
        }

        addActionCard(
            "💬 Messages",
            "Open your conversations"
        ) {
            showTab("Messages")
        }

        addActionCard(
            "👤 My Profile",
            "View your account and profile"
        ) {
            showTab("Profile")
        }
    }

    private fun showRooms() {
        content.addView(
            KingdomTheme.heading(this, "Voice Rooms")
        )

        addCard(
            "Find Your Room",
            "Enter a room number to join a personal or official room."
        )

        val roomInput = EditText(this).apply {
            hint = "Enter room number"
            setHintTextColor(KingdomTheme.MUTED)
            setTextColor(KingdomTheme.WHITE)
            textSize = 16f
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(16, 14, 16, 14)
            background = KingdomTheme.background(
                KingdomTheme.CARD_LIGHT,
                18f
            )
        }

        content.addView(
            roomInput,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = 12
                bottomMargin = 12
            }
        )

        val joinButton = KingdomTheme.button(
            this,
            "JOIN ROOM"
        )

        joinButton.setOnClickListener {
            val roomID = roomInput.text.toString().trim()

            if (roomID.isBlank()) {
                Toast.makeText(
                    this,
                    "Enter room number",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(this, RoomActivity::class.java)
                intent.putExtra("roomID", roomID)
                startActivity(intent)
            }
        }

        content.addView(joinButton)

        content.addView(
            KingdomTheme.heading(this, "Room Options")
        )

        addActionCard(
            "🌐 Official Rooms",
            "Join an official room using its room number."
        ) {
            roomInput.requestFocus()
            Toast.makeText(
                this,
                "Enter an official room number above",
                Toast.LENGTH_SHORT
            ).show()
        }

        addActionCard(
            "🏠 Personal Rooms",
            "Join a user's personal room using its number."
        ) {
            roomInput.requestFocus()
        }
    }

    private fun showMessages() {
        content.addView(
            KingdomTheme.heading(this, "Messages")
        )

        addCard(
            "Your Conversations",
            "Your chat conversations will appear here."
        )

        addActionCard(
            "Refresh Messages",
            "Reload your conversations"
        ) {
            Toast.makeText(
                this,
                "Messages screen is ready for chat integration",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showProfile() {
        content.addView(
            KingdomTheme.heading(this, "My Profile")
        )

        val user = FirebaseAuth.getInstance().currentUser

        val name = user?.displayName ?: "Kingdom Member"
        val email = user?.email ?: "No email available"

        addCard(
            "👤 $name",
            email
        )

        addActionCard(
            "⚙ Theme Settings",
            "Change app colours and card styles"
        ) {
            startActivity(
                Intent(this, ThemeSettingsActivity::class.java)
            )
        }

        addActionCard(
            "🚪 Logout",
            "Sign out from your account"
        ) {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(
                this,
                "Logged out",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun addCard(title: String, description: String) {
        val card = KingdomTheme.card(this)

        card.addView(
            KingdomTheme.text(
                this,
                title,
                18f,
                KingdomTheme.CYAN,
                true
            )
        )

        val detail = KingdomTheme.text(
            this,
            description,
            13f,
            KingdomTheme.MUTED
        )

        detail.setPadding(0, 9, 0, 0)
        card.addView(detail)

        content.addView(
            card,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = 14
            }
        )
    }

    private fun addActionCard(
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val card = KingdomTheme.card(this)

        card.isClickable = true
        card.isFocusable = true
        card.setOnClickListener { action() }

        card.addView(
            KingdomTheme.text(
                this,
                title,
                16f,
                KingdomTheme.WHITE,
                true
            )
        )

        val detail = KingdomTheme.text(
            this,
            description,
            12f,
            KingdomTheme.MUTED
        )

        detail.setPadding(0, 7, 0, 0)
        card.addView(detail)

        content.addView(
            card,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = 12
            }
        )
    }
}
