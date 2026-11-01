package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MainActivity : Activity() {

    private val bg = Color.rgb(12, 14, 35)
    private val card = Color.rgb(27, 31, 65)
    private val white = Color.WHITE
    private val muted = Color.rgb(174, 181, 215)
    private val cyan = Color.rgb(69, 226, 235)
    private val purple = Color.rgb(150, 102, 255)
    private val pink = Color.rgb(255, 105, 175)
    private val gold = Color.rgb(255, 190, 86)

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var currentFeature: String? = null
    private var selectedPhoto: Uri? = null
    private var chatListener: ListenerRegistration? = null
    private var chatScroll: ScrollView? = null
    private var chatMessagesBox: LinearLayout? = null
    private var activeChatId: String? = null
    private var activeChatOtherName: String? = null

    private lateinit var profileName: EditText
    private lateinit var profileCountry: EditText
    private lateinit var profileAge: EditText
    private lateinit var profileGender: TextView
    private lateinit var profileImage: ImageView
    private lateinit var profileStatus: TextView

    private val photoPrefs by lazy {
        getSharedPreferences("kvc_profile", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        ensurePublicProfile()
        showHome()
    }

    override fun onDestroy() {
        chatListener?.remove()
        chatListener = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        chatListener?.remove()
        chatListener = null
        activeChatId = null
        if (currentFeature != null) showHome() else super.onBackPressed()
    }

    private fun ensurePublicProfile() {
        val user = auth.currentUser ?: return
        val ref = db.collection("users").document(user.uid)

        ref.get().addOnSuccessListener { doc ->
            val name = doc.getString("name")
                ?: user.email?.substringBefore("@")
                ?: "Kingdom User"

            val country = doc.getString("country") ?: ""

            val publicData = hashMapOf<String, Any>(
                "name" to name,
                "country" to country,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            db.collection("publicProfiles")
                .document(user.uid)
                .set(publicData)

        }.addOnFailureListener {
            // Public profile will be created when user saves profile.
        }
    }

    private fun syncPublicProfile(name: String, country: String) {
        val user = auth.currentUser ?: return

        val data = hashMapOf<String, Any>(
            "name" to name,
            "country" to country,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("publicProfiles")
            .document(user.uid)
            .set(data)
            .addOnFailureListener { error ->
                notice("Public profile sync failed: ${error.localizedMessage}")
            }
    }

    private fun showHome() {
        chatListener?.remove()
        chatListener = null
        activeChatId = null
        currentFeature = null

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.isFillViewport = true

        val page = LinearLayout(this).apply {
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
        bell.setOnClickListener {
            showFeature("Notifications")
        }
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

        hero.addView(
            tile("🎙", "Explore Voice Rooms", cyan) {
                openRooms()
            },
            LinearLayout.LayoutParams(-1, dp(85)).apply {
                topMargin = dp(18)
            }
        )

        page.addView(
            hero,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(22)
            }
        )

        page.addView(
            tile("👑", "OWNER PANEL", gold) {
                openOwnerPanel()
            },
            LinearLayout.LayoutParams(-1, dp(85)).apply {
                topMargin = dp(18)
            }
        )

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
