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
    private var selectedGender = ""

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

    companion object {
        private const val PHOTO_REQUEST = 501
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

        if (currentFeature != null) {
            showHome()
        } else {
            super.onBackPressed()
        }
    }

    private fun ensurePublicProfile() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

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
            .addOnFailureListener {
                notice("Public profile sync failed: ${it.localizedMessage}")
            }
    }

    private fun showHome() {
        chatListener?.remove()
        chatListener = null
        activeChatId = null
        currentFeature = null

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(24))
        }

        scroll.addView(page)

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val logo = text("♛", 30f, cyan, true).apply {
            gravity = Gravity.CENTER
        }

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
                intArrayOf(
                    Color.rgb(49, 39, 111),
                    Color.rgb(16, 100, 125)
                ),
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
                14f,
                white,
                false
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

        addRow(
            page,
            "🖼",
            "Profile & DP",
            "Name, photo and user details",
            purple
        )

        addRow(
            page,
            "🌍",
            "Country, Age & Gender",
            "Manage profile information",
            cyan
        )

        addRow(
            page,
            "🔐",
            "Update Password",
            "Account security",
            gold
        )

        addRow(
            page,
            "⚙",
            "Settings",
            "Privacy and preferences",
            purple
        )

        addRow(
            page,
            "❓",
            "Help & Customer Contact",
            "Support and assistance",
            cyan
        )

        addRow(
            page,
            "🚪",
            "Sign Out",
            "Sign out of this device",
            pink
        )

        addRow(
            page,
            "🗑",
            "Delete Account",
            "Request account deletion",
            pink
        )

        val footer = text("KINGDOM VOICE CHAT", 10f, muted, true)
        footer.gravity = Gravity.CENTER
        footer.setPadding(0, dp(24), 0, dp(4))
        page.addView(footer)

        setContentView(scroll)
    }

    private fun openRooms() {
        try {
            startActivity(Intent(this, RoomActivity::class.java))
        } catch (e: Exception) {
            notice("Voice rooms could not open: ${e.localizedMessage}")
        }
    }

    private fun openOwnerPanel() {
        try {
            startActivity(Intent(this, OwnerPanelActivity::class.java))
        } catch (e: Exception) {
            notice("Owner panel could not open: ${e.localizedMessage}")
        }
    }

    private fun showFeature(feature: String) {
        currentFeature = feature

        when (feature) {
            "Voice Rooms" -> openRooms()
            "Sub Rooms" -> openRooms()
            "My Profile", "Profile & DP",
            "Country, Age & Gender" -> showProfile()

            "Followers" -> showPeople(false)
            "Chat Messages" -> showPeople(true)

            "Update Password" -> showPasswordScreen()

            "Sign Out" -> confirmSignOut()
            "Delete Account" -> confirmDeleteAccount()

            else -> {
                val screen = page(feature)
                screen.addView(
                    infoCard(
                        feature,
                        "This feature is not connected yet."
                    ),
                    LinearLayout.LayoutParams(-1, -2).apply {
                        topMargin = dp(18)
                    }
                )
                setContentView(screen)
            }
        }
    }    private fun showProfile() {
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
            button("Change Profile Photo") {
                choosePhoto()
            },
            LinearLayout.LayoutParams(-1, dp(48)).apply {
                topMargin = dp(12)
            }
        )

        screen.addView(
            photoBox,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
            }
        )

        val user = auth.currentUser

        screen.addView(
            infoCard("USER ID", user?.uid ?: "Not signed in"),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(14)
            }
        )

        screen.addView(
            infoCard("LEVEL", "Not connected yet"),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(10)
            }
        )

        section(screen, "Personal Information", "EDIT DETAILS")

        profileName = inputField("Profile name")
        profileCountry = inputField("Country")
        profileAge = inputField("Age")

        profileGender = text("Select gender  ›", 15f, white, true).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), 0, dp(14), 0)
            background = fieldBackground()
            isClickable = true
            isFocusable = true
            setOnClickListener {
                chooseGender()
            }
        }

        addLabeledField(screen, "Name", profileName)
        addLabeledField(screen, "Country", profileCountry)
        addLabeledField(screen, "Age", profileAge)
        addLabeledView(screen, "Gender", profileGender)

        screen.addView(
            button("SAVE PROFILE") {
                saveProfile()
            },
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                topMargin = dp(20)
            }
        )

        setContentView(screen)
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                profileName.setText(doc.getString("name") ?: "")
                profileCountry.setText(doc.getString("country") ?: "")
                profileAge.setText(
                    doc.getLong("age")?.toString() ?: ""
                )

                selectedGender = doc.getString("gender") ?: ""

                profileGender.text =
                    if (selectedGender.isBlank()) {
                        "Select gender  ›"
                    } else {
                        "$selectedGender  ›"
                    }

                syncPublicProfile(
                    doc.getString("name") ?: "",
                    doc.getString("country") ?: ""
                )
            }
            .addOnFailureListener {
                notice("Profile load failed: ${it.localizedMessage}")
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return

        val name = profileName.text.toString().trim()
        val country = profileCountry.text.toString().trim()
        val ageText = profileAge.text.toString().trim()
        val age = ageText.toLongOrNull()

        if (name.isBlank()) {
            notice("Please enter your name")
            return
        }

        if (ageText.isNotBlank() && age == null) {
            notice("Please enter a valid age")
            return
        }

        val data = hashMapOf<String, Any>(
            "name" to name,
            "country" to country,
            "gender" to selectedGender,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        if (age != null) {
            data["age"] = age
        }

        db.collection("users").document(uid)
            .set(data)
            .addOnSuccessListener {
                syncPublicProfile(name, country)
                notice("Profile saved successfully")
            }
            .addOnFailureListener {
                notice("Save failed: ${it.localizedMessage}")
            }
    }

    private fun chooseGender() {
        val options = arrayOf("Male", "Female", "Other")

        AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(options) { _, which ->
                selectedGender = options[which]
                profileGender.text = "${options[which]}  ›"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }

        startActivityForResult(intent, PHOTO_REQUEST)
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PHOTO_REQUEST &&
            resultCode == Activity.RESULT_OK
        ) {
            val uri = data?.data ?: return

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            photoPrefs.edit()
                .putString("photoUri", uri.toString())
                .apply()

            selectedPhoto = uri

            if (::profileImage.isInitialized) {
                showPhoto(uri)
            }
        }
    }

    private fun loadLocalPhoto(): Uri? {
        val value = photoPrefs.getString("photoUri", null)
        return value?.let { Uri.parse(it) }
    }

    private fun showPhoto(uri: Uri) {
        try {
            if (::profileImage.isInitialized) {
                profileImage.setImageURI(uri)
            }
        } catch (_: Exception) {
            notice("Could not load photo")
        }
    }

    private fun showPeople(chatOnly: Boolean) {
        currentFeature = if (chatOnly) "Chat Messages" else "Followers"

        val screen = page(
            if (chatOnly) "Chat Contacts" else "Discover Users"
        )

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        screen.addView(
            list,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(12)
            }
        )

        setContentView(screen)

        val myUid = auth.currentUser?.uid ?: return

        db.collection("publicProfiles")
            .get()
            .addOnSuccessListener { result ->

                list.removeAllViews()

                for (doc in result.documents) {
                    val targetId = doc.id

                    if (targetId == myUid) continue

                    val name = doc.getString("name") ?: "User"
                    val country = doc.getString("country") ?: ""

                    db.collection("follows")
                        .document(myUid)
                        .collection("following")
                        .document(targetId)
                        .get()
                        .addOnSuccessListener { myFollow ->

                            db.collection("follows")
                                .document(targetId)
                                .collection("following")
                                .document(myUid)
                                .get()
                                .addOnSuccessListener { theirFollow ->

                                    val following = myFollow.exists()
                                    val mutual =
                                        following && theirFollow.exists()

                                    if (chatOnly && !mutual) {
                                        return@addOnSuccessListener
                                    }

                                    val userCard =
                                        LinearLayout(this).apply {
                                            orientation =
                                                LinearLayout.VERTICAL
                                            setPadding(
                                                dp(14),
                                                dp(14),
                                                dp(14),
                                                dp(14)
                                            )
                                            background = cardBackground()
                                        }

                                    userCard.addView(
                                        text(name, 17f, white, true)
                                    )

                                    if (country.isNotBlank()) {
                                        userCard.addView(
                                            text(country, 13f, muted, false)
                                        )
                                    }

                                    val actions =
                                        LinearLayout(this).apply {
                                            orientation =
                                                LinearLayout.HORIZONTAL
                                            gravity =
                                                Gravity.CENTER_VERTICAL
                                        }

                                    if (!chatOnly) {
                                        actions.addView(
                                            button(
                                                if (following) {
                                                    "UNFOLLOW"
                                                } else {
                                                    "FOLLOW"
                                                }
                                            ) {
                                                changeFollow(
                                                    targetId,
                                                    following
                                                ) {
                                                    showPeople(false)
                                                }
                                            },
                                            LinearLayout.LayoutParams(
                                                0,
                                                dp(44),
                                                1f
                                            )
                                        )
                                    }

                                    if (mutual) {
                                        actions.addView(
                                            button("MESSAGE") {
                                                openChat(targetId, name)
                                            },
                                            LinearLayout.LayoutParams(
                                                0,
                                                dp(44),
                                                1f
                                            ).apply {
                                                if (!chatOnly) {
                                                    leftMargin = dp(8)
                                                }
                                            }
                                        )
                                    }

                                    userCard.addView(
                                        actions,
                                        LinearLayout.LayoutParams(-1, -2)
                                            .apply {
                                                topMargin = dp(10)
                                            }
                                    )

                                    list.addView(
                                        userCard,
                                        LinearLayout.LayoutParams(-1, -2)
                                            .apply {
                                                bottomMargin = dp(10)
                                            }
                                    )
                                }
                        }
                }

                if (result.isEmpty) {
                    list.addView(
                        text("No users found yet.", 14f, muted, false)
                    )
                }
            }
            .addOnFailureListener {
                notice("Could not load users: ${it.localizedMessage}")
            }
    }

    private fun changeFollow(
        targetId: String,
        currentlyFollowing: Boolean,
        done: () -> Unit
    ) {
        val myUid = auth.currentUser?.uid ?: return

        val ref = db.collection("follows")
            .document(myUid)
            .collection("following")
            .document(targetId)

        if (currentlyFollowing) {
            ref.delete()
                .addOnSuccessListener {
                    done()
                }
                .addOnFailureListener {
                    notice("Unfollow failed: ${it.localizedMessage}")
                }
        } else {
            ref.set(
                mapOf(
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).addOnSuccessListener {
                done()
            }.addOnFailureListener {
                notice("Follow failed: ${it.localizedMessage}")
            }
        }
    }    private fun openChat(otherUid: String, otherName: String) {
        val myUid = auth.currentUser?.uid ?: return
        if (myUid == otherUid) return

        val members = listOf(myUid, otherUid).sorted()
        val chatId = "${members[0]}_${members[1]}"

        val myFollow = db.collection("follows")
            .document(myUid)
            .collection("following")
            .document(otherUid)

        val theirFollow = db.collection("follows")
            .document(otherUid)
            .collection("following")
            .document(myUid)

        myFollow.get().addOnSuccessListener firstCheck@{ first ->
            if (!first.exists()) {
                notice("You both must follow each other to chat")
                return@firstCheck
            }

            theirFollow.get().addOnSuccessListener secondCheck@{ second ->
                if (!second.exists()) {
                    notice("You both must follow each other to chat")
                    return@secondCheck
                }

                db.collection("chats").document(chatId)
                    .get()
                    .addOnSuccessListener chatCheck@{ existing ->
                        if (existing.exists()) {
                            showChatScreen(chatId, otherName)
                        } else {
                            val data = mapOf(
                                "memberIds" to members,
                                "createdAt" to FieldValue.serverTimestamp()
                            )

                            db.collection("chats").document(chatId)
                                .set(data)
                                .addOnSuccessListener {
                                    showChatScreen(chatId, otherName)
                                }
                                .addOnFailureListener {
                                    notice(
                                        "Could not start chat: ${it.localizedMessage}"
                                    )
                                }
                        }
                    }
                    .addOnFailureListener {
                        notice("Chat access failed: ${it.localizedMessage}")
                    }
            }.addOnFailureListener {
                notice("Follow check failed: ${it.localizedMessage}")
            }
        }.addOnFailureListener {
            notice("Follow check failed: ${it.localizedMessage}")
        }
    }

    private fun showChatScreen(chatId: String, otherName: String) {
        currentFeature = "Chat Messages"
        activeChatId = chatId
        activeChatOtherName = otherName

        chatListener?.remove()
        chatListener = null

        val screen = page(otherName)

        val messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(8), dp(6), dp(8))
        }

        chatMessagesBox = messages

        val scroll = ScrollView(this).apply {
            addView(messages)
            isFillViewport = true
        }

        chatScroll = scroll

        screen.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f).apply {
                topMargin = dp(10)
            }
        )

        val input = EditText(this).apply {
            hint = "Write a message"
            setTextColor(white)
            setHintTextColor(muted)
            background = fieldBackground()
            setPadding(dp(12), dp(10), dp(12), dp(10))
            minLines = 1
            maxLines = 4
        }

        val send = button("SEND") {
            sendMessage(chatId, input.text.toString(), input)
        }

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        composer.addView(
            input,
            LinearLayout.LayoutParams(0, dp(52), 1f)
        )

        composer.addView(
            send,
            LinearLayout.LayoutParams(dp(90), dp(52)).apply {
                leftMargin = dp(8)
            }
        )

        screen.addView(
            composer,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(10)
            }
        )

        setContentView(screen)
        listenForMessages(chatId)
    }

    private fun sendMessage(
        chatId: String,
        value: String,
        input: EditText
    ) {
        val message = value.trim()

        if (message.isBlank()) return

        if (message.length > 2000) {
            notice("Message is too long")
            return
        }

        val uid = auth.currentUser?.uid ?: return

        val data = mapOf(
            "senderId" to uid,
            "text" to message,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(data)
            .addOnSuccessListener {
                input.text.clear()
            }
            .addOnFailureListener {
                notice("Message failed: ${it.localizedMessage}")
            }
    }

    private fun listenForMessages(chatId: String) {
        val messagesView = chatMessagesBox ?: return

        chatListener?.remove()

        chatListener = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    notice("Chat update failed: ${error.localizedMessage}")
                    return@addSnapshotListener
                }

                messagesView.removeAllViews()

                snapshot?.documents?.forEach { doc ->
                    val sender = doc.getString("senderId") ?: ""
                    val body = doc.getString("text") ?: ""
                    val mine = sender == auth.currentUser?.uid

                    val bubble = TextView(this).apply {
                        text = body
                        textSize = 15f
                        setTextColor(white)
                        setPadding(dp(12), dp(10), dp(12), dp(10))
                        background = messageBackground(mine)
                        maxWidth = (
                            resources.displayMetrics.widthPixels * 0.78f
                        ).toInt()
                    }

                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = if (mine) Gravity.END else Gravity.START
                        setPadding(dp(6), dp(4), dp(6), dp(4))
                        addView(bubble)
                    }

                    messagesView.addView(
                        row,
                        LinearLayout.LayoutParams(-1, -2)
                    )
                }

                chatScroll?.post {
                    chatScroll?.fullScroll(View.FOCUS_DOWN)
                }
            }
    }

    private fun showPasswordScreen() {
        currentFeature = "Update Password"

        val screen = page("Update Password")

        val password = EditText(this).apply {
            hint = "New password"
            setTextColor(white)
            setHintTextColor(muted)
            inputType = 129
            background = fieldBackground()
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }

        screen.addView(
            text("Enter a new password for your account.", 14f, muted, false)
        )

        screen.addView(
            password,
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                topMargin = dp(18)
            }
        )

        screen.addView(
            button("UPDATE PASSWORD") {
                val value = password.text.toString()

                if (value.length < 6) {
                    notice("Password must be at least 6 characters")
                    return@button
                }

                val user = auth.currentUser

                if (user == null) {
                    notice("Please sign in again")
                    return@button
                }

                user.updatePassword(value)
                    .addOnSuccessListener {
                        password.text.clear()
                        notice("Password updated successfully")
                    }
                    .addOnFailureListener {
                        notice(
                            "Password update failed. Sign in again and retry."
                        )
                    }
            },
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                topMargin = dp(16)
            }
        )

        setContentView(screen)
    }

    private fun confirmSignOut() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Sign Out") { _, _ ->
                chatListener?.remove()
                chatListener = null
                auth.signOut()
                notice("Signed out")

                finish()
            }
            .show()
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(this)
            .setTitle("Account Deletion")
            .setMessage(
                "This will delete your sign-in account. " +
                    "Your Firestore profile and other data may still remain. " +
                    "Continue only if you understand this."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ ->
                val user = auth.currentUser

                if (user == null) {
                    notice("No signed-in account")
                    return@setPositiveButton
                }

                user.delete()
                    .addOnSuccessListener {
                        chatListener?.remove()
                        chatListener = null
                        notice("Account deleted")
                        finish()
                    }
                    .addOnFailureListener {
                        notice(
                            "Deletion failed. Sign in again and retry."
                        )
                    }
            }
            .show()
    }

    private fun page(title: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(22))
            setBackgroundColor(bg)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = text("‹", 34f, cyan, true)
        back.setOnClickListener {
            onBackPressed()
        }

        top.addView(back, LinearLayout.LayoutParams(dp(42), dp(48)))

        top.addView(
            text(title, 21f, white, true),
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        root.addView(top)

        return root
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

            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun tile(
        emoji: String,
        title: String,
        accent: Int,
        action: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = cardBackground()
            isClickable = true
            isFocusable = true

            addView(
                text(emoji, 28f, accent, true),
                LinearLayout.LayoutParams(dp(48), -2)
            )

            addView(
                text(title, 16f, white, true),
                LinearLayout.LayoutParams(0, -2, 1f)
            )

            setOnClickListener {
                action()
            }
        }
    }

    private fun section(
        parent: LinearLayout,
        title: String,
        subtitle: String
    ) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(22), 0, dp(10))
        }

        box.addView(text(subtitle, 10f, cyan, true))
        box.addView(text(title, 19f, white, true))

        parent.addView(box)
    }

    private fun addGrid(
        parent: LinearLayout,
        icon1: String,
        title1: String,
        icon2: String,
        title2: String
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            tile(icon1, title1, cyan) {
                showFeature(title1)
            },
            LinearLayout.LayoutParams(0, dp(78), 1f).apply {
                rightMargin = dp(6)
            }
        )

        row.addView(
            tile(icon2, title2, purple) {
                showFeature(title2)
            },
            LinearLayout.LayoutParams(0, dp(78), 1f).apply {
                leftMargin = dp(6)
            }
        )

        parent.addView(
            row,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(10)
            }
        )
    }

    private fun addRow(
        parent: LinearLayout,
        icon: String,
        title: String,
        description: String,
        accent: Int
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = cardBackground()
            isClickable = true
            isFocusable = true
        }

        row.addView(
            text(icon, 24f, accent, true),
            LinearLayout.LayoutParams(dp(44), -2)
        )

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(text(title, 15f, white, true))
            addView(text(description, 12f, muted, false))
        }

        row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))

        row.setOnClickListener {
            showFeature(title)
        }

        parent.addView(
            row,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(9)
            }
        )
    }

    private fun button(
        label: String,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(bg)
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            background = GradientDrawable().apply {
                setColor(cyan)
                cornerRadius = dp(13).toFloat()
            }
            isClickable = true
            isFocusable = true

            setOnClickListener {
                action()
            }
        }
    }

    private fun inputField(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 15f
            setTextColor(white)
            setHintTextColor(muted)
            background = fieldBackground()
            setPadding(dp(14), dp(10), dp(14), dp(10))
            singleLine = true
        }
    }

    private fun addLabeledField(
        parent: LinearLayout,
        label: String,
        field: EditText
    ) {
        addLabeledView(parent, label, field)
    }

    private fun addLabeledView(
        parent: LinearLayout,
        label: String,
        view: View
    ) {
        parent.addView(
            text(label, 13f, muted, true),
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(12)
                bottomMargin = dp(6)
            }
        )

        parent.addView(
            view,
            LinearLayout.LayoutParams(-1, dp(50))
        )
    }

    private fun infoCard(
        label: String,
        value: String
    ): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = cardBackground()
        }

        box.addView(text(label, 10f, cyan, true))
        box.addView(text(value, 13f, white, false))

        return box
    }

    private fun fieldBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(card)
            cornerRadius = dp(12).toFloat()
            setStroke(dp(1), Color.rgb(60, 68, 110))
        }
    }

    private fun cardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(card)
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), Color.rgb(50, 57, 98))
        }
    }

    private fun circleBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(card)
            setStroke(dp(2), cyan)
        }
    }

    private fun gradient(
        colors: IntArray,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            colors
        ).apply {
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun messageBackground(mine: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            setColor(
                if (mine) {
                    Color.rgb(52, 93, 137)
                } else {
                    Color.rgb(48, 52, 83)
                }
            )
            cornerRadius = dp(16).toFloat()
        }
    }

    private fun notice(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
