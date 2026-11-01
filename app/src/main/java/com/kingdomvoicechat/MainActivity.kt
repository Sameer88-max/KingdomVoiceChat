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
    private fun showProfile() {
        val screen = page("My Profile")
        val photoBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = cardBackground()
        }

        val photo = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(100), dp(100))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = circleBackground()
            clipToOutline = true
        }
        photoBox.addView(photo)
        loadLocalPhoto()?.let { showPhoto(it) }

        val changePhoto = button("Change Profile Photo") {
            choosePhoto()
        }
        photoBox.addView(changePhoto, LinearLayout.LayoutParams(-1, dp(48)).apply {
            topMargin = dp(12)
        })
        screen.addView(photoBox, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(18)
        })

        val user = auth.currentUser
        screen.addView(infoCard("USER ID", user?.uid ?: "Not signed in"),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        screen.addView(infoCard("LEVEL", "Not connected yet"),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })

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
            setOnClickListener { chooseGender() }
        }

        addLabeledField(screen, "Name", profileName)
        addLabeledField(screen, "Country", profileCountry)
        addLabeledField(screen, "Age", profileAge)
        addLabeledView(screen, "Gender", profileGender)

        screen.addView(button("SAVE PROFILE") { saveProfile() },
            LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(20) })

        setContentView(screen)
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener
                profileName.setText(doc.getString("name") ?: "")
                profileCountry.setText(doc.getString("country") ?: "")
                profileAge.setText(doc.getLong("age")?.toString() ?: "")
                selectedGender = doc.getString("gender") ?: ""
                profileGender.text =
                    if (selectedGender.isBlank()) "Select gender  ›"
                    else "$selectedGender  ›"
                val name = doc.getString("name") ?: ""
                val country = doc.getString("country") ?: ""
                syncPublicProfile(name, country)
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
        if (age != null) data["age"] = age

        db.collection("users").document(uid).set(data)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            getSharedPreferences("profile", MODE_PRIVATE)
                .edit()
                .putString("photoUri", uri.toString())
                .apply()
            showPhoto(uri)
        }
    }

    private fun loadLocalPhoto(): Uri? {
        val value = getSharedPreferences("profile", MODE_PRIVATE)
            .getString("photoUri", null)
        return value?.let { Uri.parse(it) }
    }

    private fun showPhoto(uri: Uri) {
        try {
            val bitmap = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return
            profilePhotoView?.setImageBitmap(bitmap)
        } catch (_: Exception) {
            notice("Could not load photo")
        }
    }

    private fun showPeople(chatOnly: Boolean) {
        val screen = page(if (chatOnly) "Chat Contacts" else "Discover Users")
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        screen.addView(list, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(12)
        })
        setContentView(screen)

        val myUid = auth.currentUser?.uid ?: return
        db.collection("publicProfiles").get()
            .addOnSuccessListener { result ->
                list.removeAllViews()
                for (doc in result.documents) {
                    val targetId = doc.id
                    if (targetId == myUid) continue

                    val name = doc.getString("name") ?: "User"
                    val country = doc.getString("country") ?: ""

                    db.collection("follows").document(myUid)
                        .collection("following").document(targetId).get()
                        .addOnSuccessListener { myFollow ->
                            db.collection("follows").document(targetId)
                                .collection("following").document(myUid).get()
                                .addOnSuccessListener { theirFollow ->
                                    val following = myFollow.exists()
                                    val mutual = following && theirFollow.exists()

                                    if (chatOnly && !mutual) return@addOnSuccessListener

                                    val card = LinearLayout(this).apply {
                                        orientation = LinearLayout.VERTICAL
                                        setPadding(dp(14), dp(14), dp(14), dp(14))
                                        background = cardBackground()
                                    }
                                    card.addView(text(name, 17f, white, true))
                                    if (country.isNotBlank()) {
                                        card.addView(text(country, 13f, muted, false))
                                    }

                                    val actions = LinearLayout(this).apply {
                                        orientation = LinearLayout.HORIZONTAL
                                        gravity = Gravity.CENTER_VERTICAL
                                    }

                                    if (!chatOnly) {
                                        actions.addView(button(
                                            if (following) "UNFOLLOW" else "FOLLOW"
                                        ) {
                                            changeFollow(targetId, following) {
                                                showPeople(false)
                                            }
                                        }, LinearLayout.LayoutParams(0, dp(44), 1f))
                                    }

                                    if (mutual) {
                                        actions.addView(button("MESSAGE") {
                                            openChat(targetId, name)
                                        }, LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                                            if (!chatOnly) leftMargin = dp(8)
                                        })
                                    }

                                    card.addView(actions,
                                        LinearLayout.LayoutParams(-1, -2).apply {
                                            topMargin = dp(10)
                                        })
                                    list.addView(card,
                                        LinearLayout.LayoutParams(-1, -2).apply {
                                            bottomMargin = dp(10)
                                        })
                                }
                        }
                }
                if (result.isEmpty) {
                    list.addView(text("No users found yet.", 14f, muted, false))
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
        val ref = db.collection("follows").document(myUid)
            .collection("following").document(targetId)

        if (currentlyFollowing) {
            ref.delete()
                .addOnSuccessListener { done() }
                .addOnFailureListener {
                    notice("Unfollow failed: ${it.localizedMessage}")
                }
        } else {
            ref.set(
                mapOf("createdAt" to FieldValue.serverTimestamp())
            ).addOnSuccessListener { done() }
                .addOnFailureListener {
                    notice("Follow failed: ${it.localizedMessage}")
                }
        }
    }

    private fun openChat(otherUid: String, otherName: String) {
        val myUid = auth.currentUser?.uid ?: return
        if (myUid == otherUid) return

        val members = listOf(myUid, otherUid).sorted()
        val chatId = "${members[0]}_${members[1]}"

        val myFollow = db.collection("follows").document(myUid)
            .collection("following").document(otherUid)
        val theirFollow = db.collection("follows").document(otherUid)
            .collection("following").document(myUid)

        myFollow.get().addOnSuccessListener { first ->
            if (!first.exists()) {
                notice("You both must follow each other to chat")
                return@addOnSuccessListener
            }
            theirFollow.get().addOnSuccessListener { second ->
                if (!second.exists()) {
                    notice("You both must follow each other to chat")
                    return@addOnSuccessListener
                }

                db.collection("chats").document(chatId).get()
                    .addOnSuccessListener { existing ->
                        if (existing.exists()) {
                            showChatScreen(chatId, otherName)
                        } else {
                            val data = mapOf(
                                "memberIds" to members,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                            db.collection("chats").document(chatId).set(data)
                                .addOnSuccessListener {
                                    showChatScreen(chatId, otherName)
                                }
                                .addOnFailureListener {
                                    notice("Could not start chat: ${it.localizedMessage}")
                                }
                        }
                    }
                    .addOnFailureListener {
                        notice("Chat access failed: ${it.localizedMessage}")
                    }
            }
        }.addOnFailureListener {
            notice("Follow check failed: ${it.localizedMessage}")
        }
    }

    private fun showChatScreen(chatId: String, otherName: String) {
        val screen = page(otherName)
        val messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val scroll = ScrollView(this).apply {
            addView(messages)
        }
        screen.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = dp(10)
        })

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
        composer.addView(input, LinearLayout.LayoutParams(0, dp(52), 1f))
        composer.addView(send, LinearLayout.LayoutParams(dp(90), dp(52)).apply {
            leftMargin = dp(8)
        })
        screen.addView(composer, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(10)
        })

        setContentView(screen)
        listenForMessages(chatId)
    }

    private fun sendMessage(chatId: String, value: String, input: EditText) {
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
        db.collection("chats").document(chatId)
            .collection("messages").add(data)
            .addOnSuccessListener {
                input.text.clear()
            }
            .addOnFailureListener {
                notice("Message failed: ${it.localizedMessage}")
            }
    }

    private fun listenForMessages(chatId: String) {
        val messagesView = currentMessagesView ?: return
        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("createdAt")
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
                        maxWidth = (resources.displayMetrics.widthPixels * 0.78f).toInt()
                    }
                    val row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = if (mine) Gravity.END else Gravity.START
                        setPadding(dp(6), dp(4), dp(6), dp(4))
                        addView(bubble)
                    }
                    messagesView.addView(row,
                        LinearLayout.LayoutParams(-1, -2))
                }
            }
    }
