// app/src/main/java/com/kingdomvoicechat/RoomActivity.kt

package com.kingdomvoicechat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomConfig
import com.zegocloud.uikit.prebuilt.liveaudioroom.ZegoUIKitPrebuiltLiveAudioRoomFragment

class RoomActivity : AppCompatActivity() {

    companion object {
        private const val APP_ID = 976076264L
        private const val MIC_PERMISSION = 1001
    }

    private val appSign: String
        get() = BuildConfig.ZEGO_APP_SIGN

    private val db by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var roomContainer: FrameLayout

    private var roomStarted = false
    private var pendingRoomID = ""
    private var pendingHost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KingdomTheme.applyTo(this)

        roomContainer = FrameLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor(KingdomTheme.BACKGROUND)
        }

        setContentView(roomContainer)

        if (appSign.isBlank()) {
            showError("ZEGOCLOUD AppSign is required")
            return
        }

        val requestedRoomID =
            intent.getStringExtra("roomID")?.trim().orEmpty()

        if (requestedRoomID.isBlank()) {
            showError("Room number is missing")
            return
        }

        verifyRoomAccess(requestedRoomID)
    }

    private fun verifyRoomAccess(roomID: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            showError("Please login first")
            return
        }

        // Official rooms are created and managed through Owner Panel.
        db.collection("officialRooms")
            .document(roomID)
            .get()
            .addOnSuccessListener { officialDoc ->

                if (officialDoc.exists()) {
                    val active =
                        officialDoc.getBoolean("isActive") ?: false

                    if (!active) {
                        showError("This official room is unavailable")
                        return@addOnSuccessListener
                    }

                    // Official rooms are audience-only in this client.
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
        db.collection("rooms")
            .document(roomID)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    showError("Room not found")
                    return@addOnSuccessListener
                }

                val ownerUID =
                    document.getString("ownerUid").orEmpty()

                val status =
                    document.getString("status") ?: "active"

                val locked =
                    document.getBoolean("isLocked") ?: false

                if (status != "active") {
                    showError("This room is unavailable")
                    return@addOnSuccessListener
                }

                if (locked && ownerUID != uid) {
                    showError("This room is locked")
                    return@addOnSuccessListener
                }

                val isHost = ownerUID == uid

                requestMicAndJoin(roomID, isHost)
            }
            .addOnFailureListener {
                showError("Unable to load room details")
            }
    }

    private fun requestMicAndJoin(roomID: String, isHost: Boolean) {
        // Save these BEFORE requesting permission.
        pendingRoomID = roomID
        pendingHost = isHost

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            joinVoiceRoom()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == MIC_PERMISSION) {
            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                joinVoiceRoom()
            } else {
                showError("Microphone permission is required")
            }
        }
    }

    private fun joinVoiceRoom() {
        if (roomStarted) return

        if (pendingRoomID.isBlank()) {
            showError("Room verification is incomplete")
            return
        }

        roomStarted = true

        val firebaseUser =
            FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            roomStarted = false
            showError("Please login first")
            return
        }

        val userID = firebaseUser.uid
            .replace(Regex("[^A-Za-z0-9_]"), "_")

        val userName = (
            firebaseUser.displayName ?: "Kingdom_User"
        ).replace(Regex("[^A-Za-z0-9_]"), "_")

        val config = if (pendingHost) {
            ZegoUIKitPrebuiltLiveAudioRoomConfig.host()
        } else {
            ZegoUIKitPrebuiltLiveAudioRoomConfig.audience()
        }

        val fragment =
            ZegoUIKitPrebuiltLiveAudioRoomFragment.newInstance(
                APP_ID,
                appSign,
                userID,
                userName,
                pendingRoomID,
                config
            )

        supportFragmentManager.beginTransaction()
            .replace(roomContainer.id, fragment)
            .commitAllowingStateLoss()
    }

    private fun showError(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
        finish()
    }
}
