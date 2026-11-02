package com.kingdomvoicechat

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MainActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val bg = Color.rgb(10, 15, 35)
    private val card = Color.rgb(25, 34, 65)
    private val white = Color.WHITE
    private val muted = Color.rgb(175, 188, 215)
    private val cyan = Color.rgb(55, 220, 240)
    private val purple = Color.rgb(135, 95, 255)
    private val gold = Color.rgb(255, 190, 80)

    private var currentTab = "Home"
    private var currentFeature: String? = null

    private var selectedGender = "Not specified"

    private lateinit var profileName: EditText
    private lateinit var profileCountry: EditText
    private lateinit var profileAge: EditText
    private lateinit var profileImage: ImageView

    private val prefs by lazy {
        getSharedPreferences("kvc_profile", MODE_PRIVATE)
    }

    companion object {
        private const val PHOTO_REQUEST = 501
        private const val FIRST_NUMERIC_ID = 1000001L
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

    override fun onDestroy() {
        super.onDestroy()
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

    // ------------------------------------------------
    // USER PROFILE CREATION
    // ------------------------------------------------

    private fun ensurePublicProfile() {
        val user = auth.currentUser ?: return

        val userRef = db.collection("users").document(user.uid)
        val publicRef = db.collection("publicProfiles").document(user.uid)
        val counterRef = db.collection("appCounters")
            .document("userNumericId")

        db.runTransaction { transaction ->

            val userDoc = transaction.get(userRef)
            val publicDoc = transaction.get(publicRef)

            val oldId = userDoc.getLong("numericId")
                ?: publicDoc.getLong("numericId")

            val numericId = if (oldId != null) {
                oldId
            } else {
                val counter = transaction.get(counterRef)
                val lastId = counter.getLong("lastId")
                    ?: FIRST_NUMERIC_ID - 1

                val newId = lastId + 1

                transaction.set(
                    counterRef,
                    mapOf("lastId" to newId),
                    SetOptions.merge()
                )

                newId
            }

            val name = userDoc.getString("name")
                ?: user.email?.substringBefore("@")
                ?: "Kingdom User"

            val country = userDoc.getString("country") ?: ""

            val data = mapOf(
                "numericId" to numericId,
                "name" to name,
                "country" to country,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            transaction.set(userRef, data, SetOptions.merge())
            transaction.set(publicRef, data, SetOptions.merge())

            numericId
        }.addOnFailureListener {
            notice("Profile setup failed: ${it.localizedMessage}")
        }
    }

    private fun syncPublicProfile(name: String, country: String) {
        val user = auth.currentUser ?: return

        val userRef = db.collection("users").document(user.uid)

        userRef.get().addOnSuccessListener { document ->

            val numericId = document.getLong("numericId")

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
                .set(data, SetOptions.merge())
        }
    }

    // ------------------------------------------------
    // NAVIGATION
    // ------------------------------------------------

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
            setPadding(dp(5), dp(8), dp(5), dp(8))
            setBackgroundColor(Color.rgb(17, 23, 48))
        }

        val items = listOf(
            "⌂" to "Home",
            "🎙" to "Rooms",
            "✉" to "Messages",
            "♙" to "Profile"
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
                    22f,
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

    // ------------------------------------------------
    // HOME
    // ------------------------------------------------

    private fun showHome() {

        val (root, page) = createTabLayout()

        page.addView(
            text("♛ KINGDOM VOICE CHAT", 23f, white, true)
        )

        page.addView(
            text("VOICE • CHAT • CONNECT", 11f, cyan, true)
        )

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(22))
            background = gradient(
                intArrayOf(
                    Color.rgb(48, 43, 120),
                    Color.rgb(15, 105, 130)
                )
            )
        }

        hero.addView(
            text("YOUR WORLD. YOUR VOICE.", 12f, cyan, true)
        )

        hero.addView(
            text(
                "Find your people.",
                28f,
                white,
                true
            ).apply {
                setPadding(0, dp(12), 0, dp(8))
            }
        )

        hero.addView(
            text(
                "Join rooms, meet friends and share your voice.",
                14f,
                white,
                false
            )
        )

        hero.addView(
            button("Explore Voice Rooms") {
                showTab("Rooms")
            },
            LinearLayout.LayoutParams(-1, dp(50)).apply {
                topMargin = dp(18)
            }
        )

        page.addView(
            hero,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(22)
            }
        )

        section(page, "Discover")

        addRow(
            page,
            "👑",
            "Official Rooms",
            "Explore official voice rooms"
        ) {
            showTab("Rooms")
        }

        addRow(
            page,
            "🎙",
            "Normal Rooms",
            "Discover public voice rooms"
        ) {
            showTab("Rooms")
        }

        addRow(
            page,
            "💬",
            "Messages",
            "Connect with other users"
        ) {
            showTab("Messages")
        }

        addRow(
            page,
            "💎",
            "My Wallet",
            "View Coins and Pearls"
        ) {
            showWallet()
        }

        setContentView(root)
    }

    // ------------------------------------------------
    // ROOMS
    // ------------------------------------------------

    private fun showRooms() {

        val (root, page) = createTabLayout()

        page.addView(
            text("Explore Rooms", 25f, white, true)
        )

        page.addView(
            text("Join a voice room using its Room ID.", 13f, muted)
        )

        section(page, "Room Categories")

        addRow(
            page,
            "👑",
            "Official Rooms",
            "Kingdom official rooms"
        ) {
            enterRoom()
        }

        addRow(
            page,
            "🎙",
            "Public Rooms",
            "Join a public room"
        ) {
            enterRoom()
        }

        page.addView(
            button("Enter Room ID") {
                enterRoom()
            },
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                topMargin = dp(18)
            }
        )

        setContentView(root)
    }

    private fun enterRoom() {

        val input = EditText(this).apply {
            hint = "Enter Room ID"
            setTextColor(white)
            setHintTextColor(muted)
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        AlertDialog.Builder(this)
            .setTitle("Join Voice Room")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Join") { _, _ ->

                val roomId = input.text.toString().trim()

                if (roomId.isBlank()) {
                    notice("Please enter Room ID")
                    return@setPositiveButton
                }

                val intent = Intent(this, RoomActivity::class.java)
                intent.putExtra("roomID", roomId)
                startActivity(intent)
            }
            .show()
    }

    // ------------------------------------------------
    // MESSAGES
    // ------------------------------------------------

    private fun showMessages() {

        val (root, page) = createTabLayout()

        page.addView(
            text("Messages", 25f, white, true)
        )

        page.addView(
            text(
                "Connect with Kingdom users.",
                13f,
                muted
            )
        )

        section(page, "Communication")

        addRow(
            page,
            "👥",
            "People",
            "Find registered users"
        ) {
            showPeople()
        }

        addRow(
            page,
            "💬",
            "Chats",
            "Your conversations"
        ) {
            showSimpleScreen(
                "Chats",
                "Chat conversations will appear here when the messaging backend is connected."
            )
        }

        addRow(
            page,
            "🎙",
            "Voice Rooms",
            "Talk with people"
        ) {
            showTab("Rooms")
        }

        setContentView(root)
    }

    private fun showPeople() {

        val page = page("People")

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        page.addView(list)

        setContentView(page)

        db.collection("publicProfiles")
            .limit(50)
            .get()
            .addOnSuccessListener { result ->

                list.removeAllViews()

                if (result.isEmpty) {
                    list.addView(
                        text("No users found.", 14f, muted)
                    )
                    return@addOnSuccessListener
                }

                for (document in result.documents) {

                    val name = document.getString("name")
                        ?: "Kingdom User"

                    val country = document.getString("country") ?: ""
                    val uid = document.id

                    addRow(
                        list,
                        "👤",
                        name,
                        "$country  •  ID: ${document.getLong("numericId") ?: "-"}"
                    ) {
                        showUserDetails(uid, name, country)
                    }
                }
            }
            .addOnFailureListener {
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

    // ------------------------------------------------
    // PROFILE MENU
    // ------------------------------------------------

    private fun showProfileMenu() {

        val (root, page) = createTabLayout()

        val user = auth.currentUser

        page.addView(
            text("My Profile", 25f, white, true)
        )

        val profileCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(22), dp(20), dp(22))
            background = cardBackground()
        }

        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(90), dp(90))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = circleBackground()
            clipToOutline = true
        }

        loadProfileImage(image)

        profileCard.addView(image)

        profileCard.addView(
            text(
                user?.email ?: "Kingdom User",
                18f,
                white,
                true
            ).apply {
                setPadding(0, dp(12), 0, dp(5))
            }
        )

        profileCard.addView(
            text(
                "UID: ${user?.uid ?: "-"}",
                11f,
                muted
            )
        )

        page.addView(
            profileCard,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
            }
        )

        section(page, "Account")

        addRow(page, "✏", "Edit Profile", "Update your profile") {
            showProfile()
        }

        addRow(page, "💎", "Wallet", "Coins and Pearls") {
            showWallet()
        }

        addRow(page, "🎁", "Gift Store", "View available gifts") {
            showGiftStore()
        }

        addRow(page, "⚙", "Settings", "Account settings") {
            showAccountSettings()
        }

        addRow(page, "🚪", "Sign Out", "Sign out of your account") {
            confirmSignOut()
        }

        setContentView(root)
    }

    // ------------------------------------------------
    // PROFILE EDITING
    // ------------------------------------------------

    private fun showProfile() {

        currentFeature = "Edit Profile"

        val page = page("Edit Profile")

        profileImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(110), dp(110))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = circleBackground()
            clipToOutline = true
        }

        loadProfileImage(profileImage)

        page.addView(
            profileImage,
            LinearLayout.LayoutParams(dp(110), dp(110)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )

        page.addView(
            button("Change Profile Photo") {
                choosePhoto()
            }
        )

        profileName = inputField("Your Name")
        profileCountry = inputField("Country")
        profileAge = inputField("Age")

        page.addView(label("Name"))
        page.addView(profileName)

        page.addView(label("Country"))
        page.addView(profileCountry)

        page.addView(label("Age"))
        page.addView(profileAge)

        page.addView(label("Gender"))

        val genderButton = button("Select Gender: $selectedGender") {
            AlertDialog.Builder(this)
                .setTitle("Select Gender")
                .setItems(arrayOf("Male", "Female", "Other", "Not specified")) { _, which ->
                    selectedGender = arrayOf(
                        "Male",
                        "Female",
                        "Other",
                        "Not specified"
                    )[which]

                    genderButtonTextUpdate()
                }
                .show()
        }

        page.addView(genderButton)

        val saveButton = button("Save Profile") {

            val name = profileName.text.toString().trim()
            val country = profileCountry.text.toString().trim()
            val age = profileAge.text.toString().trim()

            if (name.isBlank()) {
                notice("Please enter your name")
                return@button
            }

            val user = auth.currentUser ?: return@button

            val data = hashMapOf<String, Any>(
                "name" to name,
                "country" to country,
                "gender" to selectedGender,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (age.isNotBlank()) {
                data["age"] = age
            }

            db.collection("users")
                .document(user.uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {

                    syncPublicProfile(name, country)

                    notice("Profile saved successfully")
                    currentFeature = null
                    showProfileMenu()
                }
                .addOnFailureListener {
                    notice(it.localizedMessage ?: "Profile save failed")
                }
        }

        page.addView(
            saveButton,
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                topMargin = dp(20)
            }
        )

        setContentView(page)

        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->

                    profileName.setText(document.getString("name") ?: "")
                    profileCountry.setText(document.getString("country") ?: "")
                    profileAge.setText(
                        document.get("age")?.toString() ?: ""
                    )

                    selectedGender =
                        document.getString("gender") ?: "Not specified"

                    genderButton.text = "Select Gender: $selectedGender"
                }
        }

        genderButton.tag = "gender"
    }

    private fun genderButtonTextUpdate() {
        val root = findViewById<LinearLayout>(android.R.id.content)
        val button = findTextView(root, "Select Gender:")
        button?.text = "Select Gender: $selectedGender"
    }

    private fun findTextView(view: View, startsWith: String): TextView? {
        if (view is TextView && view.text.toString().startsWith(startsWith)) {
            return view
        }

        if (view is LinearLayout) {
            for (i in 0 until view.childCount) {
                val found = findTextView(view.getChildAt(i), startsWith)
                if (found != null) return found
            }
        }

        return null
    }

    // ------------------------------------------------
    // PHOTO PICKER
    // ------------------------------------------------

    private fun choosePhoto() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
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

    // ------------------------------------------------
    // WALLET API
    // ------------------------------------------------

    private fun showWallet() {

        currentFeature = "Wallet"

        val page = page("My Wallet")

        val balanceText = text(
            "Loading wallet...",
            20f,
            cyan,
            true
        )

        page.addView(
            balanceText,
            LinearLayout.LayoutParams(-1, dp(90))
        )

        page.addView(
            button("Refresh Balance") {
                loadWallet(balanceText)
            }
        )

        page.addView(
            button("Open Gift Store") {
                showGiftStore()
            },
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                topMargin = dp(15)
            }
        )

        page.addView(
            text(
                "Wallet balance is provided by the Kingdom Wallet API.",
                12f,
                muted
            ).apply {
                setPadding(0, dp(15), 0, 0)
            }
        )

        setContentView(page)

        loadWallet(balanceText)
    }

    private fun loadWallet(target: TextView) {

        target.text = "Loading wallet..."

        WalletApi.getWallet(object : WalletApi.Callback<WalletBalance> {

            override fun onSuccess(value: WalletBalance) {
                runOnUiThread {
                    target.text =
                        "🪙 Coins: ${value.coins}\n💎 Pearls: ${value.pearls}"
                }
            }

            override fun onError(message: String) {
                runOnUiThread {
                    target.text = "Wallet unavailable"
                    notice(message)
                }
            }
        })
    }

    // ------------------------------------------------
    // GIFT STORE
    // ------------------------------------------------

    private fun showGiftStore() {

        currentFeature = "Gift Store"

        val page = page("Gift Store")

        page.addView(
            text(
                "Available gifts",
                14f,
                muted
            )
        )

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        page.addView(list)

        setContentView(page)

        WalletApi.getGifts(object : WalletApi.Callback<List<GiftItem>> {

            override fun onSuccess(value: List<GiftItem>) {

                runOnUiThread {

                    list.removeAllViews()

                    if (value.isEmpty()) {
                        list.addView(
                            text("No gifts available.", 14f, muted)
                        )
                        return@runOnUiThread
                    }

                    value.forEach { gift ->

                        addRow(
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
                    notice(message)
                    list.addView(
                        text("Unable to load gifts.", 14f, muted)
                    )
                }
            }
        })
    }

    private fun askGiftReceiver(gift: GiftItem) {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
        }

        val receiverInput = EditText(this).apply {
            hint = "Receiver Firebase UID"
            setTextColor(white)
            setHintTextColor(muted)
        }

        val roomInput = EditText(this).apply {
            hint = "Room ID"
            setTextColor(white)
            setHintTextColor(muted)
        }

        box.addView(receiverInput)
        box.addView(roomInput)

        AlertDialog.Builder(this)
            .setTitle("Send ${gift.name}")
            .setMessage("Gift price: ${gift.price} Coins")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Send Gift") { _, _ ->

                val receiver = receiverInput.text.toString().trim()
                val roomId = roomInput.text.toString().trim()

                if (receiver.isBlank() || roomId.isBlank()) {
                    notice("Enter receiver UID and Room ID")
                    return@setPositiveButton
                }

                WalletApi.sendGift(
                    receiver,
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

    // ------------------------------------------------
    // SETTINGS
    // ------------------------------------------------

    private fun showAccountSettings() {

        val page = page("Settings")

        addRow(
            page,
            "🎨",
            "Theme Settings",
            "Change app appearance"
        ) {
            startActivity(
                Intent(this, ThemeSettingsActivity::class.java)
            )
        }

        addRow(
            page,
            "👑",
            "Owner Panel",
            "Open owner controls"
        ) {
            startActivity(
                Intent(this, OwnerPanelActivity::class.java)
            )
        }

        addRow(
            page,
            "🚪",
            "Sign Out",
            "Sign out of your account"
        ) {
            confirmSignOut()
        }

        setContentView(page)
    }

    private fun confirmSignOut() {

        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Sign Out") { _, _ ->

                auth.signOut()

                val intent = Intent(this, AuthActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
            .show()
    }

    // ------------------------------------------------
    // BASIC SCREEN HELPERS
    // ------------------------------------------------

    private fun page(title: String): LinearLayout {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(bg)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = text("‹", 32f, cyan, true)
        back.setOnClickListener {
            currentFeature = null
            showTab(currentTab)
        }

        header.addView(back)

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

    private fun section(parent: LinearLayout, title: String) {
        parent.addView(
            text(title, 19f, white, true).apply {
                setPadding(0, dp(22), 0, dp(10))
            }
        )
    }

    private fun addRow(
        parent: LinearLayout,
        icon: String,
        title: String,
        subtitle: String,
        action: () -> Unit
    ) {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = cardBackground()
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        row.addView(
            text(icon, 25f, cyan, true)
        )

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        labels.addView(
            text(title, 15f, white, true)
        )

        labels.addView(
            text(subtitle, 11f, muted)
        )

        row.addView(
            labels,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(text("›", 24f, muted))

        parent.addView(
            row,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(10)
            }
        )
    }

    private fun label(value: String): TextView {
        return text(value, 13f, cyan, true).apply {
            setPadding(0, dp(16), 0, dp(7))
        }
    }

    private fun inputField(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            setTextColor(white)
            setHintTextColor(muted)
            textSize = 15f
            setPadding(dp(13), dp(12), dp(13), dp(12))
            background = cardBackground()
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

    private fun button(
        title: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = gradient(
                intArrayOf(
                    Color.rgb(45, 115, 220),
                    Color.rgb(120, 70, 220)
                )
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
    }

    private fun gradient(colors: IntArray): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            colors
        ).apply {
            cornerRadius = dp(20).toFloat()
        }
    }

    private fun cardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(card)
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), Color.rgb(48, 61, 100))
        }
    }

    private fun circleBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(card)
            setStroke(dp(2), cyan)
        }
    }

    private fun notice(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
