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

    private var currentTab = "Home"
    private var currentFeature: String? = null

    private var selectedGender = ""

    private var chatListener: ListenerRegistration? = null
    private var chatScroll: ScrollView? = null
    private var chatMessagesBox: LinearLayout? = null

    private lateinit var profileName: EditText
    private lateinit var profileCountry: EditText
    private lateinit var profileAge: EditText
    private lateinit var profileGender: TextView
    private lateinit var profileImage: ImageView
    private lateinit var profileStatus: TextView

    private val photoPrefs by lazy {
        getSharedPreferences("kvc_profile", MODE_PRIVATE)
    }

    companion object {
        private const val PHOTO_REQUEST = 501
        private const val COUNTER_COLLECTION = "appCounters"
        private const val COUNTER_DOCUMENT = "userNumericId"
        private const val FIRST_NUMERIC_ID = 1000001L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        if (auth.currentUser == null) {
            finish()
            return
        }

        ensurePublicProfile()
        showTab("Home")
    }

    override fun onDestroy() {
        chatListener?.remove()
        chatListener = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        chatListener?.remove()
        chatListener = null

        if (currentFeature != null) {
            currentFeature = null
            showTab(currentTab)
        } else if (currentTab != "Home") {
            showTab("Home")
        } else {
            super.onBackPressed()
        }
    }

    // USER ID

    private fun ensurePublicProfile() {
        val user = auth.currentUser ?: return

        val userRef = db.collection("users").document(user.uid)
        val publicRef = db.collection("publicProfiles").document(user.uid)
        val counterRef = db.collection(COUNTER_COLLECTION)
            .document(COUNTER_DOCUMENT)

        db.runTransaction { transaction ->

            val userDoc = transaction.get(userRef)
            val publicDoc = transaction.get(publicRef)

            val existingId = userDoc.getLong("numericId")
                ?: publicDoc.getLong("numericId")

            val numericId = if (existingId != null) {
                existingId
            } else {
                val counterDoc = transaction.get(counterRef)
                val lastId = counterDoc.getLong("lastId")
                    ?: FIRST_NUMERIC_ID - 1

                val nextId = lastId + 1

                transaction.set(counterRef, mapOf("lastId" to nextId))
                nextId
            }

            val name = userDoc.getString("name")
                ?: user.email?.substringBefore("@")
                ?: "Kingdom User"

            val country = userDoc.getString("country") ?: ""

            transaction.set(
                userRef,
                mapOf(
                    "numericId" to numericId,
                    "name" to name,
                    "country" to country,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            transaction.set(
                publicRef,
                mapOf(
                    "numericId" to numericId,
                    "name" to name,
                    "country" to country,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            numericId
        }.addOnFailureListener {
            notice("User ID generation failed: ${it.localizedMessage}")
        }
    }

    private fun syncPublicProfile(name: String, country: String) {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { userDoc ->

                val numericId = userDoc.getLong("numericId")

                val data = hashMapOf<String, Any>(
                    "name" to name,
                    "country" to country,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                if (numericId != null) {
                    data["numericId"] = numericId
                }

                db.collection("publicProfiles")
                    .document(user.uid)
                    .set(
                        data,
                        com.google.firebase.firestore.SetOptions.merge()
                    )
            }
    }

    // NAVIGATION

    private fun showTab(tab: String) {
        currentTab = tab
        currentFeature = null

        chatListener?.remove()
        chatListener = null

        when (tab) {
            "Home" -> showHome()
            "Rooms" -> showRooms()
            "Messages" -> showMessages()
            "Profile" -> showProfileMenu()
        }
    }

    private fun createTabLayout(): Pair<LinearLayout, LinearLayout> {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        root.addView(bottomNavigation())

        return Pair(root, content)
    }

    private fun bottomNavigation(): LinearLayout {

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(7), dp(4), dp(7))
            setBackgroundColor(Color.rgb(19, 22, 49))
        }

        val items = listOf(
            Pair("⌂", "Home"),
            Pair("🎙", "Rooms"),
            Pair("✉", "Messages"),
            Pair("♙", "Profile")
        )

        items.forEach { item ->

            val selected = currentTab == item.second

            val button = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    showTab(item.second)
                }
            }

            button.addView(
                text(
                    item.first,
                    23f,
                    if (selected) cyan else muted,
                    true
                )
            )

            button.addView(
                text(
                    item.second,
                    11f,
                    if (selected) cyan else muted,
                    selected
                )
            )

            nav.addView(
                button,
                LinearLayout.LayoutParams(0, dp(55), 1f)
            )
        }

        return nav
    }

    // HOME

    private fun showHome() {

        val (root, page) = createTabLayout()

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(text("♛", 32f, cyan, true))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, 0, 0)

            addView(text("KINGDOM", 22f, white, true))
            addView(text("VOICE • CHAT • CONNECT", 10f, cyan, true))
        }

        header.addView(
            brand,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val notification = text("🔔", 23f, white, false)

        notification.setOnClickListener {
            showSimpleScreen(
                "Announcements",
                "Official announcements will appear here."
            )
        }

        header.addView(notification)
        page.addView(header)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(20))
            background = gradient(
                intArrayOf(
                    Color.rgb(49, 39, 111),
                    Color.rgb(16, 100, 125)
                ),
                24
            )
        }

        hero.addView(text("YOUR WORLD. YOUR VOICE.", 11f, cyan, true))

        hero.addView(
            text("Find your people.", 29f, white, true).apply {
                setPadding(0, dp(12), 0, dp(8))
            }
        )

        hero.addView(
            text(
                "Join rooms, meet friends and share your moments.",
                14f,
                white,
                false
            )
        )

        hero.addView(
            tile("🎙", "Explore Voice Rooms", cyan) {
                showTab("Rooms")
            },
            LinearLayout.LayoutParams(-1, dp(78)).apply {
                topMargin = dp(18)
            }
        )

        page.addView(
            hero,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(20)
            }
        )

        section(page, "Featured Rooms", "DISCOVER")

        addRow(
            page,
            "👑",
            "Official Rooms",
            "Explore Kingdom official rooms",
            gold
        ) {
            showTab("Rooms")
        }

        addRow(
            page,
            "🎙",
            "Normal Rooms",
            "Discover public voice rooms",
            cyan
        ) {
            showTab("Rooms")
        }

        section(page, "Kingdom Updates", "ANNOUNCEMENTS")

        addRow(
            page,
            "📢",
            "Official Announcements",
            "News and event announcements",
            purple
        ) {
            showSimpleScreen(
                "Official Announcements",
                "Official announcements will appear here."
            )
        }

        setContentView(root)
    }

    // ROOMS

    private fun showRooms() {

        val (root, page) = createTabLayout()

        page.addView(text("Explore Rooms", 25f, white, true))

        page.addView(
            text(
                "Find your favourite voice room.",
                13f,
                muted,
                false
            )
        )

        section(page, "Room Categories", "DISCOVER")

        addRow(
            page,
            "👑",
            "Official Rooms",
            "Kingdom official voice rooms",
            gold
        ) {
            openRooms()
        }

        addRow(
            page,
            "🎙",
            "Normal Rooms",
            "Personal and public rooms",
            cyan
        ) {
            openRooms()
        }

        addRow(
            page,
            "🏠",
            "Sub Rooms",
            "Explore available sub rooms",
            purple
        ) {
            openRooms()
        }

        page.addView(
            button("OPEN VOICE ROOMS") {
                openRooms()
            },
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                topMargin = dp(15)
            }
        )

        setContentView(root)
    }

    private fun openRooms() {
        try {
            startActivity(Intent(this, RoomActivity::class.java))
        } catch (e: Exception) {
            notice("Rooms could not open: ${e.localizedMessage}")
        }
    }

    // MESSAGES

    private fun showMessages() {

        val (root, page) = createTabLayout()

        page.addView(text("Messages", 25f, white, true))

        page.addView(
            text(
                "Connect with your Kingdom friends.",
                13f,
                muted,
                false
            )
        )

        section(page, "Your Connections", "MESSAGING")

        addRow(page, "💬", "Msgs", "Private conversations", cyan) {
            showPeople(true)
        }

        addRow(page, "🎙", "Voice", "Voice communication", purple) {
            showSimpleScreen(
                "Voice",
                "Voice communication feature."
            )
        }

        addRow(page, "👥", "Group", "Group conversations", gold) {
            showSimpleScreen(
                "Group",
                "Group conversations feature."
            )
        }

        addRow(page, "🤝", "Friends", "Your Kingdom friends", cyan) {
            showPeople(false)
        }

        addRow(page, "⚙", "System", "System messages and updates", pink) {
            showSimpleScreen(
                "System",
                "System messages and updates will appear here."
            )
        }

        setContentView(root)
    }

    // PROFILE MENU

    private fun showProfileMenu() {

        val (root, page) = createTabLayout()

        page.addView(text("My Profile", 25f, white, true))

        val user = auth.currentUser
        val uid = user?.uid

        val accountCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(15), dp(14), dp(15))
            background = cardBackground()
        }

        val avatar = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(65), dp(65))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = circleBackground()
            clipToOutline = true
        }

        loadLocalPhoto()?.let {
            try {
                avatar.setImageURI(it)
            } catch (_: Exception) {
            }
        }

        accountCard.addView(avatar)

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        details.addView(
            text(
                user?.displayName ?: user?.email?.substringBefore("@")
                ?: "Kingdom User",
                17f,
                white,
                true
            )
        )

        details.addView(
            text(
                user?.email ?: "Account",
                12f,
                muted,
                false
            )
        )

        val idText = text("USER ID: Loading...", 12f, cyan, true)
        details.addView(idText)

        accountCard.addView(
            details,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        page.addView(
            accountCard,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(15)
            }
        )

        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (!isFinishing && !isDestroyed) {
                        val numericId = doc.getLong("numericId")
                        idText.text = "USER ID: ${numericId ?: "Generating..."}"
                    }
                }
        }

        section(page, "My Account", "PROFILE")

        addRow(page, "🖼", "DP & Cover", "Profile photo and cover", cyan) {
            showProfile()
        }

        addRow(page, "⭐", "Level", "Account level and progress", gold) {
            showSimpleScreen("Level", "Level information will appear here.")
        }

        addRow(page, "👥", "Followers", "People following you", purple) {
            showPeople(false)
        }

        addRow(page, "🤝", "Following", "People you follow", cyan) {
            showPeople(false)
        }

        addRow(page, "🪙", "Wallet", "Coins and Pearls balance", gold) {
            showWallet()
        }

        section(page, "Preferences", "SETTINGS")

        addRow(page, "⚙", "Settings", "Account and privacy settings", purple) {
            showAccountSettings()
        }

        addRow(page, "🚪", "Sign Out", "Sign out of this device", pink) {
            confirmSignOut()
        }

        addRow(page, "🗑", "Delete Account", "Delete your sign-in account", pink) {
            confirmDeleteAccount()
        }

        setContentView(root)
    }

    // PROFILE EDITING

    private fun showProfile() {
        currentFeature = "My Profile"

        val screen = page("My Profile")

        val photoBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = cardBackground()
        }

        profileImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(100), dp(100))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = circleBackground()
            clipToOutline = true
        }

        photoBox.addView(profileImage)

        loadLocalPhoto()?.let {
            showPhoto(it)
        }

        photoBox.addView(
            button("CHANGE PROFILE PHOTO") {
                choosePhoto()
            },
            LinearLayout.LayoutParams(-1, dp(48)).apply {
                topMargin = dp(12)
            }
        )

        screen.addView(
            photoBox,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(15)
            }
        )

        val coverButton = button("CHANGE COVER PHOTO") {
            showSimpleScreen(
                "Cover Photo",
                "Cloud cover photo upload is not connected yet."
            )
        }

        screen.addView(
            coverButton,
            LinearLayout.LayoutParams(-1, dp(48)).apply {
                topMargin = dp(12)
            }
        )

        val user = auth.currentUser

        profileStatus = text("Loading account ID...", 14f, cyan, true)
        screen.addView(profileStatus)

        if (user != null) {
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (!isFinishing && !isDestroyed) {
                        val numericId = doc.getLong("numericId")
                        profileStatus.text =
                            "KINGDOM USER ID: ${numericId ?: "Generating..."}"
                    }
                }
        }

        profileName = inputField("Profile name")
        profileCountry = inputField("Country")
        profileAge = inputField("Age")

        profileGender = text(
            "Select gender  ›",
            15f,
            white,
            true
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
            set
