// app/src/main/java/com/kingdomvoicechat/RoomActivity.kt
package com.kingdomvoicechat

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomConfig
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomFragment

/**
 * Kingdom Voice Chat custom room screen.
 *
 * Keeps the existing room verification and ZEGOCLOUD audio-room join flow.
 * The ZEGOCLOUD prebuilt fragment is kept attached as a tiny, transparent
 * audio-session host while this Activity draws the custom room UI.
 *
 * NOTE:
 * - Seat occupancy/locking and staff roles are stored in Firestore.
 * - ZEGOCLOUD's prebuilt seat UI is not the same as this custom seat grid.
 *   Full microphone-seat synchronization requires migrating to Zego Express
 *   Engine/custom UIKit seat management.
 * - Gallery background and selected music currently play locally on this
 *   device. To share them with all room members, upload media to Firebase
 *   Storage and implement synchronized video/audio streaming or Zego mixing.
 * - Enforce all owner/admin permissions in Firestore Security Rules or
 *   trusted Cloud Functions. Client-side checks are not a security boundary.
 */
class RoomActivity : AppCompatActivity() {

    companion object {
        private const val APP_ID = 976076264L
        private const val MIC_PERMISSION = 1001
        private const val DEFAULT_SEATS = 20
        private const val NAVY = 0xFF080D35.toInt()
        private const val CARD = 0xFF101B70.toInt()
        private const val CYAN = 0xFF00DFFF.toInt()
        private const val WHITE = Color.WHITE
    }

    private val appSign: String get() = BuildConfig.ZEGO_APP_SIGN
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private lateinit var root: FrameLayout
    private lateinit var content: LinearLayout
    private lateinit var seatGrid: GridLayout
    private lateinit var roomTitle: TextView
    private lateinit var roomInfo: TextView
    private lateinit var hostSeat: TextView
    private lateinit var statusText: TextView
    private lateinit var backgroundImage: ImageView
    private lateinit var backgroundVideo: VideoView
    private lateinit var audioSessionContainer: FrameLayout

    private var roomStarted = false
    private var pendingRoomID = ""
    private var pendingHost = false
    private var roomOwnerUid = ""
    private var currentRole = "USER"
    private var roomName = "Singing Fun Room"
    private var seatCount = DEFAULT_SEATS
    private var themeColor = CARD
    private var roomLocked = false
    private var roomListener: ListenerRegistration? = null
    private var seatsListener: ListenerRegistration? = null
    private var staffListener: ListenerRegistration? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentSeats: Map<Int, DocumentSnapshot> = emptyMap()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                persistUri(uri)
                backgroundImage.setImageURI(uri)
                backgroundImage.visibility = View.VISIBLE
                backgroundVideo.visibility = View.GONE
                saveRoomFields(mapOf(
                    "backgroundType" to "image",
                    "backgroundUri" to uri.toString()
                ))
                Toast.makeText(this, "Background selected on this device", Toast.LENGTH_SHORT).show()
            }
        }

    private val videoPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                persistUri(uri)
                backgroundVideo.setVideoURI(uri)
                backgroundVideo.visibility = View.VISIBLE
                backgroundImage.visibility = View.GONE
                backgroundVideo.setOnPreparedListener { it.isLooping = true; backgroundVideo.start() }
                saveRoomFields(mapOf(
                    "backgroundType" to "video",
                    "backgroundUri" to uri.toString()
                ))
                Toast.makeText(this, "Video background selected on this device", Toast.LENGTH_SHORT).show()
            }
        }

    private val audioPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) playSelectedAudio(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KingdomTheme.applyTo(this)

        buildScreen()
        if (appSign.isBlank()) {
            showError("ZEGOCLOUD AppSign is required")
            return
        }

        val requestedRoomID = intent.getStringExtra("roomID")?.trim().orEmpty()
        if (requestedRoomID.isBlank()) {
            showError("Room number is missing")
            return
        }
        verifyRoomAccess(requestedRoomID)
    }

    private fun buildScreen() {
        root = FrameLayout(this).apply { setBackgroundColor(NAVY) }
        backgroundImage = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }
        backgroundVideo = VideoView(this).apply { visibility = View.GONE }
        root.addView(backgroundImage, FrameLayout.LayoutParams(-1, -1))
        root.addView(backgroundVideo, FrameLayout.LayoutParams(-1, -1))

        // Keep Zego's fragment attached for the existing prebuilt audio session.
        // The custom interface is drawn above it.
        audioSessionContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            alpha = 0.01f
            isClickable = false
            isFocusable = false
        }
        root.addView(audioSessionContainer, FrameLayout.LayoutParams(1, 1))

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(10))
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val back = text("‹", 34, WHITE).apply {
            setPadding(dp(8), 0, dp(14), 0)
            setOnClickListener { finish() }
        }
        val brand = column()
        brand.addView(text("👑  Kingdom Voice Chat", 19, WHITE, true))
        brand.addView(text("Room ID: ${intent.getStringExtra("roomID").orEmpty()}  •  20 Seats", 12, 0xFFDAE5FF.toInt()))
        top.addView(back)
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(text("⋮", 28, WHITE).apply {
            setPadding(dp(12), 0, dp(8), 0)
            setOnClickListener { showRoomMenu() }
        })
        content.addView(top)

        val banner = column().apply {
            gravity = Gravity.CENTER
            background = rounded(CARD, 22f, CYAN, 2)
            setPadding(dp(12), dp(14), dp(12), dp(14))
        }
        roomTitle = text("Singing Fun Room", 22, WHITE, true)
        roomInfo = text("Let's Sing Together  ♥", 15, 0xFFE5EDFF.toInt())
        statusText = text("● Room Online", 13, 0xFF4CFF87.toInt(), true)
        banner.addView(roomTitle)
        banner.addView(roomInfo)
        banner.addView(statusText)
        content.addView(banner, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(10)
            bottomMargin = dp(10)
        })

        hostSeat = text("👑  HOST SEAT\nRoom owner / authorized staff", 16, WHITE, true).apply {
            gravity = Gravity.CENTER
            background = rounded(0xFF152B91.toInt(), 18f, CYAN, 2)
            setPadding(dp(10), dp(16), dp(10), dp(16))
            setOnClickListener { showHostSeatInfo() }
        }
        content.addView(hostSeat, LinearLayout.LayoutParams(-1, dp(82)).apply {
            bottomMargin = dp(10)
        })

        val seatHeading = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        seatHeading.addView(text("Room Seats", 17, WHITE, true), LinearLayout.LayoutParams(0, -2, 1f))
        seatHeading.addView(text("⚙ Settings", 14, CYAN, true).apply {
            setOnClickListener { openRoomSettings() }
        })
        content.addView(seatHeading)

        seatGrid = GridLayout(this).apply {
            columnCount = 4
            useDefaultMargins = false
        }
        content.addView(seatGrid)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        controls.addView(actionButton("🎵 Music") { audioPicker.launch(arrayOf("audio/*")) })
        controls.addView(actionButton("🖼 Photo") { imagePicker.launch(arrayOf("image/*")) })
        controls.addView(actionButton("🎬 Video") { videoPicker.launch(arrayOf("video/*")) })
        controls.addView(actionButton("⚙ Menu") { showRoomMenu() })
        content.addView(controls)

        val footer = text("Welcome to Kingdom Voice Chat  ♥", 14, 0xFFFFE45C.toInt(), true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(8))
        }
        content.addView(footer)
    }

    private fun verifyRoomAccess(roomID: String) {
        val user = auth.currentUser
        if (user == null) {
            showError("Please login first")
            return
        }

        db.collection("officialRooms").document(roomID).get()
            .addOnSuccessListener { officialDoc ->
                if (officialDoc.exists()) {
                    val active = officialDoc.getBoolean("isActive") ?: false
                    if (!active) {
                        showError("This official room is unavailable")
                        return@addOnSuccessListener
                    }
                    pendingRoomID = roomID
                    pendingHost = false
                    requestMicAndJoin(roomID, false)
                    return@addOnSuccessListener
                }
                verifyPersonalRoom(roomID, user.uid)
            }
            .addOnFailureListener {
                showError("Unable to verify room. Check your connection.")
            }
    }

    private fun verifyPersonalRoom(roomID: String, uid: String) {
        db.collection("rooms").document(roomID).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    showError("Room not found")
                    return@addOnSuccessListener
                }

                roomOwnerUid = document.getString("ownerUid").orEmpty()
                roomName = document.getString("roomName") ?: "Singing Fun Room"
                seatCount = (document.getLong("seatCount")?.toInt() ?: DEFAULT_SEATS)
                    .let { if (it == 12 || it == 16 || it == 20) it else DEFAULT_SEATS }
                themeColor = parseColor(document.getString("themeColor"))
                roomLocked = document.getBoolean("isLocked") ?: false

                val status = document.getString("status") ?: "active"
                if (status != "active") {
                    showError("This room is unavailable")
                    return@addOnSuccessListener
                }
                if (roomLocked && roomOwnerUid != uid) {
                    showError("This room is locked")
                    return@addOnSuccessListener
                }

                currentRole = if (roomOwnerUid == uid) "OWNER" else "USER"
                renderRoomHeader()
                applySavedBackground(document)
                listenRoom(roomID)
                listenSeats(roomID)
                listenStaffRole(roomID, uid)

                pendingRoomID = roomID
                // Preserve original ZEGO host/audience behavior: only room owner
                // enters as Zego host. Admin roles are handled in room UI permissions.
                pendingHost = roomOwnerUid == uid
                requestMicAndJoin(roomID, pendingHost)
            }
            .addOnFailureListener {
                showError("Unable to load room details")
            }
    }

    private fun listenRoom(roomID: String) {
        roomListener?.remove()
        roomListener = db.collection("rooms").document(roomID)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null || !snap.exists()) return@addSnapshotListener
                roomName = snap.getString("roomName") ?: roomName
                seatCount = (snap.getLong("seatCount")?.toInt() ?: seatCount)
                    .let { if (it == 12 || it == 16 || it == 20) it else 20 }
                themeColor = parseColor(snap.getString("themeColor"))
                roomLocked = snap.getBoolean("isLocked") ?: false
                renderRoomHeader()
                renderSeats()
                applySavedBackground(snap)
            }
    }

    private fun listenSeats(roomID: String) {
        seatsListener?.remove()
        seatsListener = db.collection("rooms").document(roomID).collection("seats")
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                currentSeats = snap.documents.associateBy { it.id.toIntOrNull() ?: -1 }
                renderSeats()
            }
    }

    private fun listenStaffRole(roomID: String, uid: String) {
        if (uid == roomOwnerUid) {
            currentRole = "OWNER"
            return
        }
        staffListener?.remove()
        staffListener = db.collection("rooms").document(roomID)
            .collection("staff").document(uid)
            .addSnapshotListener { snap, _ ->
                currentRole = snap?.getString("role")?.uppercase() ?: "USER"
                renderSeats()
            }
    }

    private fun renderRoomHeader() {
        roomTitle.text = roomName
        roomInfo.text = "Let's Sing Together  ♥"
        roomTitle.setTextColor(WHITE)
        roomTitle.textSize = 22f
        roomInfo.textSize = 15f
        roomTitle.contentDescription = "Room name: $roomName"
        roomTitle.setOnClickListener {
            if (canManageRoom()) openRoomSettings()
        }
        statusText.text = if (roomLocked) "● Room Locked" else "● Room Online"
        statusText.setTextColor(if (roomLocked) 0xFFFF7777.toInt() else 0xFF4CFF87.toInt())
        hostSeat.background = rounded(themeColor, 18f, CYAN, 2)
        renderSeats()
    }

    private fun renderSeats() {
        if (!::seatGrid.isInitialized) return
        seatGrid.removeAllViews()
        seatGrid.columnCount = 4
        for (number in 1..seatCount) {
            val doc = currentSeats[number]
            val occupied = doc?.getBoolean("isOccupied") == true ||
                !doc?.getString("userUid").isNullOrBlank()
            val locked = doc?.getBoolean("isLocked") == true
            val name = doc?.getString("userName").orEmpty()
            val dp = doc?.getString("profileImage").orEmpty()

            val label = when {
                locked -> "🔒\nSeat $number"
                occupied -> "●\n${name.ifBlank { "Member" }}"
                else -> "＋\nJoin Seat"
            }
            val cell = TextView(this).apply {
                text = "$number\n$label"
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(WHITE)
                background = rounded(themeColor, 17f, CYAN, 2)
                setPadding(dp(3), dp(10), dp(3), dp(10))
                setOnClickListener { onSeatTapped(number, occupied, locked, doc) }
                contentDescription = if (occupied) "Seat $number occupied by $name" else "Seat $number available"
            }
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(102)
                columnSpec = GridLayout.spec((number - 1) % 4, 1f)
                rowSpec = GridLayout.spec((number - 1) / 4)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            seatGrid.addView(cell, params)
        }
        hostSeat.text = when (currentRole) {
            "OWNER" -> "👑  HOST SEAT\nRoom Owner"
            "SUPER_ADMIN" -> "👑  HOST SEAT\nRoom Super Admin"
            "ADMIN" -> "👑  HOST SEAT\nRoom Admin"
            else -> "👑  HOST SEAT\nReserved for Owner / Admin"
        }
    }

    private fun onSeatTapped(number: Int, occupied: Boolean, locked: Boolean, doc: DocumentSnapshot?) {
        val uid = auth.currentUser?.uid ?: return
        if (locked) {
            if (canLockSeat()) {
                AlertDialog.Builder(this)
                    .setTitle("Seat $number")
                    .setMessage("This seat is locked. Unlock it?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Unlock") { _, _ -> setSeatLock(number, false) }
                    .show()
            } else Toast.makeText(this, "This seat is locked", Toast.LENGTH_SHORT).show()
            return
        }

        if (occupied) {
            val seatUid = doc?.getString("userUid").orEmpty()
            if (seatUid == uid) {
                AlertDialog.Builder(this)
                    .setTitle("Leave seat $number?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Leave") { _, _ -> leaveSeat(number) }
                    .show()
            } else if (canModerate()) {
                showSeatModeration(number, doc)
            } else {
                Toast.makeText(this, "Seat is already occupied", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (roomLocked && currentRole != "OWNER" && currentRole != "SUPER_ADMIN") {
            Toast.makeText(this, "Room is locked", Toast.LENGTH_SHORT).show()
            return
        }
        claimSeat(number)
    }

    private fun claimSeat(number: Int) {
        val user = auth.currentUser ?: return
        val ref = db.collection("rooms").document(pendingRoomID)
            .collection("seats").document(number.toString())
        db.runTransaction { tx ->
            val existing = tx.get(ref)
            check(existing.getBoolean("isLocked") != true) { "Seat is locked" }
            check(existing.getBoolean("isOccupied") != true &&
                existing.getString("userUid").isNullOrBlank()) { "Seat is occupied" }
            tx.set(ref, mapOf(
                "seatNumber" to number,
                "userUid" to user.uid,
                "userName" to (user.displayName ?: "Kingdom User"),
                "profileImage" to (user.photoUrl?.toString() ?: ""),
                "isOccupied" to true,
                "isMuted" to false,
                "isLocked" to false
            ))
        }.addOnFailureListener {
            Toast.makeText(this, it.message ?: "Unable to join seat", Toast.LENGTH_LONG).show()
        }
    }

    private fun leaveSeat(number: Int) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("rooms").document(pendingRoomID)
            .collection("seats").document(number.toString())
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            check(snap.getString("userUid") == uid) { "You do not own this seat" }
            tx.delete(ref)
        }.addOnFailureListener {
            Toast.makeText(this, it.message ?: "Unable to leave seat", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSeatModeration(number: Int, doc: DocumentSnapshot?) {
        val uid = doc?.getString("userUid").orEmpty()
        val options = mutableListOf("Remove user")
        if (canMute()) options.add("Toggle mute")
        if (canLockSeat()) options.add("Lock seat")
        AlertDialog.Builder(this)
            .setTitle("Manage seat $number")
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Remove user" -> db.collection("rooms").document(pendingRoomID)
                        .collection("seats").document(number.toString()).delete()
                    "Toggle mute" -> {
                        val muted = doc?.getBoolean("isMuted") ?: false
                        db.collection("rooms").document(pendingRoomID)
                            .collection("seats").document(number.toString())
                            .update("isMuted", !muted)
                        Toast.makeText(this, "Mute state saved. Connect this to Zego moderation for live audio mute.", Toast.LENGTH_LONG).show()
                    }
                    "Lock seat" -> setSeatLock(number, true)
                }
            }.show()
    }

    private fun setSeatLock(number: Int, locked: Boolean) {
        db.collection("rooms").document(pendingRoomID).collection("seats")
            .document(number.toString())
            .set(mapOf("seatNumber" to number, "isLocked" to locked), SetOptions.merge())
            .addOnFailureListener {
                Toast.makeText(this, it.message ?: "Could not update seat", Toast.LENGTH_LONG).show()
            }
    }

    private fun showHostSeatInfo() {
        if (currentRole == "OWNER" || currentRole == "SUPER_ADMIN" || currentRole == "ADMIN") {
            Toast.makeText(this, "Authorized host role: $currentRole", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Host seat is reserved for room staff", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRoomMenu() {
        val options = mutableListOf("Room information", "Choose photo background", "Choose video background", "Choose music")
        if (canManageRoom()) options.add("Room settings")
        if (currentRole == "OWNER") options.add("Manage room staff")
        if (canLockSeat()) options.add("Lock / unlock room")
        AlertDialog.Builder(this)
            .setTitle(roomName)
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Room information" -> AlertDialog.Builder(this)
                        .setMessage("Room: $roomName\nID: $pendingRoomID\nRole: $currentRole\nSeats: $seatCount")
                        .setPositiveButton("OK", null).show()
                    "Choose photo background" -> imagePicker.launch(arrayOf("image/*"))
                    "Choose video background" -> videoPicker.launch(arrayOf("video/*"))
                    "Choose music" -> audioPicker.launch(arrayOf("audio/*"))
                    "Room settings" -> openRoomSettings()
                    "Manage room staff" -> manageStaff()
                    "Lock / unlock room" -> saveRoomFields(mapOf("isLocked" to !roomLocked))
                }
            }.show()
    }

    private fun openRoomSettings() {
        if (!canManageRoom()) {
            Toast.makeText(this, "Only room owner or authorized super admin can change settings", Toast.LENGTH_SHORT).show()
            return
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }
        val nameInput = EditText(this).apply { setText(roomName); hint = "Room name" }
        val seatSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@RoomActivity, android.R.layout.simple_spinner_dropdown_item, listOf("12", "16", "20"))
            setSelection(listOf(12, 16, 20).indexOf(seatCount).coerceAtLeast(0))
        }
        val colorInput = EditText(this).apply {
            setText(String.format("#%06X", 0xFFFFFF and themeColor))
            hint = "Theme colour HEX"
        }
        box.addView(TextView(this).apply { text = "Room name"; setTextColor(WHITE) })
        box.addView(nameInput)
        box.addView(TextView(this).apply { text = "Seat capacity"; setTextColor(WHITE) })
        box.addView(seatSpinner)
        box.addView(TextView(this).apply { text = "Theme colour"; setTextColor(WHITE) })
        box.addView(colorInput)
        AlertDialog.Builder(this)
            .setTitle("Room Settings")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val colour = colorInput.text.toString().trim()
                if (!colour.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
                    Toast.makeText(this, "Enter colour like #101B70", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val capacity = seatSpinner.selectedItem.toString().toInt()
                saveRoomFields(mapOf(
                    "roomName" to nameInput.text.toString().trim().ifBlank { "Voice Room" },
                    "seatCount" to capacity,
                    "themeColor" to colour
                ))
            }.show()
    }

    private fun manageStaff() {
        if (currentRole != "OWNER") return
        val input = EditText(this).apply { hint = "Enter Firebase user UID" }
        AlertDialog.Builder(this)
            .setTitle("Add room staff")
            .setMessage("Enter the user's Firebase UID. Assign only trusted people.")
            .setView(input)
            .setPositiveButton("Next") { _, _ ->
                val uid = input.text.toString().trim()
                if (uid.isBlank() || uid == roomOwnerUid) return@setPositiveButton
                val roles = arrayOf("SUPER_ADMIN", "ADMIN")
                AlertDialog.Builder(this)
                    .setTitle("Select role")
                    .setItems(roles) { _, index ->
                        val role = roles[index]
                        val member = hashMapOf<String, Any>(
                            "userUid" to uid,
                            "role" to role,
                            "canMute" to true,
                            "canLockSeat" to true,
                            "canRemoveUser" to (role == "SUPER_ADMIN"),
                            "canManageAdmins" to false
                        )
                        db.collection("rooms").document(pendingRoomID)
                            .collection("staff").document(uid).set(member)
                            .addOnSuccessListener {
                                Toast.makeText(this, "$role assigned", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, it.message ?: "Staff update failed", Toast.LENGTH_LONG).show()
                            }
                    }.show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun canManageRoom(): Boolean =
        currentRole == "OWNER" || currentRole == "SUPER_ADMIN"

    private fun canLockSeat(): Boolean =
        currentRole == "OWNER" || currentRole == "SUPER_ADMIN" || currentRole == "ADMIN"

    private fun canMute(): Boolean =
        currentRole == "OWNER" || currentRole == "SUPER_ADMIN" || currentRole == "ADMIN"

    private fun canModerate(): Boolean = canMute()

    private fun saveRoomFields(values: Map<String, Any>) {
        if (!canManageRoom() && currentRole != "OWNER") {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("rooms").document(pendingRoomID)
            .set(values, SetOptions.merge())
            .addOnSuccessListener { Toast.makeText(this, "Room updated", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this, it.message ?: "Update failed", Toast.LENGTH_LONG).show() }
    }

    private fun applySavedBackground(document: DocumentSnapshot) {
        val uriString = document.getString("backgroundUri").orEmpty()
        if (uriString.isBlank()) return
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return
        when (document.getString("backgroundType")) {
            "image" -> {
                backgroundImage.setImageURI(uri)
                backgroundImage.visibility = View.VISIBLE
                backgroundVideo.visibility = View.GONE
            }
            "video" -> {
                backgroundVideo.setVideoURI(uri)
                backgroundVideo.visibility = View.VISIBLE
                backgroundImage.visibility = View.GONE
                backgroundVideo.setOnPreparedListener { it.isLooping = true; backgroundVideo.start() }
            }
        }
    }

    private fun persistUri(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun playSelectedAudio(uri: Uri) {
        runCatching {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@RoomActivity, uri)
                setOnPreparedListener {
                    it.start()
                    Toast.makeText(this@RoomActivity, "Playing on this device", Toast.LENGTH_SHORT).show()
                }
                setOnCompletionListener { it.release(); mediaPlayer = null }
                prepareAsync()
            }
        }.onFailure {
            Toast.makeText(this, "Unable to play selected audio: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestMicAndJoin(roomID: String, isHost: Boolean) {
        pendingRoomID = roomID
        pendingHost = isHost
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            joinVoiceRoom()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MIC_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                joinVoiceRoom()
            } else showError("Microphone permission is required")
        }
    }

    private fun joinVoiceRoom() {
        if (roomStarted) return
        if (pendingRoomID.isBlank()) {
            showError("Room verification is incomplete")
            return
        }
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            showError("Please login first")
            return
        }
        roomStarted = true
        val userID = firebaseUser.uid.replace(Regex("[^A-Za-z0-9_]"), "_")
        val userName = (firebaseUser.displayName ?: "Kingdom_User")
            .replace(Regex("[^A-Za-z0-9_]"), "_")
        val config = if (pendingHost) {
            ZegoUIKitPrebuiltLiveAudioRoomConfig.host()
        } else {
            ZegoUIKitPrebuiltLiveAudioRoomConfig.audience()
        }
        val fragment = ZegoUIKitPrebuiltLiveAudioRoomFragment.newInstance(
            APP_ID, appSign, userID, userName, pendingRoomID, config
        )
        supportFragmentManager.beginTransaction()
            .replace(audioSessionContainer.id, fragment)
            .commitAllowingStateLoss()
    }

    private fun actionButton(label: String, action: () -> Unit) =
        TextView(this).apply {
            text = label
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(WHITE)
            background = rounded(0xFF142A83.toInt(), 14f, CYAN, 1)
            setPadding(dp(8), dp(12), dp(8), dp(12))
            setOnClickListener { action() }
            val lp = LinearLayout.LayoutParams(0, -2, 1f)
            lp.setMargins(dp(3), dp(10), dp(3), dp(6))
            layoutParams = lp
        }

    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

    private fun text(
        value: String,
        size: Int,
        colour: Int,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(colour)
        if (bold) typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun rounded(
        colour: Int,
        radius: Float,
        stroke: Int = Color.TRANSPARENT,
        strokeWidth: Int = 0
    ) = GradientDrawable().apply {
        setColor(colour)
        cornerRadius = dp(radius.toInt()).toFloat()
        if (strokeWidth > 0) setStroke(dp(strokeWidth), stroke)
    }

    private fun parseColor(value: String?): Int =
        runCatching { Color.parseColor(value ?: "#101B70") }.getOrDefault(CARD)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDestroy() {
        roomListener?.remove()
        seatsListener?.remove()
        staffListener?.remove()
        runCatching { backgroundVideo.stopPlayback() }
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
