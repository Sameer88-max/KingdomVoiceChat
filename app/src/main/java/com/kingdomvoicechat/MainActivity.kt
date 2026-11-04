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
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions

class MainActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val functions by lazy { FirebaseFunctions.getInstance() }
    private val prefs by lazy { getSharedPreferences("kvc_profile", MODE_PRIVATE) }

    private val bg = Color.rgb(4, 8, 38)
    private val panel = Color.rgb(13, 22, 67)
    private val panel2 = Color.rgb(20, 29, 82)
    private val cyan = Color.rgb(0, 224, 255)
    private val blue = Color.rgb(20, 104, 255)
    private val purple = Color.rgb(174, 0, 255)
    private val white = Color.WHITE
    private val muted = Color.rgb(184, 199, 235)

    private var currentTab = "Home"
    private val draftRooms = mutableListOf<RoomModel>()
    private var currentFeature: String? = null
    private var selectedGender = "Not specified"

    private lateinit var profileName: EditText
    private lateinit var profileCountry: EditText
    private lateinit var profileAge: EditText
    private lateinit var profileImage: ImageView

    companion object {
        private const val PHOTO_REQUEST = 501
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KingdomTheme.applyTo(this)

        if (auth.currentUser == null) {
            finish()
            return
        }

        ensurePublicProfile()
        showTab("Home")
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (currentFeature != null) {
            currentFeature = null
            showTab(currentTab)
        } else if (currentTab != "Home") {
            showTab("Home")
        } else {
            super.onBackPressed()
        }
    }

    // FIREBASE PROFILE SYNC

    private fun ensurePublicProfile() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name")
                    ?: user.email?.substringBefore("@")
                    ?: "Kingdom User"

                val country = doc.getString("country") ?: ""

                syncPublicProfile(name, country) { ok, message ->
                    if (!ok) notice(message)
                }
            }
            .addOnFailureListener {
                notice("Profile load failed: ${it.localizedMessage}")
            }
    }

    private fun syncPublicProfile(
        name: String,
        country: String,
        callback: ((Boolean, String) -> Unit)? = null
    ) {
        val data = hashMapOf<String, Any>(
            "name" to name,
            "country" to country
        )

        functions.getHttpsCallable("syncPublicProfile").call(data)
            .addOnSuccessListener {
                callback?.invoke(true, "Profile synced successfully.")
            }
            .addOnFailureListener { error ->
                callback?.invoke(
                    false,
                    error.localizedMessage ?: "Profile sync failed"
                )
            }
    }

    // APP SHELL / NAVIGATION

    private fun showTab(tab: String) {
        currentTab = tab
        currentFeature = null

        when (tab) {
            "Home" -> showHome()
            "Rooms" -> showRooms()
            "Messages" -> showMessages()
            "Profile" -> showProfileMenu()
        }
    }

    private fun appRoot(): Pair<LinearLayout, LinearLayout> {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(18))
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNavigation())

        return root to content
    }

    private fun bottomNavigation(): LinearLayout {return BottomNavigation(this, currentTab) {
    showTab(it)
}.build()
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(7), dp(7), dp(7), dp(7))
            background = rounded(
                intArrayOf(
                    Color.rgb(8, 15, 54),
                    Color.rgb(13, 25, 75)
                ),
                28,
                cyan,
                1
            )
        }

        listOf(
            "⌂" to "Home",
            "🎙" to "Rooms",
            "✉" to "Messages",
            "♙" to "Profile"
        ).forEach { item ->

            val active = currentTab == item.second

            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(7), dp(4), dp(7))

                if (active) {
                    background = rounded(
                        intArrayOf(
                            Color.rgb(8, 95, 190),
                            Color.rgb(13, 35, 105)
                        ),
                        22,
                        cyan,
                        1
                    )
                }

                isClickable = true
                setOnClickListener {
                    showTab(item.second)
                }
            }

            cell.addView(
                text(
                    item.first,
                    24f,
                    if (active) cyan else muted,
                    true
                )
            )

            cell.addView(
                text(
                    item.second,
                    11f,
                    if (active) cyan else muted,
                    true
                )
            )

            nav.addView(
                cell,
                LinearLayout.LayoutParams(0, dp(62), 1f)
            )
        }

        return nav
    }

    // HOME SCREEN

    private fun showHome() {val (root, content) = appRoot()
addTopHeader(content, true)
content.addView(HomeScreen(this, { openCreateRoom() }, { showRoomSearch() }).build())
setContentView(root)
return
        }
 /*
        {
        val (root, content) = appRoot()

        addTopHeader(content, true)

        val banner = neonCard().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(18), dp(14), dp(18))
        }

        banner.addView(text("🎙", 48f, cyan, true))

        val copy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, 0, 0)
        }

        copy.addView(text("Welcome to", 18f, white, true))
        copy.addView(text("Kingdom Voice Chat", 23f, cyan, true))
        copy.addView(text("Sing • Chat • Make Friends", 12f, white))

        banner.addView(copy)
        banner.addView(text("👑", 42f, Color.YELLOW, true))

        content.addView(banner, matchWrap(top = 12))

        section(content, "🔥  Live Rooms", "See All") {
            showTab("Rooms")
        }

        val live = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        live.addView(
            roomMiniCard(
                "Music Forever",
                "Saba Melody",
                "505",
                "23.5K",
                "🎵"
            ) {
                enterRoom("505")
            },
            weighted()
        )

        live.addView(
            roomMiniCard(
                "Sing Together",
                "Sameer ZS",
                "821",
                "18.7K",
                "🎤"
            ) {
                enterRoom("821")
            },
            weighted()
        )

        live.addView(
            roomMiniCard(
                "Fun & Chat",
                "Zeba Sameer",
                "505",
                "12.9K",
                "💬"
            ) {
                enterRoom("505")
            },
            weighted()
        )

        content.addView(live, matchWrap(top = 8))

        section(content, "⭐  Popular Rooms", "See All") {
            showTab("Rooms")
        }

        val popular = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        listOf(
            Triple("Star Singer Club", "8.2K Online", "🎙"),
            Triple("Royal Family", "6.7K Online", "👑"),
            Triple("Music Mania", "5.9K Online", "🎶")
        ).forEach { item ->
            popular.addView(
                popularCard(
                    item.first,
                    item.second,
                    item.third
                ) {
                    showTab("Rooms")
                },
                weighted()
         )
        }
        

        content.addView(popular, matchWrap(top = 8))

        val rewards = neonCard().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(15), dp(14), dp(15))
        }

        rewards.addView(text("🎁", 42f, Color.YELLOW, true))

        val rewardText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, dp(8), 0)
        }

        rewardText.addView(
            text("Daily Rewards", 19f, Color.YELLOW, true)
        )

        rewardText.addView(
            text("Login daily & get free coins!", 12f, white)
        )

        rewards.addView(
            rewardText,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        rewards.addView(
            button("Claim Now") {
                notice(
                    "Daily rewards will be available when rewards service is connected."
                )
            }
        )

        content.addView(rewards, matchWrap(top = 18))
// /*
        setContentView(root) // l
    }
*/
 */
    // ROOMS SCREEN

    private fun showRooms() {val (root, content) = appRoot()
addTopHeader(content, true)
content.addView(RoomsScreen(this, draftRooms, { openCreateRoom() }, { showRoomSearch() }) { enterRoom(it.roomId) }.build())
setContentView(root)
return
        /*
        {val (root, content) = appRoot()

        addTopHeader(content, true)

        content.addView(
            neonBanner(
                "🎙",
                "Join Amazing",
                "Voice Rooms",
                "Sing • Chat • Make Friends"
            ),
            matchWrap(top = 10)
        )

        val filters = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        listOf(
            "All Rooms",
            "Singing",
            "Gaming",
            "Fun",
            "Quiz"
        ).forEachIndexed { index, label ->
            filters.addView(
                chip(label, index == 0) {
                    notice("$label rooms selected")
                },
                weighted()
            )
        }

        content.addView(filters, matchWrap(top = 12))

        val grid = listOf(
            Triple("Music Lovers Club", "Sing & Enjoy Together", "505"),
            Triple("Gaming Zone", "Play • Chat • Fun", "821"),
            Triple("Fun Time Club", "Laugh • Chat • Chill", "505"),
            Triple("Quiz Challenge", "Test Your Knowledge", "821"),
            Triple("Music Mania", "Best Songs & Vibes", "505"),
            Triple("StarLeo Concert", "Sing Like a Star", "821")
        )

        grid.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            pair.forEach { item ->
                row.addView(
                    roomLargeCard(
                        item.first,
                        item.second,
                        item.third
                    ) {
                        enterRoom(item.third)
                    },
                    weighted()
                )
            }

            if (pair.size == 1) {
                row.addView(View(this), weighted())
            }

            content.addView(row, matchWrap(top = 10))
        }
        */
       //   setContentView(root)//
    }

    // MESSAGES SCREEN

    private fun showMessages() {val (root, content) = appRoot()
addTopHeader(content, true)
content.addView(MessagesScreen(this).build())
setContentView(root)
return
        }
 /*
        {
        val (root, content) = appRoot()

        addTopHeader(content, true)

        content.addView(
            neonBanner(
                "💬",
                "Stay Connected",
                "Chat & Messages",
                "Make Friends • Share Moments"
            ),
            matchWrap(top = 10)
        )

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        listOf(
            "All Chats",
            "Friends",
            "Groups",
            "System"
        ).forEachIndexed { i, label ->
            tabs.addView(
                chip(label, i == 0) {
                    notice("$label selected")
                },
                weighted()
            )
        }

        content.addView(tabs, matchWrap(top = 12))

        val search = EditText(this).apply {
            hint = "⌕  Search chats, users, or room..."
            setTextColor(white)
            setHintTextColor(muted)
            textSize = 14f
            setPadding(dp(15), dp(12), dp(15), dp(12))
            background = rounded(
                intArrayOf(
                    Color.rgb(10, 22, 72),
                    Color.rgb(14, 28, 82)
                ),
                24,
                Color.rgb(46, 75, 150),
                1
            )
        }

        content.addView(search, matchWrap(top = 12))

        val chats = listOf(
            Triple("King Sameer", "Hello bro! How are you?", "👑"),
            Triple("Saba Melody", "Thank you for joining the room ❤️", "🎤"),
            Triple("Room 821 Family", "Zeba Ansari: Good night everyone 🌙", "👥"),
            Triple("Peter", "Bro, tomorrow live concert hai 👍", "🧑"),
            Triple("Zeba Sameer Siddiqui", "Voice message (0:32)", "👑"),
            Triple("Official Events", "New Event: One Day Singing Competition 🎉", "📢"),
            Triple("Riya Khan", "Hahaha! Nice voice 😊", "👩"),
            Triple("StarLeo Clan", "Welcome to StarLeo Concert House 821", "🦁")
        )

        chats.forEachIndexed { index, chat ->
            addChatRow(
                content,
                chat.first,
                chat.second,
                chat.third,
                if (index == 0 || index == 1) "1" else ""
            ) {
                showSimpleScreen(
                    chat.first,
                    "Conversation UI will appear here after the chat backend is connected."
                )
                 // /*
            }
        }

        setContentView(root)
    }
     */
     */
private fun openCreateRoom() {
    CreateRoomDialog(this) { name, id -> createDraftRoom(name, id) }.show()
}

private fun createDraftRoom(name: String, id: String) {
    if (draftRooms.any { it.roomId == id }) { notice("Room ID already exists"); return }
    val user = auth.currentUser
    draftRooms.add(RoomModel(id, name, user?.uid ?: "", user?.email?.substringBefore("@") ?: "Kingdom User", onlineCount = 1, isOnline = true))
    notice("Room created")
    showTab("Rooms")
}

private fun showRoomSearch() {
    RoomSearchDialog(this) { query ->
        val room = draftRooms.find { it.roomId.equals(query, true) }
        if (room != null) enterRoom(room.roomId) else notice("Room not found")
    }.show()
}
    // PROFILE SCREEN

    private fun showProfileMenu() {
        val (root, content) = appRoot()
        addTopHeader(content, true)

        val user = auth.currentUser

        val profile = neonCard().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }

        val identity = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val avatar = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(94), dp(94))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = circleBackground()
            clipToOutline = true
        }

        loadProfileImage(avatar)
        identity.addView(avatar)

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), 0, 0, 0)
        }

        details.addView(
            text(
                user?.email?.substringBefore("@") ?: "Kingdom User",
                20f,
                white,
                true
            )
        )

        details.addView(
            text(
                "✔  ID: ${user?.uid?.take(8) ?: "—"}",
                13f,
                cyan,
                true
            )
        )

        details.addView(
            text(
    "⭐ Level ${UserLevel.newUser().level}      XP ${UserLevel.newUser().xp}",
    13f,
    Color.MAGENTA,
    true
)
        )

        details.addView(
            text("📍 Pakistan  •  ♂ Male", 12f, muted)
        )

        identity.addView(
            details,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        profile.addView(identity)
        content.addView(profile, matchWrap(top = 12))

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = rounded(
                intArrayOf(
                    Color.rgb(9, 18, 60),
                    Color.rgb(18, 29, 82)
                ),
                22,
                Color.rgb(42, 66, 135),
                1
            )
            setPadding(dp(5), dp(14), dp(5), dp(14))
        }

        listOf(
            "👥" to "Followers",
            "🫂" to "Following",
            "🎁" to "Gifts",
            "💎" to "Coins"
        ).forEach { item ->

            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            cell.addView(text(item.first, 23f, cyan, true))
            cell.addView(text("0", 17f, white, true))
            cell.addView(text(item.second, 10f, muted))

            stats.addView(
                cell,
                LinearLayout.LayoutParams(0, -2, 1f)
            )
        }

        content.addView(stats, matchWrap(top = 12))

        content.addView(
            button("✎   Edit Profile") {
                showProfile()
            },
            matchWrap(top = 12)
        )

        addProfileOption(
            content,
            "👛",
            "Wallet",
            "Your Coins & Balance"
        ) {
            showWallet()
        }

        addProfileOption(
            content,
            "🎁",
            "Gifts Received",
            "Your Received Gifts"
        ) {
            showSimpleScreen(
                "Gifts Received",
                "Your received gifts will appear here when connected."
            )
        }

        addProfileOption(
            content,
            "👑",
            "Levels",
            "Your Growth & Rank"
        ) {
            showSimpleScreen(
                "Levels",
                "Your level and rank information will appear here."
            )
        }

        addProfileOption(
            content,
            "💬",
            "Message Requests",
            "New Messages"
        ) {
            showTab("Messages")
        }

        addProfileOption(
            content,
            "👥",
            "My Followers",
            "People Who Follow You"
        ) {
            showPeople()
        }

        addProfileOption(
            content,
            "⚙",
            "Settings",
            "App Preferences"
        ) {
            showAccountSettings()
        }

        addProfileOption(
            content,
            "ℹ",
            "About Kingdom Voice Chat",
            "Version 1.0.0"
        ) {
            showSimpleScreen(
                "About Kingdom Voice Chat",
                "Kingdom Voice Chat • Version 1.0.0"
            )
        }

        addProfileOption(
            content,
            "🚪",
            "Sign Out",
            "Sign out of your account"
        ) {
            confirmSignOut()
        }

        setContentView(root)
    }

    private fun addTopHeader(
        parent: LinearLayout,
        actions: Boolean
    ) {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(text("👑", 35f, Color.YELLOW, true))

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(7), 0, 0, 0)
        }

        brand.addView(text("Kingdom", 23f, white, true))
        brand.addView(text("Voice Chat", 15f, cyan, true))

        header.addView(
            brand,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        if (actions) {
            header.addView(
                text("⌕", 30f, white, true).apply {
                    setPadding(dp(8), 0, dp(8), 0)
                    setOnClickListener { showPeople() }
                }
            )

            header.addView(
                text("♧", 27f, white, true).apply {
                    setPadding(dp(8), 0, dp(8), 0)
                    setOnClickListener {
                        notice("No new notifications")
                    }
                }
            )

            header.addView(
                text("⚙", 27f, white, true).apply {
                    setPadding(dp(8), 0, 0, 0)
                    setOnClickListener { showAccountSettings() }
                }
            )
        }

        parent.addView(header)
    }

    // PROFILE EDITOR

    private fun showProfile() {
        currentFeature = "Edit Profile"

        val root = page("Edit Profile")
        val content = root.tag as LinearLayout

        profileImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(110), dp(110))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = circleBackground()
            clipToOutline = true
        }

        loadProfileImage(profileImage)

        content.addView(
            profileImage,
            LinearLayout.LayoutParams(dp(110), dp(110)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )

        content.addView(
            button("Change Profile Photo") {
                choosePhoto()
            },
            matchWrap(top = 10)
        )

        profileName = inputField("Your Name")
        profileCountry = inputField("Country")
        profileAge = inputField("Age")

        content.addView(label("Name"))
        content.addView(profileName)
        content.addView(label("Country"))
        content.addView(profileCountry)
        content.addView(label("Age"))
        content.addView(profileAge)
        content.addView(label("Gender"))

        lateinit var genderButton: TextView

        genderButton = button("Select Gender: $selectedGender") {
            val options = arrayOf(
                "Male",
                "Female",
                "Other",
                "Not specified"
            )

            AlertDialog.Builder(this)
                .setTitle("Select Gender")
                .setItems(options) { _, which ->
                    selectedGender = options[which]
                    genderButton.text = "Select Gender: $selectedGender"
                }
                .show()
        }

        content.addView(genderButton)

        content.addView(
            button("Save Profile") {

                val name = profileName.text.toString().trim()
                val country = profileCountry.text.toString().trim()
                val ageText = profileAge.text.toString().trim()

                if (name.isBlank()) {
                    notice("Please enter your name")
                    return@button
                }

                if (name.length > 40) {
                    notice("Name must be 40 characters or less")
                    return@button
                }

                if (country.length > 60) {
                    notice("Country must be 60 characters or less")
                    return@button
                }

                val age = if (ageText.isBlank()) {
                    null
                } else {
                    ageText.toIntOrNull()
                }

                if (ageText.isNotBlank() && (age == null || age !in 1..120)) {
                    notice("Enter a valid age")
                    return@button
                }

                val user = auth.currentUser ?: return@button

                syncPublicProfile(name, country) { success, message ->

                    if (!success) {
                        notice(message)
                        return@syncPublicProfile
                    }

                    val privateData = hashMapOf<String, Any>(
                        "name" to name,
                        "country" to country,
                        "gender" to selectedGender,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    if (age != null) {
                        privateData["age"] = age
                    }

                    db.collection("users")
                        .document(user.uid)
                        .set(privateData, SetOptions.merge())
                        .addOnSuccessListener {
                            notice(message)
                            currentFeature = null
                            showProfileMenu()
                        }
                        .addOnFailureListener {
                            notice(
                                it.localizedMessage
                                    ?: "Private profile save failed"
                            )
                        }
                }
            },
            matchWrap(top = 18)
        )

        setContentView(root)

        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->

                    if (isFinishing || isDestroyed) {
                        return@addOnSuccessListener
                    }

                    profileName.setText(doc.getString("name") ?: "")
                    profileCountry.setText(doc.getString("country") ?: "")
                    profileAge.setText(doc.get("age")?.toString() ?: "")

                    selectedGender =
                        doc.getString("gender") ?: "Not specified"

                    genderButton.text = "Select Gender: $selectedGender"
                }
                .addOnFailureListener {
                    notice(it.localizedMessage ?: "Unable to load profile")
                }
        }
    }

    // PHOTO PICKER

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }

        startActivityForResult(intent, PHOTO_REQUEST)
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            prefs.edit()
                .putString("profile_photo", uri.toString())
                .apply()

            if (::profileImage.isInitialized) {
                profileImage.setImageURI(uri)
            }

            notice("Profile photo updated")
        }
    }

    private fun loadProfileImage(image: ImageView) {
        val saved = prefs.getString("profile_photo", null)

        if (!saved.isNullOrBlank()) {
            try {
                image.setImageURI(Uri.parse(saved))
            } catch (_: Exception) {
                image.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        } else {
            image.setImageResource(android.R.drawable.sym_def_app_icon)
        }
    }

    // WALLET / GIFTS

    private fun showWallet() {
        currentFeature = "Wallet"

        val root = page("My Wallet")
        val content = root.tag as LinearLayout

        val balance = text("Loading wallet...", 22f, cyan, true)

        content.addView(balance, matchWrap(top = 20))

        content.addView(
            button("Refresh Balance") {
                loadWallet(balance)
            },
            matchWrap(top = 12)
        )

        content.addView(
            button("Open Gift Store") {
                showGiftStore()
            },
            matchWrap(top = 12)
        )

        content.addView(
            text(
                "Wallet balance is provided by the Kingdom Wallet API.",
                12f,
                muted
            )
        )

        setContentView(root)
        loadWallet(balance)
    }

    private fun loadWallet(target: TextView) {
        target.text = "Loading wallet..."

        WalletApi.getWallet(object : WalletApi.Callback<WalletBalance> {

            override fun onSuccess(value: WalletBalance) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        target.text =
                            "🪙 Coins: ${value.coins}\n💎 Pearls: ${value.pearls}"
                    }
                }
            }

            override fun onError(message: String) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        target.text = "Wallet unavailable"
                        notice(message)
                    }
                }
            }
        })
    }

    private fun showGiftStore() {
        currentFeature = "Gift Store"

        val root = page("Gift Store")
        val content = root.tag as LinearLayout

        content.addView(text("Available gifts", 15f, muted))

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(list)
        setContentView(root)

        list.addView(text("Loading gifts...", 14f, muted))

        WalletApi.getGifts(object : WalletApi.Callback<List<GiftItem>> {

            override fun onSuccess(value: List<GiftItem>) {
                runOnUiThread {

                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    list.removeAllViews()

                    if (value.isEmpty()) {
                        list.addView(
                            text("No gifts available.", 14f, muted)
                        )
                        return@runOnUiThread
                    }

                    value.forEach { gift ->
                        addProfileOption(
                            list,
                            "🎁",
                            gift.name,
                            "Price: ${gift.price} Coins"
                        ) {
                            askGiftReceiver(gift)
                        }
                    }
                }
            }

            override fun onError(message: String) {
                runOnUiThread {

                    if (isFinishing || isDestroyed) {
                        return@runOnUiThread
                    }

                    list.removeAllViews()
                    list.addView(
                        text("Unable to load gifts.", 14f, muted)
                    )
                    notice(message)
                }
            }
        })
    }

    private fun askGiftReceiver(gift: GiftItem) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
        }

        val receiver = EditText(this).apply {
            hint = "Receiver Firebase UID"
            setTextColor(white)
            setHintTextColor(muted)
        }

        val room = EditText(this).apply {
            hint = "Room ID"
            setTextColor(white)
            setHintTextColor(muted)
        }

        box.addView(receiver)
        box.addView(room)

        AlertDialog.Builder(this)
            .setTitle("Send ${gift.name}")
            .setMessage("Gift price: ${gift.price} Coins")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Send Gift") { _, _ ->

                val uid = receiver.text.toString().trim()
                val roomId = room.text.toString().trim()

                if (uid.isBlank() || roomId.isBlank()) {
                    notice("Enter receiver UID and Room ID")
                    return@setPositiveButton
                }

                WalletApi.sendGift(
                    uid,
                    gift.id,
                    roomId,
                    object : WalletApi.Callback<org.json.JSONObject> {

                        override fun onSuccess(value: org.json.JSONObject) {
                            runOnUiThread {
                                notice("Gift sent successfully")
                                showWallet()
                            }
                        }

                        override fun onError(message: String) {
                            runOnUiThread {
                                notice(message)
                            }
                        }
                    }
                )
            }
            .show()
    }

    // SETTINGS / SIGN OUT

    private fun showAccountSettings() {
        currentFeature = "Settings"

        val root = page("Settings")
        val content = root.tag as LinearLayout

        addProfileOption(
            content,
            "🎨",
            "Theme Settings",
            "Change app appearance"
        ) {
            startActivity(Intent(this, ThemeSettingsActivity::class.java))
        }

        addProfileOption(
            content,
            "👑",
            "Owner Panel",
            "Open owner controls"
        ) {
            startActivity(Intent(this, OwnerPanelActivity::class.java))
        }

        addProfileOption(
            content,
            "🚪",
            "Sign Out",
            "Sign out of your account"
        ) {
            confirmSignOut()
        }

        setContentView(root)
    }

    private fun confirmSignOut() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Sign Out") { _, _ ->

                auth.signOut()

                startActivity(
                    Intent(this, AuthActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )

                finish()
            }
            .show()
    }

    // PEOPLE / GENERIC PAGES

    private fun showPeople() {
        currentFeature = "People"

        val root = page("People")
        val content = root.tag as LinearLayout

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(list)
        setContentView(root)

        list.addView(text("Loading users...", 14f, muted))

        db.collection("publicProfiles")
            .limit(50)
            .get()
            .addOnSuccessListener { result ->

                list.removeAllViews()

                if (result.isEmpty) {
                    list.addView(text("No users found.", 14f, muted))
                    return@addOnSuccessListener
                }

                result.documents.forEach { doc ->
                    val name = doc.getString("name") ?: "Kingdom User"
                    val country = doc.getString("country") ?: ""
                    val id = doc.getLong("numericId") ?: 0L

                    addProfileOption(
                        list,
                        "👤",
                        name,
                        "$country • ID: $id"
                    ) {
                        showUserDetails(doc.id, name, country)
                    }
                }
            }
            .addOnFailureListener {
                list.removeAllViews()
                list.addView(
                    text("Unable to load users.", 14f, muted)
                )
                notice(it.localizedMessage ?: "Unable to load users")
            }
    }

    private fun showUserDetails(
        uid: String,
        name: String,
        country: String
    ) {
        AlertDialog.Builder(this)
            .setTitle(name)
            .setMessage("Country: $country\nUser UID:\n$uid")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSimpleScreen(title: String, message: String) {
        currentFeature = title

        val root = page(title)
        val content = root.tag as LinearLayout

        content.addView(
            text(message, 14f, muted).apply {
                setPadding(0, dp(25), 0, 0)
            }
        )

        setContentView(root)
    }

    // REUSABLE VISUAL COMPONENTS

    private fun page(title: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(16))
            setBackgroundColor(bg)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(
            text("‹", 34f, cyan, true).apply {
                setOnClickListener {
                    currentFeature = null
                    showTab(currentTab)
                }
            }
        )

        header.addView(
            text(title, 22f, white, true).apply {
                setPadding(dp(12), 0, 0, 0)
            }
        )

        root.addView(header)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        root.tag = content
        return root
    }

    private fun section(
        parent: LinearLayout,
        title: String,
        actionTitle: String? = null,
        action: (() -> Unit)? = null
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(18), 0, dp(6))
        }

        row.addView(
            text(title, 19f, white, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        if (actionTitle != null) {
            row.addView(
                text("$actionTitle  ➜", 13f, cyan, true).apply {
                    setOnClickListener {
                        action?.invoke()
                    }
                }
            )
        }

        parent.addView(row)
    }

    private fun roomMiniCard(
        name: String,
        host: String,
        id: String,
        viewers: String,
        icon: String,
        action: () -> Unit
    ): LinearLayout {
        val box = neonCard().apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(7), dp(10), dp(7), dp(10))
            setOnClickListener { action() }
        }

        box.addView(text("🔴 LIVE", 10f, Color.RED, true))
        box.addView(text(icon, 34f, cyan, true))
        box.addView(text(name, 12f, white, true))
        box.addView(text("👑 $host", 10f, muted))
        box.addView(text("👥 $viewers", 10f, cyan, true))

        box.addView(
            button("Join") { action() },
            matchWrap(top = 7)
        )

        return box
    }

    private fun popularCard(
        name: String,
        online: String,
        icon: String,
        action: () -> Unit
    ): LinearLayout {
        val box = neonCard().apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(6), dp(12), dp(6), dp(12))
            setOnClickListener { action() }
        }

        box.addView(text(icon, 37f, cyan, true))
        box.addView(text(name, 12f, white, true))
        box.addView(text("🔥 $online", 10f, muted))

        box.addView(
            button("Join") { action() },
            matchWrap(top = 8)
        )

        return box
    }

    private fun roomLargeCard(
        name: String,
        subtitle: String,
        roomId: String,
        action: () -> Unit
    ): LinearLayout {
        val box = neonCard().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(12), dp(10), dp(12))
        }

        box.addView(text("🔴 LIVE       👥 Online", 11f, cyan, true))

        box.addView(
            text(
                when {
                    name.contains("Gaming") -> "🎮"
                    name.contains("Quiz") -> "❓"
                    name.contains("StarLeo") -> "⭐"
                    name.contains("Music") -> "🎙"
                    else -> "😎"
                },
                43f,
                cyan,
                true
            ).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, dp(8))
            }
        )

        box.addView(text("👑 $name", 13f, white, true))
        box.addView(text(subtitle, 11f, muted))

        box.addView(
            text("⌂ Room $roomId", 11f, cyan, true).apply {
                setPadding(0, dp(8), 0, dp(8))
            }
        )

        box.addView(button("Join  ›") { action() })

        return box
    }

    private fun addChatRow(
    parent: LinearLayout,
    name: String,
    message: String,
    icon: String,
    unread: String,
    action: () -> Unit
) {
    val row = neonCard().apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(10), dp(12), dp(10), dp(12))
        setOnClickListener { action() }
    }

    row.addView(text(icon, 35f, cyan, true))

    val labels = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(10), 0, 0, 0)
    }

    labels.addView(text(name, 15f, white, true))
    labels.addView(text(message, 12f, muted))

    row.addView(
        labels,
        LinearLayout.LayoutParams(0, -2, 1f)
    )

    if (unread.isNotEmpty()) {
        row.addView(
            text(unread, 14f, white, true).apply {
                background = rounded(
                    intArrayOf(Color.RED, Color.rgb(210, 20, 60)),
                    30
                )
                setPadding(dp(9), dp(5), dp(9), dp(5))
            }
        )
    }

    parent.addView(row, matchWrap(top = 8))
}

    private fun addProfileOption(
        parent: LinearLayout,
        icon: String,
        title: String,
        subtitle: String,
        action: () -> Unit
    ) {
        val row = neonCard().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(13), dp(13), dp(13), dp(13))
            setOnClickListener { action() }
        }

        row.addView(text(icon, 28f, cyan, true))

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        labels.addView(text(title, 15f, white, true))
        labels.addView(text(subtitle, 11f, muted))

        row.addView(
            labels,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(text("›", 27f, muted, true))

        parent.addView(row, matchWrap(top = 8))
    }

    private fun neonBanner(
        icon: String,
        first: String,
        second: String,
        subtitle: String
    ): LinearLayout {
        val box = neonCard().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(14), dp(12), dp(14))
        }

        box.addView(text(icon, 38f, cyan, true))

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), 0, 0, 0)
        }

        labels.addView(text(first, 16f, white, true))
        labels.addView(text(second, 23f, cyan, true))
        labels.addView(text(subtitle, 11f, muted))

        box.addView(labels)
        box.addView(text("👑", 30f, Color.YELLOW, true))

        return box
    }

    private fun chip(
        title: String,
        selected: Boolean,
        action: () -> Unit
    ): TextView {
        return text(
            title,
            10f,
            if (selected) white else muted,
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(11), dp(5), dp(11))

            background = rounded(
                if (selected) {
                    intArrayOf(
                        Color.rgb(0, 150, 230),
                        Color.rgb(65, 35, 210)
                    )
                } else {
                    intArrayOf(
                        Color.rgb(12, 20, 65),
                        Color.rgb(20, 28, 75)
                    )
                },
                24,
                if (selected) cyan else Color.rgb(48, 66, 130),
                1
            )

            setOnClickListener { action() }
        }
    }

    private fun neonCard(): LinearLayout {
        return LinearLayout(this).apply {
            background = rounded(
                intArrayOf(
                    Color.rgb(12, 20, 67),
                    Color.rgb(24, 24, 83)
                ),
                22,
                Color.rgb(55, 75, 155),
                1
            )
        }
    }

    private fun button(
        title: String,
        action: () -> Unit
    ): TextView {
        return text(title, 14f, white, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))

            background = rounded(
                intArrayOf(cyan, blue, purple),
                28,
                cyan,
                1
            )

            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun inputField(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            setTextColor(white)
            setHintTextColor(muted)
            textSize = 15f
            setPadding(dp(13), dp(12), dp(13), dp(12))

            background = rounded(
                intArrayOf(
                    Color.rgb(12, 20, 65),
                    Color.rgb(20, 29, 80)
                ),
                16,
                Color.rgb(55, 75, 140),
                1
            )
        }
    }

    private fun label(value: String): TextView {
        return text(value, 13f, cyan, true).apply {
            setPadding(0, dp(14), 0, dp(6))
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)

            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    private fun rounded(
    colors: IntArray,
    radius: Int,
    stroke: Int = Color.TRANSPARENT,
    strokeWidth: Int = 0
): GradientDrawable {
    return GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        colors
    ).apply {
        cornerRadius = dp(radius).toFloat()

        if (strokeWidth > 0) {
            setStroke(dp(strokeWidth), stroke)
        }
    }
}

    private fun circleBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(panel)
            setStroke(dp(2), cyan)
        }
    }

    private fun matchWrap(top: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(-1, -2).apply {
            if (top > 0) {
                topMargin = dp(top)
            }
        }
    }

    private fun weighted(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, -2, 1f).apply {
            marginEnd = dp(5)
        }
    }

    private fun enterRoom(defaultId: String? = null) {
        val input = EditText(this).apply {
            hint = "Enter Room ID"
            setTextColor(white)
            setHintTextColor(muted)

            if (defaultId != null) {
                setText(defaultId)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Join Voice Room")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Join") { _, _ ->

                val id = input.text.toString().trim()

                if (id.isBlank()) {
                    notice("Please enter Room ID")
                    return@setPositiveButton
                }

                startActivity(
                    Intent(this, RoomActivity::class.java)
                        .putExtra("roomID", id)
                )
            }
            .show()
    }

    private fun notice(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun dp(value: Int): Int {
        return (
            value * resources.displayMetrics.density
        ).toInt()
    }
}
