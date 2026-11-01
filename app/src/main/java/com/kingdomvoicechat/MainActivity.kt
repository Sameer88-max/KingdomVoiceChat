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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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

        hero.addView(
            explore,
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

        val ownerPanel = tile("👑", "OWNER PANEL", gold) {
            openOwnerPanel()
        }

        page.addView(
            ownerPanel,
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
        currentFeature = "My Profile"

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
        }

        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(30))
        }

        scroll.addView(screen)

        screen.addView(
            tile("‹", "BACK TO HOME", cyan) { showHome() },
            LinearLayout.LayoutParams(-1, dp(60))
        )

        val title = text("MY PROFILE", 27f, white, true)
        title.setPadding(0, dp(24), 0, dp(6))
        screen.addView(title)

        screen.addView(text("YOUR KINGDOM IDENTITY", 11f, cyan, true))

        val photoBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = gradient(intArrayOf(card, bg), 20)
        }

        profileImage = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(45, 51, 90))
                setStroke(dp(2), cyan)
            }
            clipToOutline = true
            setImageResource(android.R.drawable.ic_menu_camera)
        }

        photoBox.addView(
            profileImage,
            LinearLayout.LayoutParams(dp(112), dp(112))
        )

        val photoButton = tile("📷", "CHOOSE PROFILE PHOTO", cyan) {
            choosePhoto()
        }

        photoBox.addView(
            photoButton,
            LinearLayout.LayoutParams(-1, dp(65)).apply {
                topMargin = dp(14)
            }
        )

        screen.addView(
            photoBox,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
            }
        )

        val user = auth.currentUser

        val idCard = infoCard("USER ID", user?.uid ?: "Not signed in")
        screen.addView(
            idCard,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(14)
            }
        )

        val levelCard = infoCard("LEVEL", "Not connected yet")
        screen.addView(
            levelCard,
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
            setOnClickListener { chooseGender() }
        }

        addLabeledField(screen, "PROFILE NAME", profileName)
        addLabeledField(screen, "COUNTRY", profileCountry)
        addLabeledField(screen, "AGE", profileAge)
        addLabeledView(screen, "GENDER", profileGender)

        val save = tile("✓", "SAVE PROFILE", cyan) {
            saveProfile()
        }

        screen.addView(
            save,
            LinearLayout.LayoutParams(-1, dp(65)).apply {
                topMargin = dp(20)
            }
        )

        profileStatus = text("", 12f, muted, false)
        profileStatus.setPadding(0, dp(12), 0, dp(4))
        screen.addView(profileStatus)

        section(screen, "Account Services", "MORE")

        addRow(screen, "💰", "Coins Balance",
            "Wallet integration pending", gold)
        addRow(screen, "💎", "Pearls Balance",
            "Secure rewards integration pending", cyan)
        addRow(screen, "👑", "VIP & Noble",
            "Membership information", gold)
        addRow(screen, "🎧", "Customer Service",
            "Help and support", purple)
        addRow(screen, "⚙", "Settings",
            "Privacy and preferences", cyan)
        addRow(screen, "📨", "Agency Apply",
            "Agency application information", purple)
        addRow(screen, "🎁", "Reward",
            "Reward system integration pending", pink)
        addRow(screen, "✉", "Invitation",
            "Invitation feature pending", cyan)

        setContentView(scroll)
        loadProfile()
        loadLocalPhoto()
    }

    private fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            notice("Please log in first.")
            return
        }

        profileStatus.text = "Loading profile..."

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    profileName.setText(
                        document.getString("name") ?: ""
                    )
                    profileCountry.setText(
                        document.getString("country") ?: ""
                    )
                    profileAge.setText(
                        document.getString("age") ?: ""
                    )

                    val gender = document.getString("gender") ?: ""
                    profileGender.text =
                        if (gender.isBlank()) "Select gender  ›"
                        else "$gender  ›"

                    profileStatus.text = "Profile loaded successfully."
                } else {
                    profileName.setText(
                        user.email?.substringBefore("@") ?: ""
                    )
                    profileStatus.text =
                        "Complete your details and press SAVE PROFILE."
                }
            }
            .addOnFailureListener { error ->
                profileStatus.text =
                    "Could not load profile: ${error.localizedMessage}"
            }
    }

    private fun saveProfile() {
        val user = auth.currentUser
        if (user == null) {
            notice("Please log in first.")
            return
        }

        val name = profileName.text.toString().trim()
        val country = profileCountry.text.toString().trim()
        val age = profileAge.text.toString().trim()
        val gender = profileGender.text.toString()
            .removeSuffix("  ›").trim()

        if (name.isBlank()) {
            notice("Profile name enter karein.")
            return
        }

        if (age.isNotBlank() && age.toIntOrNull() == null) {
            notice("Age mein sirf numbers enter karein.")
            return
        }

        if (age.isNotBlank() && age.toInt() !in 1..120) {
            notice("Age 1 se 120 ke darmiyan enter karein.")
            return
        }

        if (gender == "Select gender" || gender.isBlank()) {
            notice("Gender select karein.")
            return
        }

        profileStatus.text = "Saving profile..."

        val data = hashMapOf<String, Any>(
            "name" to name,
            "country" to country,
            "age" to age,
            "gender" to gender,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(user.uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                profileStatus.text = "Profile saved successfully."
                notice("Profile save ho gaya.")
            }
            .addOnFailureListener { error ->
                profileStatus.text =
                    "Save failed: ${error.localizedMessage}"
                notice("Profile save nahi ho saka.")
            }
    }

    private fun chooseGender() {
        val choices = arrayOf("Male", "Female", "Prefer not to say")

        AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(choices) { _, which ->
                profileGender.text = choices[which] + "  ›"
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }

        try {
            startActivityForResult(intent, 501)
        } catch (e: Exception) {
            notice("Photo picker open nahi ho saka.")
        }
    }

    @Deprecated("Legacy activity result API for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 501 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Some photo providers do not offer persistent access.
            }

            selectedPhoto = uri
            photoPrefs.edit().putString("photo_uri", uri.toString()).apply()
            showPhoto(uri)
            notice("Profile photo selected on this device.")
        }
    }

    private fun loadLocalPhoto() {
        val saved = photoPrefs.getString("photo_uri", null) ?: return
        try {
            showPhoto(Uri.parse(saved))
        } catch (_: Exception) {
            photoPrefs.edit().remove("photo_uri").apply()
        }
    }

    private fun showPhoto(uri: Uri) {
        try {
            profileImage.setImageURI(uri)
        } catch (_: Exception) {
            notice("Photo display nahi ho saki.")
        }
    }

    private fun infoCard(label: String, value: String): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = gradient(intArrayOf(card, bg), 15)
        }
        box.addView(text(label, 10f, cyan, true))
        box.addView(
            text(value, 12f, white, false).apply {
                setPadding(0, dp(6), 0, 0)
            }
        )
        return box
    }

    private fun inputField(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 15f
            singleLine = true
            setPadding(dp(14), dp(8), dp(14), dp(8))
            background = fieldBackground()
        }
    }

    private fun fieldBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.rgb(17, 21, 48))
            cornerRadius = dp(12).toFloat()
            setStroke(dp(1), Color.rgb(75, 83, 135))
        }
    }

    private fun addLabeledField(
        target: LinearLayout,
        label: String,
        field: EditText
    ) {
        addLabeledView(target, label, field)
    }

    private fun addLabeledView(
        target: LinearLayout,
        label: String,
        view: android.view.View
    ) {
        val group = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        group.addView(text(label, 10f, cyan, true))
        group.addView(
            view,
            LinearLayout.LayoutParams(-1, dp(54)).apply {
                topMargin = dp(7)
            }
        )

        target.addView(
            group,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(12)
            }
        )
    }

    private fun showFeature(name: String) {
        if (name == "My Profile" ||
            name == "Profile & DP" ||
            name == "Country, Age & Gender"
        ) {
            showProfile()
            return
        }

        currentFeature = name

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.isFillViewport = true

        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }

        scroll.addView(screen)

        screen.addView(
            tile("‹", "BACK TO HOME", cyan) { showHome() },
            LinearLayout.LayoutParams(-1, dp(62))
        )

        val title = text(name, 27f, white, true)
        title.setPadding(0, dp(28), 0, dp(8))
        screen.addView(title)
        screen.addView(text("KINGDOM VOICE CHAT", 12f, cyan, true))

        val description = when (name) {
            "Sub Rooms" -> "Explore and manage sub-room options."
            "Gifts" -> "Gift catalogue and secure gift sending will be connected here."
            "Chat Messages" -> "Private messages and friend chat will be connected here."
            "Wallet" -> "Coin balance, Pearls and transaction history will appear here."
            "Followers" -> "Your followers and following lists will appear here."
            "Levels" -> "Your activity level and level progress will appear here."
            "Update Password" -> "Use the secure password update process for your account."
            "Settings" -> "Privacy, notifications and app preferences."
            "Help & Customer Contact" -> "Customer support and complaint submission."
            "Notifications" -> "Your app notifications will appear here."
            "Delete Account" -> "Account deletion requires identity checks and confirmation."
            else -> "This section is ready for its next implementation phase."
        }

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = gradient(intArrayOf(card, bg), 20)
        }

        info.addView(text("FEATURE CENTRE", 12f, cyan, true))
        info.addView(
            text(description, 15f, white, false).apply {
                setPadding(0, dp(12), 0, dp(12))
            }
        )
        info.addView(
            text("Status: Backend integration pending.", 12f, gold, false)
        )

        screen.addView(
            info,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(22)
            }
        )

        if (name == "Delete Account") {
            screen.addView(
                tile("!", "ACCOUNT DELETION INFORMATION", pink) {
                    AlertDialog.Builder(this)
                        .setTitle("Delete account")
                        .setMessage(
                            "Account deletion is not enabled yet. No account has been deleted."
                        )
                        .setPositiveButton("OK", null)
                        .show()
                },
                LinearLayout.LayoutParams(-1, dp(65)).apply {
                    topMargin = dp(15)
                }
            )
        }

        setContentView(scroll)
    }

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
                        .setMessage("Room ID: $roomID\nShare this ID with others.")
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
                            RoomManager.joinRoom(this, input.text.toString())
                        }
                        .show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun openOwnerPanel() {
        try {
            startActivity(Intent(this, OwnerPanelActivity::class.java))
        } catch (_: Exception) {
            notice("Owner Panel screen could not be opened.")
        }
    }

    private fun openOption(name: String) {
        when (name) {
            "Voice Rooms" -> openRooms()
            "Owner Panel" -> openOwnerPanel()
            "Sign Out" -> confirmSignOut()
            else -> showFeature(name)
        }
    }

    private fun confirmSignOut() {
        AlertDialog.Builder(this)
            .setTitle("Sign out?")
            .setMessage("Do you want to sign out of this device?")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SIGN OUT") { _, _ ->
                auth.signOut()
                val intent = Intent(this, AuthActivity::class.java)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
                startActivity(intent)
                finish()
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

        target.addView(
            row,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(24)
                bottomMargin = dp(12)
            }
        )
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

        row.addView(
            tile(rightIcon, rightTitle, cyan) {
                openOption(rightTitle)
            },
            LinearLayout.LayoutParams(0, dp(105), 1f).apply {
                leftMargin = dp(12)
            }
        )

        target.addView(
            row,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(12)
            }
        )
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

        box.addView(
            text(title, 14f, white, true).apply {
                setPadding(0, dp(7), 0, 0)
            }
        )

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

        target.addView(
            row,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(10)
            }
        )
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
